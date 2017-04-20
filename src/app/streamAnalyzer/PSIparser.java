package app.streamAnalyzer;

import model.packet.Packet;
import model.psi.*;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;


public class PSIparser extends Parser {


    PSIparser(){
    }


    PSI analyzePSI(Packet analyzedHeader, byte[] packet) {
        Packet analyzedPacket = analyzedHeader;

        switch (analyzedHeader.getPID()) {
            case PATpid:
                return analyzePAT(analyzedHeader, packet);
            case CATpid:
                return analyzeCAT(analyzedHeader, packet);
            case TDSTpid:
                return analyzeTDST(analyzedHeader, packet);
            case NIT_STpid:
                return analyzeNIT_ST(analyzedHeader, packet);
            case SDT_BAT_STpid:
              //  return analyzeSDT_BAT(analyzedHeader, packet);
            case EIT_STpid:
              //   return analyzeEIT_ST(analyzedHeader, packet);
            case RST_STpid:
                return analyzeRST_ST(analyzedHeader, packet);
            case TDT_TOT_STpid:
                return analyzeTDT_TOT(analyzedHeader, packet);
            case netSyncPid:
                return analyzeNetSync(analyzedHeader, packet);
            case DITpid:
                return analyzeDITpid(analyzedHeader, packet);
            case SITpid:
                return analyzeSIT(analyzedHeader, packet);
            default:
                return null;
        }
    }


    model.psi.PAT analyzePAT(Packet analyzedHeader, byte[] packet) {

        int position = calculatePosition(analyzedHeader);
        position += 1; //TODO i do not understand why here is one null byte, there is no sign of it in mpeg documentation

        PSI psiCommonFields = analyzePSICommonFields(packet,position);

        position += PSIcommonFieldsLength/ byteBinaryLength;

        final int reserved = 2;
        int sectionLength = psiCommonFields.getSectionLength();

        int[] PATFields = parseNfields(packet,position,sectionLength); //72 - 1024 bitov(9 - 128 Bytov)
        byte[] binaryPATFields = intToBinary(PATFields, sectionLength);

        int tsID = (int) binToInt(binaryPATFields, position=0, transportStreamIDlength);
        short versionNum = (short) binToInt(binaryPATFields, position += transportStreamIDlength +reserved, position += versionNumLength);
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
                psiCommonFields.getTableID(),
                psiCommonFields.getSSI(),
                psiCommonFields.getSectionLength(),
                tsID,versionNum,
                currentNextIndicator,
                sectionNum,
                lastSectionNum,
                PATmap,
                CRC);
    }


    PMT analyzePMT(Packet analyzedHeader, byte[] packet) {

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
                psiCommonFields.getTableID(),
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


    private PSI analyzeSDT_BAT(Packet analyzedHeader, byte[] packet) {

        int position = calculatePosition(analyzedHeader);
        position += 1;

        PSI psiCommonFields = analyzePSICommonFields(packet, position);
        position += PSIcommonFieldsLength / byteBinaryLength;

        if (isSDT(psiCommonFields.getTableID())) {
            int sectionLength = psiCommonFields.getSectionLength();

            int[] SDTfields = parseNfields(packet, position, sectionLength);
            byte[] binarySDTfields = intToBinary(SDTfields, sectionLength);
            //TODO correct lengths
            int tranportStreamID = (int) binToInt(binarySDTfields, position = 0, position += serviceIDlength);
            byte versionNum = (byte) binToInt(binarySDTfields, position += 2, position +=  versionNumLength);
            byte currentNextIndicator = binarySDTfields[position++];
            short sectionNum = (short) binToInt(binarySDTfields, position, position += sectionNumLength);
            short lastSectionNum = (short) binToInt(binarySDTfields, position, position += sectionNumLength);
            int originalNetworkID = (int) binToInt(binarySDTfields, position, position += networkIDlength);
            int serviceID = (int) binToInt(binarySDTfields, position, position += networkIDlength);
            byte EITscheduleFlag = (byte) binToInt(binarySDTfields, position, position += sectionNumLength);
            byte EITpresentFollowingFlag = (byte) binToInt(binarySDTfields, position, position += sectionNumLength);
            byte runningStatus = (byte) binToInt(binarySDTfields, position = 0, position += runningStatusLength);
            byte freeCAmode = (byte) binToInt(binarySDTfields, position = 0, position += runningStatusLength);
            short descriptorsLoopLength = (short) binToInt(binarySDTfields, position = 0, position += descriptorsLengthLength);

            List descriptors = null;
            try {
                descriptors = loadDescriptors(analyzedHeader.getPID(), binarySDTfields, descriptorsLoopLength *= byteBinaryLength, position);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            position += descriptorsLoopLength;

            long CRC = binToInt(binarySDTfields, position, position + CRClength);


            return new SDT(
                    psiCommonFields.getTableID(),
                    psiCommonFields.getSSI(),
                    psiCommonFields.getSectionLength(),
                    tranportStreamID,
                    versionNum,
                    currentNextIndicator,
                    sectionNum,
                    lastSectionNum,
                    originalNetworkID,
                    serviceID,
                    EITscheduleFlag,
                    EITpresentFollowingFlag,
                    runningStatus,
                    freeCAmode,
                    descriptorsLoopLength,
                    descriptors,
                    CRC);
        }
        return null; //new ST();
    }


    private PSI analyzeEIT_ST(Packet analyzedHeader, byte[] packet) {

        int position = calculatePosition(analyzedHeader);
        position += 1;

        PSI psiCommonFields = analyzePSICommonFields(packet, position);
        position += PSIcommonFieldsLength / byteBinaryLength;

        if (isEIT(psiCommonFields.getTableID())) {
            int sectionLength = psiCommonFields.getSectionLength();

            int[] EITfields = parseNfields(packet, position, sectionLength);
            byte[] binaryEITfields = intToBinary(EITfields, sectionLength);

            int serviceID = (int) binToInt(binaryEITfields, position = 0, position += serviceIDlength);
            byte versionNum = (byte) binToInt(binaryEITfields, position += 2, position +=  versionNumLength);
            byte currentNextIndicator = binaryEITfields[position++];
            short sectionNum = (short) binToInt(binaryEITfields, position, position += sectionNumLength);
            short lastSectionNum = (short) binToInt(binaryEITfields, position, position += sectionNumLength);
            int transportStreamID = (int) binToInt(binaryEITfields, position, position += transportStreamIDlength);
            int originalNetworkID = (int) binToInt(binaryEITfields, position, position += networkIDlength);
            short segmentLastSectionNumber = (short) binToInt(binaryEITfields, position, position += sectionNumLength);
            short lastTableID = (short) binToInt(binaryEITfields, position = 0, position += tableIDlength);
            int eventID = (int) binToInt(binaryEITfields, position, position += eventIDlength);
            long startTime = binToInt(binaryEITfields, position = 0, position += startTimeLength);
            int duration = (int) binToInt(binaryEITfields, position = 0, position += durationLength);
            byte runningStatus = (byte) binToInt(binaryEITfields, position = 0, position += runningStatusLength);
            short descriptorsLoopLength = (short) binToInt(binaryEITfields, position = 0, position += descriptorsLengthLength);

            List descriptors = null;
            try {
                descriptors = loadDescriptors(analyzedHeader.getPID(), binaryEITfields, descriptorsLoopLength *= byteBinaryLength, position);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            position += descriptorsLoopLength;

            long CRC = binToInt(binaryEITfields, position, position + CRClength);


            return new EIT(
                    psiCommonFields.getTableID(),
                    psiCommonFields.getSSI(),
                    psiCommonFields.getSectionLength(),
                    serviceID,
                    versionNum,
                    currentNextIndicator,
                    sectionNum,
                    lastSectionNum,
                    transportStreamID,
                    originalNetworkID,
                    segmentLastSectionNumber,
                    lastTableID,
                    eventID,
                    startTime,
                    duration,
                    runningStatus,
                    descriptorsLoopLength,
                    descriptors,
                    CRC);
        }
        return null; //new ST();
    }

    private List loadDescriptors(int PID, byte[] binaryFields, int size, int position) throws UnsupportedEncodingException {

        List descriptors = new ArrayList<>();

        for(size += position; position < size;) {
            short descriptorTag = (short) binToInt(binaryFields, position, position += descriptorTagLength );
            short descriptorLength = (short) binToInt(binaryFields, position, position += descriptorLengthLength);

            switch ( descriptorTag ) {
                case service_descriptor:
                    analyzeServiceDescriptor(PID, binaryFields, position);
                    break;
                case short_event_descriptor:
                    analyzeShortEventDescriptor(PID, binaryFields, position);
                    break;
                case extended_event_descriptor:
                    break;
                case network_name_descriptor:
                    break;
                default:
                    break;
                //TODO all descriptors
            }
            position += descriptorLength;
        }
        return descriptors;
    }


    private void analyzeServiceDescriptor(int PID, byte[] binaryFields, int position) throws UnsupportedEncodingException {

        short serviceType = (short) binToInt(binaryFields, position, position += serviceTypeLength);
        short serviceProviderLength = (short) binToInt(binaryFields, position += 2, position +=  serviceProviderLengthLength);
        byte[] serviceProviderByte = Arrays.copyOfRange(binaryFields, position, position += serviceProviderLength);
        short serviceNameLength = (short) binToInt(binaryFields, position, position += serviceNameLengthLength);
        byte[] serviceNameByte = Arrays.copyOfRange(binaryFields, position, position += serviceNameLength);

        String serviceProviderString = Arrays.toString(serviceProviderByte);
        String serviceProvider = new String(new BigInteger(serviceProviderString, 2).toByteArray(), "UTF-8");

        String serviceNameString= Arrays.toString(serviceNameByte);
        String serviceName =  new String(new BigInteger(serviceNameString, 2).toByteArray(), "UTF-8");

        // descriptors.add(new Descriptor(PID, serviceType,  serviceProvider, serviceName));

        tables.updateServiceName(PID, serviceName);
    }


    private void analyzeShortEventDescriptor(int PID, byte[] binaryFields, int position) throws UnsupportedEncodingException {

        int ISOlanguageCode = (int) binToInt(binaryFields, position, position += ISOlanguageCodeLength);
        short eventNameLength = (byte) binToInt(binaryFields, position += 2, position +=  eventNameLengthLength);
        byte[] eventNameByte = Arrays.copyOfRange(binaryFields, position, position += eventNameLength);
        short textLength = (short) binToInt(binaryFields, position, position += textLengthLength);
        byte[] textByte = Arrays.copyOfRange(binaryFields, position, position += textLength);

        String eventNameString = Arrays.toString(eventNameByte);
        String eventName = new String(new BigInteger(eventNameString, 2).toByteArray(), "UTF-8");

        String textString= Arrays.toString(textByte);
        String text =  new String(new BigInteger(textString, 2).toByteArray(), "UTF-8");

        // descriptors.add(new Descriptor(PID, ISOlanguageCode,  eventName, text));
    }


    private CАТ analyzeCAT(Packet analyzedHeader, byte[] packet) {
        return new CАТ();
    }

    private PSI analyzeSIT(Packet analyzedHeader, byte[] packet) {
        return null;
    }

    private PSI analyzeDITpid(Packet analyzedHeader, byte[] packet) {
        return null;
    }

    private PSI analyzeNetSync(Packet analyzedHeader, byte[] packet) {
        return null;
    }

    private PSI analyzeTDT_TOT(Packet analyzedHeader, byte[] packet) {
        return null;
    }

    private PSI analyzeRST_ST(Packet analyzedHeader, byte[] packet) {
        return null;
    }

    private PSI analyzeNIT_ST(Packet analyzedHeader, byte[] packet) {
        return null;
    }

    private PSI analyzeTDST(Packet analyzedHeader, byte[] packet) {
        return null;
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
