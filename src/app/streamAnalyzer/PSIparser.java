package app.streamAnalyzer;

import javafx.scene.control.Tab;
import model.TSpacket;
import model.Tables;
import model.psi.CАТ;
import model.psi.PAT;
import model.psi.PMT;
import model.psi.PSI;

import java.util.HashMap;
import java.util.Map;


public class PSIparser extends Parser {


    PSIparser(){
    }


    private CАТ analyzeCAT(TSpacket analyzedHeader, byte[] packet) {
        return new CАТ();
    }


    PMT analyzePMT(TSpacket analyzedHeader, byte[] packet) {

        int position = calculatePosition(analyzedHeader);
        position+=1; //TODO i do not understand why here is one null byte, there is no sign of it in mpeg documentation

        PSI psiCommonFields = analyzePSICommonFields(packet,position);
        position += PSIcommonFieldsLength / byteBinaryLength;

        final int reserved = 2;
        int sectionLength = psiCommonFields.getSectionLength();

        int[] PMTFields = parseNfields(packet,position,sectionLength); //72 - 1024 bitov(9 - 128 Bytov)
        byte[] binaryPMTFields = intToBinary(PMTFields, sectionLength);

        int programNum = (int) binToInt(binaryPMTFields, position=0, position += programNumberLength);
        byte versionNum = (byte) binToInt(binaryPMTFields, position += reserved, position += versionNumLength);
        byte currentNextIndicator = binaryPMTFields[position++];
        byte sectionNum = (byte) binToInt(binaryPMTFields, position, position += sectionNumLength);
        byte lastSectionNum = (byte) binToInt(binaryPMTFields, position, position += sectionNumLength);
        short PCR_PID = (short) binToInt(binaryPMTFields, position += reserved+1, position += PCR_PIDlength);
        short programInfoLength = (short) binToInt(binaryPMTFields, position += reserved+2, position += programInfoLengthLength);

        int nLoopDescriptorsLength = programInfoLength * byteBinaryLength;
        byte[] descriptors = new byte[nLoopDescriptorsLength];
        //TODO load N descriptors
        position += nLoopDescriptorsLength;

        HashMap PMTmap = new HashMap<Integer,Integer>();
        HashMap ESmap = new HashMap<Integer,Integer>();
        int N = (sectionLength * byteBinaryLength) - CRClength;

        for(; position < N;) {
            int streamType = (int) binToInt(binaryPMTFields, position, position += streamTypeLength);
            int elementaryPID = (int) binToInt(binaryPMTFields, position += 3, position += elementaryPIDlength);
            int ESinfoLength = (int) binToInt(binaryPMTFields, position += 4, position += ESinfoLengthLength);
            byte[] NloopDescriptors = new byte[ESinfoLength * byteBinaryLength];
            //TODO load N ES descriptors
            position += ESinfoLength * byteBinaryLength;
            ESmap.put(elementaryPID,streamType);
            PMTmap.put(elementaryPID,programNum);
        }
        long CRC = binToInt(binaryPMTFields, position, position+CRClength);

        tables.updateESmap(ESmap,versionNum);
        tables.updatePMT(PMTmap,versionNum);

        return new PMT(
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


    PSI analyzePSI(TSpacket analyzedHeader, byte[] packet) {
        TSpacket analyzedPacket = analyzedHeader;

        switch (analyzedHeader.getPID()) {
            case PATpid:
                return analyzePAT(analyzedHeader, packet);
            case CATpid:
                return analyzeCAT(analyzedHeader, packet);
            default:
                return null;
        }
    }


    private PSI analyzePSICommonFields(byte[] packet, int startPosition) {

        int commonFields = parseCommonFields(packet,startPosition);

        byte[] binaryPacket = new byte[PSIcommonFieldsLength];
        for (int index = 0; index < PSIcommonFieldsLength; index++) {
            binaryPacket[PSIcommonFieldsLength - index - 1] = getBit(commonFields, index);
        }
        int position = 0;
        final int reserved = 2;
        short tableID = (short) binToInt(binaryPacket, position, position += tableIDlength);
        byte SSI = (byte) binToInt(binaryPacket,  position ,  position += 1);
        int sectionLength = (int) binToInt(binaryPacket, position += 1+reserved, position += sectionLengthLength);

        return new PSI(tableID,SSI,sectionLength,null);
    }


    model.psi.PAT analyzePAT(TSpacket analyzedHeader, byte[] packet) {

        int position = calculatePosition(analyzedHeader);
        position += 1; //TODO i do not understand why here is one null byte, there is no sign of it in mpeg documentation

        PSI psiCommonFields = analyzePSICommonFields(packet,position);

        position += PSIcommonFieldsLength/ byteBinaryLength;

        final int reserved = 2;
        int sectionLength = psiCommonFields.getSectionLength();

        int[] PATFields = parseNfields(packet,position,sectionLength); //72 - 1024 bitov(9 - 128 Bytov)
        byte[] binaryPATFields = intToBinary(PATFields, sectionLength);

        int tsID = (int) binToInt(binaryPATFields, position=0, tsIDlength);
        short versionNum = (short) binToInt(binaryPATFields, position += tsIDlength+reserved, position += versionNumLength);
        byte currentNextIndicator = binaryPATFields[position++];
        int sectionNum = (int) binToInt(binaryPATFields, position, position += sectionNumLength);
        int lastSectionNum = (int) binToInt(binaryPATFields, position, position += sectionNumLength);

        Map PATmap = new HashMap<Integer,Integer>();
        int N = (sectionLength * byteBinaryLength) - mandatoryPATfields;

        for(int i = 0; i < N; i+=32) {
            int programNum = (int) binToInt(binaryPATFields, position, position +=16);
            int programMapPID= (int) binToInt(binaryPATFields, position += 3, position +=13);
            PATmap.put(programNum, programMapPID);
        }
        //int networkPID = (int) PATmap.get(0x0000);
        long CRC = binToInt(binaryPATFields, position, position+=CRClength);

        tables.updatePAT(PATmap,versionNum);

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

    private int parseCommonFields(byte[] packet, int position) {
        return ((packet[position]   << 16) & 0x00ff0000 |
                (packet[++position] << 8)  & 0x0000ff00 |
                (packet[++position])       & 0x000000ff);
    }


    boolean isPayloadPSI(int PID) {
        return PID <= PSImaxPID;
    }


    boolean isPMT(Integer pid) {
        if(pid != PATpid) {
            Integer key= null;
            HashMap<Integer, Integer> PATmap = (HashMap<Integer, Integer>) tables.getPATmap();

            for( Map.Entry<Integer, Integer> entry : PATmap.entrySet()){
                if(pid.equals(entry.getValue())){
                    return true;
                }
            }
        }
        return false;
    }
}
