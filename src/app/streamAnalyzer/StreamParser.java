package app.streamAnalyzer;

import javafx.concurrent.Task;
import model.*;
import model.pes.PES;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;


public class StreamParser extends Parser {

    private HeaderParser headerParser;
    private AdaptationFieldParser adaptationFieldParser;
    private PSIparser PSIparser;
    private PESparser PESparser;
    private Task<Tables> task;


    public StreamParser(Task task) {
        this.task = task;
        headerParser = new HeaderParser();
        adaptationFieldParser = new AdaptationFieldParser();
        PSIparser = new PSIparser();
        PESparser = new PESparser();
    }


    public void parseStream(byte[] buffer) {

        this.task = new Task<Tables>() {
            @Override
            public Tables call() throws InterruptedException, IOException {

                ArrayList<TSpacket> packets = new ArrayList<>();
                boolean isPATanalyzed = false;
                int firstPosition = nil;

                for (int i = 0; i < buffer.length; i += tsPacketSize) {

                    if(!isPATanalyzed && i>=buffer.length - 2*tsPacketSize) { // if we are almost at the and there is no PAT, pretend like there is
                        isPATanalyzed = true;
                        i = firstPosition;
                    }
                    if (i == 0) {
                        i = seekBeginning(buffer, i);
                        if (i == nil) {
                            throw new IOException("File does not contain TS stream!");
                        }
                        firstPosition = i;
                    }
                    if (buffer[i] == syncByte) {

                        byte[] packet = Arrays.copyOfRange(buffer, i, i + tsPacketSize);

                        int header = headerParser.parseHeader(packet);

                        byte[] binaryHeader = new byte[tsHeaderBinaryLength];
                        for (int index = 0; index < tsHeaderBinaryLength; index++) {
                            binaryHeader[tsHeaderBinaryLength - index - 1] = getBit(header, index);
                        }
                        TSpacket analyzedHeader =  headerParser.analyzeHeader(binaryHeader,packet);

                        if (adaptationFieldParser.isAdaptationField(analyzedHeader)) {

                            short adaptationFieldHeader =  adaptationFieldParser.parseAdaptationFieldHeader(packet);

                            byte[] binaryAdaptationFieldHeader = new byte[tsAdaptationFieldHeaderBinaryLength];
                            for (int index = 0; index < tsAdaptationFieldHeaderBinaryLength; index++) {
                                binaryAdaptationFieldHeader[tsAdaptationFieldHeaderBinaryLength - index - 1] = getBit(adaptationFieldHeader, index);
                            }
                            analyzedHeader.setAdaptationFieldHeader(adaptationFieldParser.analyzeAdaptationFieldHeader(binaryAdaptationFieldHeader));

                            short adaptationFieldLength = analyzedHeader.getAdaptationFieldHeader().getAdaptationFieldLength();
                            if (adaptationFieldLength > 0) {

                                int optionalFieldsBinaryLength = adaptationFieldLength * byteBinaryLength;
                                byte[] binaryAdaptationFieldOptional = new byte[optionalFieldsBinaryLength];

                                for (int index = 0; ++index <= optionalFieldsBinaryLength; ) {
                                    binaryAdaptationFieldOptional[optionalFieldsBinaryLength - index] = getBit(optionalFieldsBinaryLength, index);
                                }
                                analyzedHeader.getAdaptationFieldHeader().setAdaptationFieldOptionalFields(
                                        adaptationFieldParser.analyzeAdaptationFieldOptionalFields(analyzedHeader.getAdaptationFieldHeader(), binaryAdaptationFieldOptional)
                                );
                            }
                        }
                        if(isPayload(analyzedHeader.getAdaptationFieldControl())) {
                            if (!isPATanalyzed) {
                                if (analyzedHeader.getPID() == PATpid) {
                                    PSIparser.analyzePAT(analyzedHeader, packet);
                                    i = firstPosition;
                                    isPATanalyzed = true;
                                }
                                continue;
                            }
                            if (PSIparser.isPayloadPSI(analyzedHeader.getPID())) {
                                analyzedHeader.setPayload(PSIparser.analyzePSI(analyzedHeader, packet));
                                updateTables(PSIparser);
                            }
                            else if (PSIparser.isPMT(analyzedHeader.getPID())) {
                                analyzedHeader.setPayload(PSIparser.analyzePMT(analyzedHeader, packet));
                                updateTables(PSIparser);
                            }
                            else {
                                analyzedHeader.setPayload(PESparser.analyzePES(analyzedHeader, packet));
                                updateTables(PESparser);
                            }
                            packets.add(analyzedHeader);
                        }
                        updateProgress(i, buffer.length);
                    }
                }
                return createTables(packets);
            }
        };
    }


    private void updateTables(Parser parser) {

        if(parser instanceof PESparser) {
            tables.setStreamCodes(parser.tables.getStreamCodes());
            tables.setPacketsSizeMap(parser.tables.getPacketsSizeMap());
            tables.setTimeMap(parser.tables.getTimeMap());
        }
        else if(parser instanceof PSIparser) {
            tables.setPATmap(parser.tables.getPATmap());
            tables.setPMTmap(parser.tables.getPMTmap());
            tables.setESmap(parser.tables.getESmap());
        }
    }


    private int seekBeginning(byte[] buffer, int i){

        for (; i < buffer.length - tsPacketSize; i++) {
            if (buffer[i] == syncByte && buffer[i + tsPacketSize] == syncByte) {
                return i;
            }
        }
        return nil;
    }


    private Tables createTables(ArrayList<TSpacket> packets) {
        HashMap<Integer, Integer> PIDmap = new HashMap<>();
        HashMap<Integer, Integer> ErrorMap = new HashMap<>();

        for (TSpacket packet : packets) {
            if (PIDmap.get(packet.getPID()) == null) {
                PIDmap.put(packet.getPID(), 1);
                ErrorMap.put(packet.getPID(), 0);
            }
            else {
                PIDmap.put(packet.getPID(), PIDmap.get(packet.getPID()) + 1);
            }
            if (packet.getTransportErrorIndicator() == 1) {
                ErrorMap.put(packet.getPID(), ErrorMap.get(packet.getPID()) + 1);
            }
        }
        return new Tables(
                PIDmap,
                ErrorMap,
                packets,
                tables.getStreamCodes(),
                tables.getPATmap(),
                tables.getTimeMap(),
                tables.getESmap(),
                tables.getPMTmap(),
                tables.getServiceNamesMap()
        );
    }


    public Stream analyzeStream(File file, Tables tables) throws IOException {

        Integer packets = 0, errors = 0;
        BasicFileAttributes attr = Files.readAttributes(Paths.get(file.getAbsolutePath()), BasicFileAttributes.class);

        String size;
        if (attr.size() > 1000000) {
            size = String.format("%.2f MB", (double) attr.size() / (double) 1000000);
        }
        else if (attr.size() > 1000) {
            size = String.format("%.2f kB", (double) attr.size() / (double) 1000);
        }
        else {
            size = String.format("%.2f Bytes", (double) attr.size());
        }
        for (Integer value : tables.getPIDmap().values()) {
            packets += value;
        }
        for (Integer value : tables.getErrorMap().values()) {
            errors += value;
        }
        Map programs = createPrograms((HashMap<Integer, Integer>) tables.getPMTmap());

        return new Stream(
                file.getName(),
                file.getAbsolutePath(),
                size,
                attr.creationTime().toString(),
                attr.lastAccessTime().toString(),
                attr.lastModifiedTime().toString(),
                attr.isRegularFile(),
                file.canWrite(),
                Files.getOwner(file.toPath()).toString(),
                StreamParser.getTsPacketSize(),
                packets,
                errors,
                programs,
                tables
        );
    }


    private boolean isPayload(Integer adaptationFieldControl) {
        return adaptationFieldControl != 2;
    }

    public static int getTsPacketSize() {
        return tsPacketSize;
    }

    public Task<Tables> getTask() {
        return task;
    }
}
