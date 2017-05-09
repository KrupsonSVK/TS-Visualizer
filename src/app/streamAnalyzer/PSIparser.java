package app.streamAnalyzer;

import model.descriptors.Descriptor;
import model.packet.Packet;
import model.psi.*;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;


class PSIparser extends Parser {

    private DescriptorParser descriptor;


    PSIparser(){
        descriptor = new DescriptorParser();
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
                return analyzeSDT_BAT(analyzedHeader, packet);
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

    /**
     * Metóda parsuje jednotlivé bity PAT tabuľky a aktualizuje tabuľku programových asociácií
     *
     * @param analyzedHeader objekt triedy Packet obsahujúci doteraz získané údaje paketu, t.j. hlavička a príp. adaptačné pole
     * @param packet transportný paket v poli bajtov
     * @return triedy PAT obsahujúci získané údaje z PAT tabuľky
     */
    model.psi.PAT analyzePAT(Packet analyzedHeader, byte[] packet) {

        int position = calculatePosition(analyzedHeader) + 1; //zistenie začiatočnej pozície na základe dĺžky predchádzajúcich hlavičiek
        PSI psiCommonFields = analyzePSICommonFields(packet,position); //analýza spoločných polí PSI tabuliek, t.j. tableID,SSI a sectionLength

        position += PSIcommonFieldsLength; //aktualizácie pozície po získaní spoločných polí

        final int reserved = 2; //MPEG konštanta
        int sectionLength = psiCommonFields.getSectionLength(); //dĺžka PAT tabuľky

        int[] PATFields = parseNfields(packet,position,sectionLength); //získanie polí PAT tabuľky do celočíslného poľa
        byte[] binaryPATFields = intToBinary(PATFields, sectionLength); //prevod PAT polí na binárne pole

        int tsID = (int) binToInt(binaryPATFields, position=0, position += transportStreamIDlength); //získanie n-bitov dĺžky transportStreamIDlength ako tsID
        short versionNum = (short) binToInt(binaryPATFields, position += reserved, position += versionNumLength);
        byte currentNextIndicator = binaryPATFields[position++];
        int sectionNum = (int) binToInt(binaryPATFields, position, position += sectionNumLength);
        int lastSectionNum = (int) binToInt(binaryPATFields, position, position += sectionNumLength);

        Map PATmap = new HashMap<Integer,Integer>(); //hashmapa ukladajúca si programové asociácie
        int N = (sectionLength * byteBinaryLength) - mandatoryPATfields; //dĺžka nasledujúceho poľa s cyklom programových asociácií

        for(int i = 0; i < N; i += PATloopLength) { //cyklus získavania programových asociácií PAT tabuľky
            int programNum = (int) binToInt(binaryPATFields, position, position += programNumberLength); //získanie čísla programu
            //získanie PIDu paketu, v ktorom sa nachádza PMT tabuľka daného programu
            int programMapPID= (int) binToInt(binaryPATFields, position += 3, position += PCR_PIDlength);
            PATmap.put(programNum, programMapPID); //vloženie asociácie do hasmapy
        }
        long CRC = nil; //binToInt(binaryPATFields, position, CRClength); //získanie posledného poľa PAT tabuľky, t.j. CRC kontrolný súčet

        tables.updatePAT(PATmap,versionNum);//aktualizácia PAT tabuľky
        //vráti objekt triedy PAT zavolaním jej konštruktora s parametrami získaných atribútov paketu
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
        position += PSIcommonFieldsLength;

        final int reserved = 2;
        int sectionLength = psiCommonFields.getSectionLength();
        sectionLength = (sectionLength * byteBinaryLength) + position + CRClength > packet.length ? tsPacketSize - position - CRClengthByte : sectionLength;
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
        byte[] descriptors = null; //new byte[nLoopDescriptorsLength];
        //TODO load N descriptors
        position += nLoopDescriptorsLength;

        HashMap PMTmap = new HashMap<Integer,Integer>();
        HashMap ESmap = new HashMap<Integer,Integer>();
        int N = (sectionLength * byteBinaryLength) - CRClength;

        for(; position < N;) {
            int streamType = (int) binToInt(binaryPMTFields, position, position += streamTypeLength);
            int elementaryPID = (int) binToInt(binaryPMTFields, position += 3, position += elementaryPIDlength);
            int ESinfoLength = (int) binToInt(binaryPMTFields, position += 4, position += ESinfoLengthLength);
            byte[] NloopDescriptors = null; // new byte[ESinfoLength * byteBinaryLength];
            //TODO load N ES descriptors
            position += ESinfoLength * byteBinaryLength;
            ESmap.put(elementaryPID,streamType);
            PMTmap.put(elementaryPID,programNum);
        }
        long CRC = nil; // binToInt(binaryPMTFields, position, position+CRClength);

        tables.updatePMTnumber();
        tables.updateESmap(ESmap,versionNum);
        tables.updatePMT(PMTmap,versionNum);
        tables.updatePCRpmtMap(analyzedHeader.getPID(),PCR_PID);

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
        position += PSIcommonFieldsLength;

        if (isSDT(psiCommonFields.getTableID())) {
            int sectionLength = psiCommonFields.getSectionLength();
            sectionLength = (sectionLength * byteBinaryLength) + position + CRClength > packet.length ? tsPacketSize - position - CRClengthByte : sectionLength;
            int[] SDTfields = parseNfields(packet, position, sectionLength);
            byte[] binarySDTfields = intToBinary(SDTfields, sectionLength);

            int transportStreamID = (int) binToInt(binarySDTfields, position = 0, position += serviceIDlength);
            byte versionNum = (byte) binToInt(binarySDTfields, position += 2, position +=  versionNumLength);
            byte currentNextIndicator = binarySDTfields[position++];
            short sectionNum = (short) binToInt(binarySDTfields, position, position += sectionNumLength);
            short lastSectionNum = (short) binToInt(binarySDTfields, position, position += sectionNumLength);
            int originalNetworkID = (int) binToInt(binarySDTfields, position, position += networkIDlength);

            position += 8;
            int end = psiCommonFields.getSectionLength() * byteBinaryLength - CRClength;
            List descriptors = null;
            for(;position < end;) {

                int serviceID = (int) binToInt(binarySDTfields, position, position += networkIDlength);
                byte EITscheduleFlag = binarySDTfields[position += 6];
                byte EITpresentFollowingFlag = binarySDTfields[position += 1];
                byte runningStatus = (byte) binToInt(binarySDTfields, position += 1, position += runningStatusLength);
                byte freeCAmode = binarySDTfields[position++];
                short descriptorsLoopLength = (short) binToInt(binarySDTfields, position, position += descriptorsLengthLength);

                if((descriptorsLoopLength *= byteBinaryLength) + position > (sectionLength * byteBinaryLength)){
                   break;
                }
                descriptors = descriptor.loadDescriptors(serviceID, binarySDTfields, descriptorsLoopLength, position);
                tables.setProgramNameMap(descriptor.tables.getProgramNameMap());
                tables.setServiceNamesMap(descriptor.tables.getServiceNamesMap());
                position += descriptorsLoopLength;
            }
            long CRC = nil; // binToInt(binarySDTfields, position, position + CRClength);

            return new SDT(
                    psiCommonFields.getTableID(),
                    psiCommonFields.getSSI(),
                    psiCommonFields.getSectionLength(),
                    transportStreamID,
                    versionNum,
                    currentNextIndicator,
                    sectionNum,
                    lastSectionNum,
                    originalNetworkID,
                    descriptors,
                    CRC);
        }
        return null; //new SDT();
    }


    private PSI analyzeEIT_ST(Packet analyzedHeader, byte[] packet) {

        int position = calculatePosition(analyzedHeader);
        position += 1;

        PSI psiCommonFields = analyzePSICommonFields(packet, position);
        position += PSIcommonFieldsBinaryLength / byteBinaryLength;

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

            List descriptors = descriptor.loadDescriptors(analyzedHeader.getPID(), binaryEITfields, descriptorsLoopLength *= byteBinaryLength, position);
            //position += descriptorsLoopLength;
            long CRC = nil; //binToInt(binaryEITfields, position, position + CRClength);

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

        byte[] binaryPacket = new byte[PSIcommonFieldsBinaryLength];
        for (int index = 0; index < PSIcommonFieldsBinaryLength; index++) {
            binaryPacket[PSIcommonFieldsBinaryLength - index - 1] = getBit(commonFields, index);
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
