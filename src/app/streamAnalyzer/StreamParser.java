package app.streamAnalyzer;

import javafx.concurrent.Task;
import model.Tables;
import model.packet.AdaptationFieldHeader;
import model.packet.Packet;
import model.pes.PES;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static model.config.Config.snapshotInterval;


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
                        tickInterval = totalPackets/snapshotInterval;
                    }
                    if (buffer[i] == syncByte) {

                        byte[] packet = Arrays.copyOfRange(buffer, i, i + tsPacketSize);

                        int header = headerParser.parseHeader(packet);
                        byte[] binaryHeader = toBinary(header,tsHeaderBinaryLength);

                        Packet analyzedHeader =  headerParser.analyzeHeader(binaryHeader,packet,packetIndex++);
                        if(isPATanalyzed) {
                            tables.updatePIDmap(analyzedHeader.getPID());
                            if(tick == tickInterval){
                                tables.updateIndexSnapshotMap();
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
                                        adaptationFieldParser.analyzeAdaptationFieldOptionalFields(analyzedHeader.getAdaptationFieldHeader(), binaryAdaptationFieldOptional, analyzedHeader.getIndex(), analyzedHeader.getPID())
                                );
                                if(isPATanalyzed) {
                                    tables.updatePCRmap(analyzedHeader.getAdaptationFieldHeader().getOptionalFields());
                                    updateTables(adaptationFieldParser);
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
                                tables.updatePTSmap(((PES)analyzedHeader.getPayload()));
                                updateTables(PESparser);
                            }
                            packets.add(analyzedHeader);
                        }
                        updateProgress(i, buffer.length);
                    }
                }
                return createPIDnErrorMaps(packets);
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

            tables.setPTSsnapshotMap(parser.tables.getPTSsnapshotMap());
            tables.setPTSpidMap(parser.tables.getPTSpidMap());
            tables.setDTSpidMap(parser.tables.getDTSpidMap());

            tables.setPTSpacketMap(parser.tables.getPTSpacketMap());
            tables.setDTSpacketMap(parser.tables.getDTSpacketMap());
        }
        else if(parser instanceof PSIparser) {
            tables.setPATmap(parser.tables.getPATmap());
            tables.setPMTmap(parser.tables.getPMTmap());
            tables.setESmap(parser.tables.getESmap());

            tables.setServiceNamesMap(parser.tables.getServiceNamesMap());
            tables.setPCRpmtMap(parser.tables.getPCRpmtMap());
        }
        else if(parser instanceof AdaptationFieldParser) {
            tables.setPCRpidMap(parser.tables.getPCRpidMap());
            tables.setPCRpacketMap(parser.tables.getPCRpacketMap());

            tables.setOPCRpidMap(parser.tables.getOPCRpidMap());
            tables.setOPCRpacketMap(parser.tables.getOPCRpacketMap());
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


    private Tables createPIDnErrorMaps(ArrayList<Packet> packets) {
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
        tables.setErrorMap(ErrorMap);
        tables.setPackets(packets);
        return tables;
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
