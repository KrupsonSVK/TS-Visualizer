package app;

import model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import javafx.concurrent.Task;
import model.pes.PES;
import model.psi.CАТs;
import model.psi.PAT;
import model.psi.PMT_;
import model.psi.PSI;


public class StreamParser extends Config {


    private Task<Table> task;
    private Table table;

    StreamParser() {
        this.task = null;
        this.table = new Table();
    }

    public static int getTsPacketSize() {
        return tsPacketSize;
    }

    public void parseStream(byte[] buffer) {

        this.task = new Task<Table>() {
            @Override
            public Table call() throws InterruptedException, IOException {

                ArrayList<TSpacket> packets = new ArrayList<TSpacket>();

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

                        int header = parseHeader(packet);

                        byte[] binaryHeader = new byte[tsHeaderBitLength];
                        for (int index = 0; index < tsHeaderBitLength; index++) {
                            binaryHeader[tsHeaderBitLength - index - 1] = getBit(header, index);
                        }
                        TSpacket analyzedHeader = analyzeHeader(binaryHeader,packet);

                        if (isAdaptationField(analyzedHeader)) {

                            short adaptationFieldHeader = parseAdaptationFieldHeader(packet);

                            byte[] binaryAdaptationFieldHeader = new byte[tsAdaptationFieldHeaderBitLength];
                            for (int index = 0; index < tsAdaptationFieldHeaderBitLength; index++) {
                                binaryAdaptationFieldHeader[tsAdaptationFieldHeaderBitLength - index - 1] = getBit(adaptationFieldHeader, index);
                            }
                            analyzedHeader.setAdaptationFieldHeader(analyzeAdaptationFieldHeader(binaryAdaptationFieldHeader));

                            short adaptationFieldLength = analyzedHeader.getAdaptationFieldHeader().getAdaptationFieldLength();
                            if (adaptationFieldLength > 0) {
                                //TODO cele pada
//                                byte[] binaryAdaptationFieldOptional = new byte[adaptationFieldLength];
//                                for (int index = 0; ++index <= adaptationFieldLength; )
//                                    binaryAdaptationFieldOptional[adaptationFieldLength - index] = getBit(adaptationFieldHeader, index);
//
//                                TSpacket.AdaptationFieldOptionalFields analyzedAdaptationFieldOptionalFields = analyzeAdaptationFieldOptionalFields(analyzedAdaptationFieldHeader, binaryAdaptationFieldOptional);
                            }
                        }
                        if(isPayload(analyzedHeader.getAdaptationFieldControl())) {
                            if (!isPATanalyzed) {
                                if (isPayloadPSI(analyzedHeader)) {
                                    if (analyzedHeader.getPID() == PATpid) {
                                        analyzePAT(analyzedHeader, packet);
                                        i = firstPosition;
                                        isPATanalyzed = true;
                                    }
                                }
                                continue;
                            }

                            if (isPayloadPSI(analyzedHeader)) {
                                analyzedHeader.setPayload(analyzePSI(analyzedHeader, packet));
                            } else {
                                analyzedHeader.setPayload(analyzePES(analyzedHeader, packet));
                            }
                        }
                        packets.add(analyzedHeader);
                        updateProgress(i, buffer.length);
                    }
                }
                return analyzePackets(packets); // v skutocnosti nic neanalyzuje
            }

        };
    }


    private boolean isPayload(Integer adaptationFieldControl) {
        return adaptationFieldControl != 2;
    }


    private PSI analyzePSI(TSpacket analyzedHeader, byte[] packet) {
        TSpacket analyzedPacket = analyzedHeader;

        if (isPMT(analyzedHeader.getPID())) {
            return analyzePMT(analyzedHeader, packet);
        }
        switch (analyzedHeader.getPID()) {
            case PATpid:
                return analyzePAT(analyzedHeader, packet);
            case CATpid:
                return analyzeCAT(analyzedHeader, packet);
            default:
                return null;
        }
    }


    private boolean isPMT(int pid) {
        if(pid != PATpid) {
            if (table.getPATmap().get(pid) != null) {
                return true;
            }
        }
        return false;
    }


    private model.psi.PAT analyzePAT(TSpacket analyzedHeader, byte[] packet) {

        int position = calculatePosition(analyzedHeader);
        position += 1; //TODO i do not understand why here is one null byte, there is no sign of it in mpeg documentation

        PSI psiCommonFields = analyzePSICommonFields(packet,position);

        position += PSIcommonFieldsLength/byteBitLength;

        final int reserved = 2;
        int sectionLength = psiCommonFields.getSectionLength();

        int[] PATFields = parsePMTfields(packet,position,sectionLength); //72 - 1024 bitov(9 - 128 Bytov)
        byte[] binaryPATFields = intToBinary(sectionLength*byteBitLength,sectionLength,PATFields);

        int tsID = binToInt(binaryPATFields, position=0, tsIDlength);
        short versionNum = (short) binToInt(binaryPATFields, position += tsIDlength+reserved, position += versionNumLength);
        byte currentNextIndicator = binaryPATFields[position++];
        int sectionNum = binToInt(binaryPATFields, position, position += sectionNumLength);
        int lastSectionNum = binToInt(binaryPATFields, position, position += sectionNumLength);

        Map PATmap = new HashMap<Integer,Integer>();
        int N = sectionLength*byteBitLength - mandatoryPATfields;;

        for(int i = 0; i < N; i+=32) {
            int programNum =  binToInt(binaryPATFields, position, position +=16);
            int programMapPID= binToInt(binaryPATFields, position += 3, position +=13);
            PATmap.put(programNum, programMapPID);
        }
        //int networkPID = (int) PATmap.get(0x0000);
        long CRC = binToInt(binaryPATFields, position, position+=CRClength);

        this.table.updateStream(PATmap,versionNum);

        return new PAT(
                psiCommonFields.tableID(),
                psiCommonFields.getSSI(),
                psiCommonFields.getSectionLength(),
                tsID,versionNum,
                currentNextIndicator,
                sectionNum,
                lastSectionNum,
                PATmap,
                CRC);
    }


    private byte[] intToBinary(int size, int length, int[] intFields) {
        byte[] binaryFields = new byte[size];
        int offset = 0;

        for (int index = length-1 ; index >= 0  ; index--) {
            for (int i = 0; i < size / length; i++, offset++) {
                binaryFields[size - offset - 1] = getBit(intFields[index], i);
            }
        }

        return binaryFields;
    }


    private int calculatePosition(TSpacket analyzedHeader) {
        int position=tsHeaderSize;

        if(isAdaptationField(analyzedHeader)) {
            position += analyzedHeader.getAdaptationFieldHeader().getAdaptationFieldLength();
        }
        return position;
    }


    private int[] parsePMTfields(byte[] packet, int pos, int length) {

        int position = pos;
        int[] bytePATfields = new int[length];
        for (int index = 0; index < length; index++) {
            bytePATfields[index] = ((packet[position++]) & 0x000000ff);
        }
        return bytePATfields;
    }


    private PSI analyzePSICommonFields(byte[] packet, int position) {

        int commonFields = parseCommonFields(packet,position);

        byte[] binaryPacket = new byte[PSIcommonFieldsLength];
        for (int index = 0; index < PSIcommonFieldsLength; index++) {
            binaryPacket[PSIcommonFieldsLength - index - 1] = getBit(commonFields, index);
        }
        final int gap = 3;
        short tableID = (short) binToInt(binaryPacket, 0, tableIDlength);
        byte SSI = (byte) binToInt(binaryPacket, tableIDlength, tableIDlength+1);
        int sectionLength = binToInt(binaryPacket, tableIDlength+1+gap, tableIDlength+1+gap+sectionLengthLength);

        return new PSI(tableID,SSI,sectionLength,null);
    }


    private boolean isAdaptationField(TSpacket header) {
        int adaptationFieldControl = header.getAdaptationFieldControl();

        return (adaptationFieldControl == adaptationFieldOnly || adaptationFieldControl == adaptationFieldAndPayload);
    }


    private CАТs analyzeCAT(TSpacket analyzedHeader, byte[] packet) {
        return new CАТs();
    }


    private PMT_ analyzePMT(TSpacket analyzedHeader, byte[] packet) {

        int position = calculatePosition(analyzedHeader);
        position+=1; //TODO i do not understand why here is one null byte, there is no sign of it in mpeg documentation

        PSI psiCommonFields = analyzePSICommonFields(packet,position);
        position += PSIcommonFieldsLength/byteBitLength;

        final int reserved = 2;
        int sectionLength = psiCommonFields.getSectionLength();

        int[] PMTFields = parsePMTfields(packet,position,sectionLength); //72 - 1024 bitov(9 - 128 Bytov)
        byte[] binaryPMTFields = intToBinary(sectionLength*byteBitLength,sectionLength,PMTFields);

        int programNum =  binToInt(binaryPMTFields, position=0, programNumberLength);
        byte versionNum = (byte) binToInt(binaryPMTFields, position += tsIDlength+reserved, position += versionNumLength);
        byte currentNextIndicator = binaryPMTFields[position++];
        byte sectionNum = (byte) binToInt(binaryPMTFields, position += tsIDlength+reserved, position += sectionNumLength);
        byte lastSectionNum = (byte) binToInt(binaryPMTFields, position += tsIDlength+reserved, position += sectionNumLength);
        short PCR_PID = (byte) binToInt(binaryPMTFields, position += tsIDlength+reserved, position += PCR_PIDlength);
        short programInfoLength = (byte) binToInt(binaryPMTFields, position += tsIDlength+reserved, position += programInfoLengthLength);

        //Map descriptorsMap = new HashMap<Integer,Integer>();
        //List descriptors = new ArrayList<Map>();
        byte[] descriptors = new byte[programInfoLength * byteBitLength];
        //TODO load N descriptors


        HashMap PMTmap = new HashMap<Integer,Integer>();
        int N = sectionLength * byteBitLength - mandatoryPATfields;

        for(int i = 0; i < N; i+=40) {
            int streamType = binToInt(binaryPMTFields, position, position += streamTypeLength);
            int elementaryPID = binToInt(binaryPMTFields, position+=3, position += elementaryPIDlength);
            int ESinfoLength = binToInt(binaryPMTFields, position+=6, position += ESinfoLengthLength-2);
            byte[] NsloopDescriptors = new byte[ESinfoLength];
            position += ESinfoLength;
            PMTmap.put(streamType, elementaryPID);
        }
        long CRC = binToInt(binaryPMTFields, position, position+=CRClength);

        this.table.updateStreamPMT(PMTmap,versionNum);

        return new PMT_(
                psiCommonFields.tableID(),
                psiCommonFields.getSSI(),
                psiCommonFields.getSectionLength(),
                programNum,
                versionNum,
                currentNextIndicator,
                sectionNum,
                lastSectionNum,
                PCR_PID,
                programInfoLength,
                descriptors,
                CRC);
    }


    private PES analyzePES(TSpacket analyzedHeader, byte[] packet) {

        int position = calculatePosition(analyzedHeader);

        int PESlength = tsPacketSize - position;
        int[] PESFields = parsePMTfields(packet, position, PESlength);
        byte[] binaryPESFields = intToBinary(PESlength * byteBitLength, PESlength, PESFields);

        if (position < tsPacketSize - packetStartCodePrefixLength ) {
            int pscp = binToInt(binaryPESFields, position=0, position += packetStartCodePrefixLength);

            if (pscp == packetStartCodePrefix) {

                int streamID = binToInt(binaryPESFields, position, position += streamIDlength);
                int PESpacketLength = binToInt(binaryPESFields, position, position += PESpacketLengthLength);
                byte PESscramblingControl = (byte) binToInt(binaryPESFields, position, position += PESscramblingControlLength);
                byte PESpriority = (byte) binToInt(binaryPESFields, position += 2, position += PESpriorityLength);
                byte DataAlignmentIndicator = (byte) binToInt(binaryPESFields, position, position += DataAlignmentIndicatorLength);
                byte copyright = (byte) binToInt(binaryPESFields, position, position += copyrightLength);
                byte OriginalOrCopy = (byte) binToInt(binaryPESFields, position, position += OriginalOrCopyLength);

                table.updateStreamCodes(analyzedHeader.getPID(), Integer.valueOf(streamID));

                return analyzePESoptionalHeader(new PES(
                                streamID,
                                PESpacketLength,
                                PESscramblingControl,
                                PESpriority,
                                DataAlignmentIndicator,
                                copyright,
                                OriginalOrCopy)
                        , binaryPESFields, position);
            }
        }
        return new PES();
    }


    private PES analyzePESoptionalHeader(PES header, byte[] binaryPESFields, int position) {

        byte PTSdtsFlags = (byte) binToInt(binaryPESFields, position, position += PTSdtsFlagsLength);
        byte ESCRflag = (byte) binToInt(binaryPESFields, position, position += PESCRflagLength);
        byte ESrateFlag = (byte) binToInt(binaryPESFields, position, position += ESrateFlagLength);
        byte DSMtrickModeFlag = (byte) binToInt(binaryPESFields, position, position += DSMtrickModeFlagLength);
        byte AdditionalCopyInfoFlag = (byte) binToInt(binaryPESFields, position, position += AdditionalCopyInfoFlagLength);
        byte PEScrcFlag = (byte) binToInt(binaryPESFields, position, position += PEScrcFlagLength);
        byte PESextensionFlag = (byte) binToInt(binaryPESFields, position, position += PESextensionFlagLength);

        int PESheaderDataLength = binToInt(binaryPESFields, position, position += PESheaderDataLengthLength);

        long PTSdts = nil;
        long ESCR = nil;
        long ESrate = nil;
        int DSMtrickMode = nil;
        int AdditionalCopyInfo = nil;
        long PEScrc = nil;

        if(PTSdtsFlags == 1) {
            PTSdts = (byte) binToInt(binaryPESFields, position, position + PTSdtsLength);
        }
        else if(PTSdtsFlags == 2) {
            PTSdts = (byte) binToInt(binaryPESFields, position, position + PTSdtsLength);
        }
        if (ESCRflag == 1) {
            ESCR = (byte) binToInt(binaryPESFields, position, position += ESCRlength);
        }
        if (ESrateFlag == 1) {
            ESrate = (byte) binToInt(binaryPESFields, position, position += ESrateLength);
        }
        if (DSMtrickModeFlag == 1) {
            DSMtrickMode = (byte) binToInt(binaryPESFields, position, position += DSMtrickModeLength);
        }
        if (AdditionalCopyInfoFlag == 1) {
            AdditionalCopyInfo = (byte) binToInt(binaryPESFields, position, position += AdditionalCopyInfoLength);
        }
        if (PEScrcFlag == 1) {
            PEScrc = (byte) binToInt(binaryPESFields, position, position += PEScrcLength);
        }
        if (PESextensionFlag == 1) {
            //TODO PES optional fields extension fields
        }

        return new PES(
                header,
                PTSdtsFlags,
                ESCRflag,
                ESrateFlag,
                DSMtrickModeFlag,
                AdditionalCopyInfoFlag,
                PEScrcFlag,
                PESextensionFlag,
                PESheaderDataLength,
                PTSdts,
                ESCR,
                ESrate,
                DSMtrickMode,
                AdditionalCopyInfo,
                PEScrc
        );
    }


    private boolean isPayloadPSI(TSpacket analyzedHeader) {
        return analyzedHeader.getPID() <= PSImaxPID;
    }


    private int seekBeginning(byte[] buffer, int i){

        for (; i < buffer.length - tsPacketSize; i++) {
            if (buffer[i] == syncByte && buffer[i + tsPacketSize] == syncByte) {
                return i;
            }
        }
        return nil;
    }


    private int parseHeader(byte[] packet) {

        return ((packet[0] << 24) & 0xff000000 |
                (packet[1] << 16) & 0x00ff0000 |
                (packet[2] << 8)  & 0x0000ff00 |
                (packet[3])       & 0x000000ff);
    }


    private short parseAdaptationFieldHeader(byte[] packet) {
        return (short)( (packet[4] << 8) & 0x0000ff00 |
                (packet[5])      & 0x000000ff );
    }

    private int parseCommonFields(byte[] packet, int position) {
        return ((packet[position]   << 16) & 0xff000000 | //TODO tuto toto treba posunut, pozret aka masa je v DVBanalyzery
                (packet[++position] << 8)  & 0x0000ff00 |
                (packet[++position])       & 0x000000ff);
    }


    private TSpacket analyzeHeader(byte[] header, byte[] packet) {

        byte transportErrorIndicator = header[8];
        byte payloadStartIndicator = header[9];
        byte transportPriority = header[10];
        short PID = (short) binToInt(header, 11, 24);
        byte tranportScramblingControl = (byte) binToInt(header, 24, 26);
        byte adaptationFieldControl = (byte) binToInt(header, 24, 28);
        byte continuityCounter = (byte) binToInt(header, 28, 32);
        short adaptationFieldLength =  0;

        return new TSpacket(transportErrorIndicator, payloadStartIndicator, transportPriority, PID, tranportScramblingControl, adaptationFieldControl, continuityCounter, adaptationFieldLength, packet);
    }

    private AdaptationFieldHeader analyzeAdaptationFieldHeader(byte[] adaptationFieldHeader) {

        int i = 8;
        short adaptationFieldLength = (short) binToInt(adaptationFieldHeader, 0, 8);

        byte DI = adaptationFieldHeader[i++];
        byte RAI = adaptationFieldHeader[i++];
        byte ESPI = adaptationFieldHeader[i++];

        byte PF = adaptationFieldHeader[i++];
        byte OF = adaptationFieldHeader[i++];
        byte SPF = adaptationFieldHeader[i++];
        byte TPDF = adaptationFieldHeader[i++];
        byte AFEF = adaptationFieldHeader[i++];

        return new AdaptationFieldHeader(adaptationFieldLength,DI, RAI, ESPI, OF, PF, SPF, TPDF, AFEF, null);
    }

    private AdaptationFieldOptionalFields analyzeAdaptationFieldOptionalFields(AdaptationFieldHeader adaptationFieldHeader, byte[] binaryAdaptationFieldOptionalFields) {

        int i = 0;
        long PCR = nil;
        long OPCR = nil;
        byte spliceCountdown = nil;
        short TPDlength = 0;
        short AFEFlength = 0;
        byte[] TPD = null;
        byte LTWF = 0;
        byte PRF = 0;
        byte SSF = 0;

        if (adaptationFieldHeader.getPCRF() == 0x1) {
            PCR = binToInt(binaryAdaptationFieldOptionalFields, i, i += 42);
        }
        if (adaptationFieldHeader.getOPCRF() == 0x1) {
            OPCR = binToInt(binaryAdaptationFieldOptionalFields, i, i += 42);
        }
        if (adaptationFieldHeader.getSPF() == 0x1){
            spliceCountdown = (byte) binToInt(binaryAdaptationFieldOptionalFields, i, i += 8);
        }
        int offset = i;
        if (adaptationFieldHeader.getTPDF() == 0x1) {

            TPDlength = (byte) binToInt(binaryAdaptationFieldOptionalFields, i, i += 8);
            offset = i;

            TPD = new byte[TPDlength];
            for (int index = 0; offset < i + TPDlength;) {
                TPD[index++] = binaryAdaptationFieldOptionalFields[offset++];
            }
        }

        if (adaptationFieldHeader.getAFEF() == 0x1) {

            AFEFlength = (short) binToInt(binaryAdaptationFieldOptionalFields, offset, offset += 8);

            LTWF = binaryAdaptationFieldOptionalFields[offset++];
            PRF = binaryAdaptationFieldOptionalFields[offset++];
            SSF = binaryAdaptationFieldOptionalFields[offset++];
        }

        //TODO dalsie nepotrebne polia

        return new AdaptationFieldOptionalFields(PCR,OPCR,spliceCountdown,TPDlength,TPD,AFEFlength,LTWF,PRF,SSF);
    }

    private Table analyzePackets(ArrayList<TSpacket> packets) {
        HashMap<Integer, Integer> PIDmap = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> ErrorMap = new HashMap<Integer, Integer>();

        for (TSpacket packet : packets) {

            if (PIDmap.get(packet.getPID()) == null) {
                PIDmap.put(packet.getPID(), 1);
                ErrorMap.put(packet.getPID(), 0);
            } else {
                PIDmap.put(packet.getPID(), PIDmap.get(packet.getPID()) + 1);
            }
            if (packet.getTransportErrorIndicator() == 1) {
                ErrorMap.put(packet.getPID(), ErrorMap.get(packet.getPID()) + 1);
            }
        }

        return new Table(PIDmap, ErrorMap, packets, table.getStreams(), table.getPATmap());
    }


    public byte getBit(int source, int position) {
        return (byte) ((source >> position) & 1);
    }

    public byte getBit(byte source, byte position) {
        return (byte) ((source >> position) & 1);
    }


    private int binToInt(byte[] binaryHeader, int start, int end) {

        int result = 0;
        for (int i = start; i < end; i++) {
            result = (result << 1) | (binaryHeader[i] == 1 ? 1 : 0);
        }
        return result;
    }


    protected Stream analyzeStream(File file, Table table) throws IOException {

        Integer packets = 0, errors = 0;
        BasicFileAttributes attr = Files.readAttributes(Paths.get(file.getAbsolutePath()), BasicFileAttributes.class);

        String size;

        if (attr.size() > 1000000) {
            size = String.format("%.2f MB", (double) attr.size() / (double) 1000000);
        }else if (attr.size() > 1000) {
            size = String.format("%.2f kB", (double) attr.size() / (double) 1000);
        }else{
            size = String.format("%.2f Bytes", (double) attr.size());
        }

        for (Integer value : table.getPIDmap().values()) {
            packets += value;
        }
        for (Integer value : table.getErrorMap().values()) {
            errors += value;
        }
        Map programs = new HashMap<Integer, String>();
        programs.put(2112,"Markiza");
        programs.put(2564,"JOJ");
        programs.put(8191,"TA3");
        programs.put(3121,"Nova");

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
                table.getPIDmap(),
                table.getPackets(),
                programs,
                table.getStreams());
    }

    public Task<Table> getTask() {
        return task;
    }
}
