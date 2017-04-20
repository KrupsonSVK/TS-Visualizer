package view.visualizationTab;


import javafx.scene.control.TreeItem;
import model.*;
import model.config.DVB;
import javafx.scene.control.Tooltip;
import model.pes.PES;
import model.psi.PMT;
import model.psi.PSI;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static model.config.Config.*;
import static model.config.DVB.*;

public class PacketInfo extends Tooltip {

    private ArrayList<TSpacket> packets;
    private Stream stream;


    PacketInfo(){
        super();
    }


    String getPacketInfo(int hashPacket) {

        for (TSpacket packet : packets) {
            if (packet.hashCode() == hashPacket) {
                return createPacketInfo(packet);
            }
        }
        return "Unable to collect packet data!";
    }


    private String createPacketInfo(TSpacket packet) {

        return ( createHeaderOutput(packet) +
                        createAdaptationFieldHeaderOutput(packet.getAdaptationFieldHeader()) +
                        createPESheaderOutput(packet.getPayload()) +
                        createPSItableOutput(packet.getPayload()) +
                        createDataOutput(getHexSequence(packet.getData())) +
                        createASCIIoutput(packet.getData(), packet.getPID()) +
                        createPATOutput(packet.getPID(),stream.getTables().getPATmap()) +
                        createPMTOutput(packet.getPID(),packet.getPayload(),stream.getTables().getPMTmap(),stream.getTables().getESmap(), stream.getTables().getPATmap()) + "\n"
        );
    }


    private String createPSItableOutput(Payload payload) {
        if(payload instanceof PSI) {
            return ( "PSI Table:\n\n" + "Table type: " + getTableName(((PSI)payload).getTableID()) + "\n" + "\n\n");
        }
        return "";
    }


    private <K,V> String createPMTOutput(Integer PID, Payload payload, Map PMTmap, Map<K, V> ESmap, Map PATmap) {

        if (isPMT(PATmap,PID)) {
            Integer service = (Integer)getByValue(PATmap, PID);
            //Program Map Table:  P")

            StringBuilder descriptionBuilder = new StringBuilder("Program Map Table:\n\n");
            descriptionBuilder.append("Program: " + toHex(service) + " (" + stream.getTables().getProgramMap().get(service) + ")\n");
            descriptionBuilder.append("PCR PID: " + toHex(((PMT)payload).getPCR_PID()) + " (" + ((PMT)payload).getPCR_PID() + ")\n");
            StringBuilder componentBuilder = new StringBuilder();

            for (Map.Entry<K, V> programEntry : ((Map<K, V>)PMTmap).entrySet()) {

                if (programEntry.getValue().equals(service)) {
                    for (Map.Entry<K, V> ESentry : ESmap.entrySet()) {

                        if (ESentry.getKey().equals(programEntry.getKey())) {
                            componentBuilder.append("Component PID: " + toHex((Integer) ESentry.getKey()) + " (" + ESentry.getKey().toString() + ")" + "\n");
                            componentBuilder.append("-stream type: " + getElementaryStreamDescriptor((Integer) ESentry.getValue()) + "\n");
                        }
                    }
                }
            }
            return ("\n\n" + descriptionBuilder.toString() +"\n" + componentBuilder.toString());
        } else {
            return "";
        }
    }


    boolean isPMT(Map PATmap, int PID) {
        return getByValue(PATmap,PID) != null;
    }


    public <K, V> K getByValue(Map<K,V> map, V value) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().equals(value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }


    private String createPATOutput(int pid, Map<Integer, Integer> PATmap) {
        if(pid == PATpid) {
            StringBuilder stringBuilder = new StringBuilder();

            for (Map.Entry<Integer, Integer> entry : PATmap.entrySet()) {
                stringBuilder.append(toHex(entry.getKey()) + ":" +  toHex(entry.getValue()) + " (" + String.format("%04d",entry.getKey()) + " -> " + String.format("%04d",entry.getValue()) + ")\n");
            }
            return "\n\nProgram Association Table: \n\n            Service -> PMT PID\n" + stringBuilder.toString();
        }
        return "";
    }


    private String createHeaderOutput(TSpacket packet) {
        return ("Header: \n\n" +
                        "Packet PID: " + toHex(packet.getPID()) + " (" + packet.getPID() + ") - " + getPacketName(packet.getPID()) + "\n" +
                        "Transport Error Indicator: " + packet.getTransportErrorIndicator() + (packet.getTransportErrorIndicator() == 0x0 ? " (No error)" : " (Error packet)") + "\n" +
                        "Payload Start Indicator: " + packet.getPayloadStartIndicator() + (packet.getPayloadStartIndicator() == 0x0 ? " (Normal payload)" : " (Payload start)") + "\n" +
                        "Transport priority: " + packet.getTransportPriority() + (packet.getTransportPriority() == 0x0 ? " (Normal priority)" : " (High priority)") + "\n" +
                        "Transport Scrambling Control: " + packet.getTransportScramblingControl() + (packet.getTransportScramblingControl() == 0x0 ? " (Not scrambled)" : " (Scrambled)") + "\n" +
                        "Continuity Counter: " + String.format("0x%01X", packet.getContinuityCounter() & 0xFFFFF) + " (" + packet.getContinuityCounter() + ")" + "\n" +
                        "Adaptation Field Control: " + packet.getAdaptationFieldControl() + (packet.getAdaptationFieldControl() == 0x1 ?
                        " (Payload only)" : (packet.getAdaptationFieldControl() == 0x2 ? " (Adaptation field only)" : " (Adaptation field followed by payload)")) + "\n\n\n"
        );
    }


    private String createPESheaderOutput(Payload payload) {
        if (payload != null) {
            if (payload.hasPESheader()) {
                PES pesPacket = (PES) payload;
                return ("PES header: \n\n" +
                        "Stream ID: " + toHex(pesPacket.getStreamID()) +  " (" + pesPacket.getStreamID() + ") = " + DVB.getStreamDescription(pesPacket.getStreamID()) + "\n" +
                        "PES packet length: " + pesPacket.getPESpacketLength() + " Bytes \n" +
                        "PES scrambling control: " + pesPacket.getPESscramblingControl() + (pesPacket.getPESscramblingControl()==0x0 ? " (Not scrambled)" : " (Scrambled)") + "\n" +
                        "PES priority: " + pesPacket.getPESpriority() + (pesPacket.getPESpriority()== 0x0 ? " (Normal priority)" : " (High priority)") + "\n" +
                        "Data alignment indicator: " + pesPacket.getDataAlignmentIndicator() + (pesPacket.getDataAlignmentIndicator()==0x1 ? " (PES packet header is immediately followed \n" + "by the video start code or audio syncword \n" + "indicated in the data_stream_alignment_descriptor)" : " (Normal payload)") + "\n" +
                        "Copyright: " + pesPacket.getCopyright() + (pesPacket.getCopyright()==0x1 ? " (Content copyrighted)" : " (Copyright not defined)") + "\n" +
                        "Original or copy: " + pesPacket.getOriginalOrCopy() + (pesPacket.getOriginalOrCopy()==0x1 ? " (Original)" : " (Copy)") + "\n" +
                        "PTS DTS flags: " + pesPacket.getPTSdtsFlags() + (pesPacket.getPTSdtsFlags()==0x3 ? " (PTS and DTS present)" : (pesPacket.getPTSdtsFlags()==2 ? " (PTS present)" : " (PTS and DTS not present)")) + "\n" +
                        "ES rate flag: " + pesPacket.getESrateFlag() + (pesPacket.getESrateFlag()==0x1 ? " (Present)" : " (Not present)") + "\n" +
                        "DSM trick mode flag: " + pesPacket.getDSMtrickModeFlag()  + (pesPacket.getDSMtrickModeFlag()==0x1 ? " (Present)" : " (Not present)") + "\n" +
                        "Additional copy info flag: " + pesPacket.getAdditionalCopyInfoFlag() + (pesPacket.getAdditionalCopyInfoFlag()==0x1 ? " (Present)" : " (Not present)") + "\n" +
                        "PES CRC flag: " + pesPacket.getPEScrcFlag() + (pesPacket.getPEScrcFlag()==0x1 ? " (Present)" : " (Not present)") + "\n" +
                        "PES extension flag: " + pesPacket.getPESextensionFlag() + (pesPacket.getPESextensionFlag()==0x1 ? " (Present)" : " (Not present)") + "\n" +
                        "PES header data length: " + pesPacket.getPESheaderDataLength() + " Bytes\n" +
                        createPESoptionalFieldsOutput(pesPacket) + "\n\n"
                );
            }
        }
        return "";
    }


    private String createPESoptionalFieldsOutput(PES optionalPESheader) {
        StringBuilder timestampBuilder = new StringBuilder();
        if(optionalPESheader.getPTSdtsFlags() >= 0x2){
            timestampBuilder.append("PTS: " + String.format("0x%09X", optionalPESheader.getPTStimestamp()) + optionalPESheader.parseTimestamp(optionalPESheader.getPTStimestamp()) + "\n");
        }
        if(optionalPESheader.getPTSdtsFlags() == 0x3){
            timestampBuilder.append("DTS: " + String.format("0x%09X", optionalPESheader.getDTStimestamp()&0xFFFFFFFF)  + optionalPESheader.parseTimestamp(optionalPESheader.getDTStimestamp()) + "\n");
        }
        return (timestampBuilder.toString() +
                (optionalPESheader.getESCRflag() == 0x1 ? "ESCR: " + String.format("0x%06X", optionalPESheader.getESCR() & 0xFFFFF) + "\n" : "" ) +
                (optionalPESheader.getESrateFlag() == 0x1 ? "ES rate: " + String.format("0x%03X", optionalPESheader.getESrate() & 0xFFFFF) + "\n" : "" ) +
                (optionalPESheader.getDSMtrickModeFlag() == 0x1 ? "DSM trick mode: " + String.format("0x%01X", optionalPESheader.getDSMtrickModeFlag() & 0xFFFFF) + "\n" : "" ) +
                (optionalPESheader.getAdditionalCopyInfoFlag() == 0x1 ? "Additional copy info: " + String.format("0x%01X", optionalPESheader.getAdditionalCopyInfoFlag() & 0xFFFFF) + "\n" : "" ) +
                (optionalPESheader.getPEScrcFlag() == 0x1 ? "PES CRC: " + String.format("0x%02X", optionalPESheader.getPEScrcFlag() & 0xFFFFF) + "\n" : "" )
        );
    }


    private String createAdaptationFieldHeaderOutput(AdaptationFieldHeader adaptationFieldHeader) {
        if (adaptationFieldHeader != null) {
            return ("Adaptation field: \n\n" +
                    "Adaptation field length: " + adaptationFieldHeader.getAdaptationFieldLength() + " Bytes\n" +
                    "Discontinuity indicator: " + adaptationFieldHeader.getDI() + (adaptationFieldHeader.getDI()==0x1 ? " (Discontinuity state)" : " (Continuous state)") + "\n" +
                    "Random access indicator: " + adaptationFieldHeader.getRAI() + (adaptationFieldHeader.getRAI()==0x1 ? " (Discontinuity state)" : " (Continuous state)") + "\n" +
                    "Elementary stream priority indicator: " + adaptationFieldHeader.getESPI() + (adaptationFieldHeader.getESPI()==0x1 ? " (Random access info is present)" : " (No random access)") + "\n" +
                    "PCR flag: " + adaptationFieldHeader.getPCRF() + (adaptationFieldHeader.getPCRF()==0x1 ? " (Program Clock Refercence present)" : " (Not present)") + "\n" +
                    "OPCR flag: " + adaptationFieldHeader.getOPCRF() + (adaptationFieldHeader.getOPCRF()==0x1 ? " (Original Program Clock Refercence present)" : " (Not present)") + "\n" +
                    "Splicing point flag: " + adaptationFieldHeader.getSplicingPointFlag() + (adaptationFieldHeader.getSplicingPointFlag()==0x1 ? " (Splicing point present)" : " (Not present)") + "\n" +
                    "Transport private data flag: " + adaptationFieldHeader.getTPDflag() + (adaptationFieldHeader.getTPDflag()==0x1 ? " (Private data present)" : " (Not present)") + "\n" +
                    "Adaptation field extension flag: " + adaptationFieldHeader.getAFEflag() + (adaptationFieldHeader.getAFEflag()==0x1 ? " (Extension present)" : " (Not present)") + "\n" +
                    createAdaptationOtionalFieldsOutput(adaptationFieldHeader, adaptationFieldHeader.getOptionalFields()) + "\n\n"
            );
        }
        return "";
    }


    private String createAdaptationOtionalFieldsOutput(AdaptationFieldHeader adaptationField, AdaptationFieldOptionalFields optionalFields) {
        if (optionalFields != null) {
            StringBuilder timestampBuilder = new StringBuilder();

            if(adaptationField.getPCRF()  == 0x1){
                timestampBuilder.append("PCR: " + String.format("0x%09X", adaptationField.getOptionalFields().getPCRtimestamp() & 0xFFFFFFFF)  + optionalFields.parseTimestamp(adaptationField.getOptionalFields().getPCRtimestamp()) + "\n");
            }
            if(adaptationField.getOPCRF() == 0x1) {
                timestampBuilder.append("OPCR: " + String.format("0x%09X", adaptationField.getOptionalFields().getOPCRtimestamp() & 0xFFFFFFFF)   + optionalFields.parseTimestamp(adaptationField.getOptionalFields().getOPCRtimestamp()) + "\n");
            }
            return ( timestampBuilder.toString() +
                    (adaptationField.getSplicingPointFlag() == 0x1 ? "Splicing point: " + String.format("0x%06X", optionalFields.getSpliceCoutdown() & 0xFFFFF) + "\n" : "") +
                    (adaptationField.getTPDflag() == 0x1 ? "TPD length: " + (optionalFields.getTPDlength() & 0xFFFFF) + " Bytes\n" : "") +
                    (adaptationField.getTPDflag() == 0x1 ? "Transport private data:\n" + createHexOutput(getHexSequence(optionalFields.getTPD())) + "\n" : "")
                    //TODO correct transport private hex output
                    //  (adaptationField.getAFEflag() == 0x1 ? "Additional copy info: " + String.format("0x%01X", optionalFields.getAFEFlength() & 0xFFFFF) + "\n" : "")
                    //  (adaptationField.getLTWF() == 0x1 ? "PES CRC: " + String.format("0x%02X", optionalFields.getLTW() & 0xFFFFF) + "\n" : "") +
                    //  (adaptationField.getPRF() == 0x1 ? "PES CRC: " + String.format("0x%02X", optionalFields.getPiecewise_rate() & 0xFFFFF) + "\n" : "") +
                    //  (adaptationField.getSSF() == 0x1 ? "PES CRC: " + String.format("0x%02X", optionalFields.getSeamless_splice() & 0xFFFFF) + "\n" : "")
            );
        }
        return "";
    }


    private String toHex(int pid) {
        return String.format("0x%04X", pid & 0xFFFFF);
    }


    private String createASCIIoutput(byte[] data, int pid){
        if(pid == EIT_STpid){
            StringBuilder stringBuilder = new StringBuilder("\n\nASCII data: \n\n");
            try {
                int index = 0;
                String asciiSequence = new String(data,  "Cp1250" );

                for(char c : asciiSequence.toCharArray()) {
                    stringBuilder.append(c);
                    if (++index % asciiLineSize == 0) {
                        stringBuilder.append("\n");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                stringBuilder.append(new String(e.getMessage() + e.getStackTrace()));
            }
            return stringBuilder.toString();
        }
        return "";
    }


    private String getHexSequence(byte[] data) {
        return new BigInteger(data).toString(16);
    }


    private static String createHexOutput(String hexSequence) {

        StringBuilder hexBuilder = new StringBuilder();

        int index = 0;
        for (char c : hexSequence.toCharArray()) {
            hexBuilder.append(Character.toUpperCase(c));
            if (++index % bytePair == 0) {
                hexBuilder.append(" ");
            }
            if (index % hexLine == 0) {
                hexBuilder.append("\n");
            }
        }
        return  hexBuilder.toString();
    }


    private static String createDataOutput(String hexSequence) {

        StringBuilder hexBuilder = new StringBuilder();
        hexBuilder.append(String.format("0x%06X   ", 0 & 0xFFFFF));

        int index = 0;
        for (char c : hexSequence.toCharArray()) {
            hexBuilder.append(Character.toUpperCase(c));
            if (++index % bytePair == 0) {
                hexBuilder.append(" ");
            }
            if (index % hexLine == 0) {
                hexBuilder.append("\n");
                hexBuilder.append(String.format("0x%06X   ", (index / 2) & 0xFFFFF));
            }
        }
        return ( "Data: \n           0001 0203 0405 0607 0809 0A0B 0C0D 0E0F\n\n" + hexBuilder.toString() + "\n" );
    }


    void hideTooltip() {
        if (this.isShowing()) {
            this.hide();
        }
    }

    public void setPackets(ArrayList<TSpacket> packets) {
        this.packets = packets;
    }

    public void setStream(Stream stream) {
        this.stream = stream;
    }
}
