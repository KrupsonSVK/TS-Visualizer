package app.streamAnalyzer;

import javafx.concurrent.Task;
import model.*;
import model.packet.AdaptationFieldHeader;
import model.packet.Packet;

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

        super.tables =  new Tables();
        PESparser.tables = super.tables;
        PSIparser.tables = super.tables;

        this.task = new Task<Tables>() {
            @Override
            public Tables call() throws InterruptedException, IOException {

                ArrayList<Packet> packets = new ArrayList<>();
                boolean isPATanalyzed = false;
                int firstPosition = nil;
                long packetIndex = 0;
                int totalPackets = 0;

                int tickInterval = nil;
                int tick = 0;

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
                        totalPackets = sumPackets(buffer,i);
                        tickInterval = totalPackets/100;
                    }
                    if (buffer[i] == syncByte) {

                        byte[] packet = Arrays.copyOfRange(buffer, i, i + tsPacketSize);

                        int header = headerParser.parseHeader(packet);
                        byte[] binaryHeader = toBinary(header,tsHeaderBinaryLength);

                        Packet analyzedHeader =  headerParser.analyzeHeader(binaryHeader,packet,packetIndex++);
                        if(isPATanalyzed) {
                            tables.updatePIDmap(analyzedHeader.getPID());
                            if(tick == tickInterval){
                                tables.updateBitrateTable();
                                tick=0;
                            }
                            tick++;
                        }

                        if (adaptationFieldParser.isAdaptationField(analyzedHeader)) {

                            short adaptationFieldHeader =  adaptationFieldParser.parseAdaptationFieldHeader(packet);
                            byte[] binaryAdaptationFieldHeader = toBinary(adaptationFieldHeader,tsAdaptationFieldHeaderBinaryLength);

                            analyzedHeader.setAdaptationFieldHeader(adaptationFieldParser.analyzeAdaptationFieldHeader(binaryAdaptationFieldHeader));

                            short adaptationFieldLength = analyzedHeader.getAdaptationFieldHeader().getAdaptationFieldLength();
                            if (adaptationFieldLength > 1 && isOptionalField(analyzedHeader.getAdaptationFieldHeader())) {

                                int[] adaptationOptionalFields = parseNfields(packet, 6, adaptationFieldLength-1);
                                byte[] binaryAdaptationFieldOptional = intToBinary(adaptationOptionalFields, adaptationFieldLength-1);

                                analyzedHeader.getAdaptationFieldHeader().setAdaptationFieldOptionalFields(
                                        adaptationFieldParser.analyzeAdaptationFieldOptionalFields(analyzedHeader.getAdaptationFieldHeader(), binaryAdaptationFieldOptional)
                                );
                                if(isPATanalyzed) {
                                    tables.updatePCRmap(analyzedHeader.getAdaptationFieldHeader().getOptionalFields());
                                }
                            }
                        }
                        if(isPayload(analyzedHeader.getAdaptationFieldControl())) {
                            if (!isPATanalyzed) { //TODO if PAT not analysed, skip PES and Adaptation field parsing
                                if (analyzedHeader.getPID() == PATpid) {
                                    PSIparser.analyzePAT(analyzedHeader, packet);
                                    i = firstPosition;
                                    packetIndex = 0;
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


            private int sumPackets(byte[] buffer, int i) {
                int totalPackets = 0;
                for (; i < buffer.length; i += tsPacketSize) {
                    if (buffer[i] == syncByte) {
                        totalPackets++;
                    }
                }
                return totalPackets-1;
            }
        };
    }


    private byte[] toBinary(int source, int length) {
        byte[] binaryField = new byte[length];
        for (int index = 0; index < length; index++) {
            binaryField[length - index - 1] = getBit(source, index);
        }
        return binaryField;
    }


    private boolean isOptionalField(AdaptationFieldHeader adaptationFieldHeader) {
        if (adaptationFieldHeader.getPCRF() == 0x01 || adaptationFieldHeader.getOPCRF() == 0x01 || adaptationFieldHeader.getSplicingPointFlag() == 0x01 || adaptationFieldHeader.getAFEflag() == 0x01) {
            return true;
        }
        return false;
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


    private Tables createTables(ArrayList<Packet> packets) {
        HashMap<Integer, Integer> PIDmap = new HashMap<>();
        HashMap<Integer, Integer> ErrorMap = new HashMap<>();

        for (Packet packet : packets) {
            if (PIDmap.get(packet.getPID()) == null) {
                PIDmap.put(packet.getPID(), 1);
                ErrorMap.put(packet.getPID(), 0);
            }
            else {
                PIDmap.put(packet.getPID(), PIDmap.get(packet.getPID()) + 1);
            }
            if (packet.getTransportErrorIndicator() == 0x01) {
                ErrorMap.put(packet.getPID(), ErrorMap.get(packet.getPID()) + 1);
            }
        }
        return new Tables(
                ErrorMap,
                packets,
                tables.getStreamCodes(),
                tables.getPATmap(),
                tables.getTimeMap(),
                tables.getESmap(),
                tables.getPMTmap(),
                tables.getServiceNamesMap(),
                tables.getPIDmap(),
                tables.getPCRmap(),
                tables.getProgramMap(),
                tables.getBitrateMap()
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
        for (Object value : tables.getPIDmap().values()) {
            packets += (Integer)value; //TODO remove redundant
        }
        for (Integer value : ((HashMap<Integer,Integer>)tables.getErrorMap()).values()) {
            errors += value;
        }
     tables.setProgramMap(createPrograms(tables.getPMTmap()));

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
