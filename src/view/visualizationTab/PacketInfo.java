package view.visualizationTab;


import model.Config;
import javafx.scene.control.Tooltip;
import model.AdaptationFieldHeader;
import model.AdaptationFieldOptionalFields;
import model.Payload;
import model.TSpacket;
import model.pes.PES;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;

import static model.Config.EIT_STpid;

public class PacketInfo extends Tooltip {

    private ArrayList<TSpacket> packets;
    private Config config;


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

        return (
                createHeaderOutput(packet) +
                        createAdaptationFieldHeaderOutput(packet.getAdaptationFieldHeader()) +
                        createPESheaderOutput(packet.getPayload()) +
                        createDataOutput(getHexSequence(packet.getData())) +
                        createASCIIoutput(packet.getData(), packet.getPID()) + "\n"
        );
    }


    private String createHeaderOutput(TSpacket packet) {
        return (
                "Header: \n\n" +
                        "Packet PID: " + String.format("0x%04X", packet.getPID() & 0xFFFFF) + " (" + packet.getPID() + ")\n" +
                        "Transport Error Indicator: " + packet.getTransportErrorIndicator() + (packet.getTransportErrorIndicator() == 0 ? " (No error)" : " (Error packet)") + "\n" +
                        "Payload Start Indicator: " + packet.getPayloadStartIndicator() + (packet.getPayloadStartIndicator() == 0 ? " (Normal payload)" : " (Payload start)") + "\n" +
                        "Transport priority: " + packet.getTransportPriority() + (packet.getTransportPriority() == 0 ? " (Normal priority)" : " (High priority)") + "\n" +
                        "Transport Scrambling Control: " + packet.getTransportScramblingControl() + (packet.getTransportScramblingControl() == 0 ? " (Not scrambled)" : " (Scrambled)") + "\n" +
                        "Continuity Counter: " + String.format("0x%01X", packet.getContinuityCounter() & 0xFFFFF) + " (" + packet.getContinuityCounter() + ")" + "\n" +
                        "Adaptation Field Control: " + packet.getAdaptationFieldControl() + (packet.getAdaptationFieldControl() == 1 ?
                        " (Payload only)" : (packet.getAdaptationFieldControl() == 2 ? " (Adaptation field only)" : " (Adaptation field followed by payload)")) + "\n\n\n"
        );
    }


    private String createPESheaderOutput(Payload payload) {
        if (payload != null) {
            if (payload.hasPESheader()) {
                PES pesPacket = (PES) payload;
                return (
                        "PES header: \n\n" +
                                "Stream ID: " + String.format("0x%04X", pesPacket.getStreamID() & 0xFFFFF) +  " (" + pesPacket.getStreamID() + ") = " + config.getStreamDescription(pesPacket.getStreamID()) + "\n" +
                                "PES packet length: " + pesPacket.getPESpacketLength() + " Bytes \n" +
                                "PES scrambling control: " + pesPacket.getPESscramblingControl() + (pesPacket.getPESscramblingControl()==0 ? " (Not scrambled)" : " (Scrambled)") + "\n" +
                                "PES priority: " + pesPacket.getPESpriority() + (pesPacket.getPESpriority()==0 ? " (Normal priority)" : " (High priority)") + "\n" +
                                "Data alignment indicator: " + pesPacket.getDataAlignmentIndicator() + (pesPacket.getDataAlignmentIndicator()==1 ? " (PES packet header is immediately followed \n" + "by the video start code or audio syncword \n" + "indicated in the data_stream_alignment_descriptor)" : " (Normal payload)") + "\n" +
                                "Copyright: " + pesPacket.getCopyright() + (pesPacket.getCopyright()==1 ? " (Content copyrighted)" : " (Copyright not defined)") + "\n" +
                                "Original or copy: " + pesPacket.getOriginalOrCopy() + (pesPacket.getOriginalOrCopy()==1 ? " (Original)" : " (Copy)") + "\n" +
                                "PTS DTS flags: " + pesPacket.getPTSdtsFlags() + (pesPacket.getPTSdtsFlags()==3 ? " (PTS and DTS present)" : (pesPacket.getPTSdtsFlags()==2 ? " (PTS present)" : " (PTS and DTS not present)")) + "\n" +
                                "ES rate flag: " + pesPacket.getESrateFlag() + (pesPacket.getESrateFlag()==1 ? " (Present)" : " (Not present)") + "\n" +
                                "DSM trick mode flag: " + pesPacket.getDSMtrickModeFlag()  + (pesPacket.getDSMtrickModeFlag()==1 ? " (Present)" : " (Not present)") + "\n" +
                                "Additional copy info flag: " + pesPacket.getAdditionalCopyInfoFlag() + (pesPacket.getAdditionalCopyInfoFlag()==1 ? " (Present)" : " (Not present)") + "\n" +
                                "PES CRC flag: " + pesPacket.getPEScrcFlag() + (pesPacket.getPEScrcFlag()==1 ? " (Present)" : " (Not present)") + "\n" +
                                "PES extension flag: " + pesPacket.getPESextensionFlag() + (pesPacket.getPESextensionFlag()==1 ? " (Present)" : " (Not present)") + "\n" +
                                "PES header data length: " + pesPacket.getPESheaderDataLength() + " Bytes\n" +
                                createPESoptionalFieldsOutput(pesPacket) + "\n\n"
                );
            }
        }
        return "";
    }


    private String createPESoptionalFieldsOutput(PES optionalPESheader) {
        return (
                (optionalPESheader.getPTSdtsFlags() >= 2 ? "PTS: " + getStingDate(optionalPESheader.getPTS()) + "\n" : "" ) +
                        (optionalPESheader.getPTSdtsFlags() == 3 ? "DTS:" + getStingDate(optionalPESheader.getPTS()) + "\n" : "" ) +
                        (optionalPESheader.getESCRflag() == 1 ? "ESCR: " + String.format("0x%06X", optionalPESheader.getESCR() & 0xFFFFF) + "\n" : "" ) +
                        (optionalPESheader.getESrateFlag() == 1 ? "ES rate: " + String.format("0x%03X", optionalPESheader.getESrate() & 0xFFFFF) + "\n" : "" ) +
                        (optionalPESheader.getDSMtrickModeFlag() == 1 ? "DSM trick mode: " + String.format("0x%01X", optionalPESheader.getDSMtrickModeFlag() & 0xFFFFF) + "\n" : "" ) +
                        (optionalPESheader.getAdditionalCopyInfoFlag() == 1 ? "Additional copy info: " + String.format("0x%01X", optionalPESheader.getAdditionalCopyInfoFlag() & 0xFFFFF) + "\n" : "" ) +
                        (optionalPESheader.getPEScrcFlag() == 1 ? "PES CRC: " + String.format("0x%02X", optionalPESheader.getPEScrcFlag() & 0xFFFFF) + "\n" : "" )
        );
    }


    private String createAdaptationFieldHeaderOutput(AdaptationFieldHeader adaptationFieldHeader) {
        if (adaptationFieldHeader != null) {
            return (
                    "Adaptation field: \n\n" +
                            "Adaptation field length: " + adaptationFieldHeader.getAdaptationFieldLength() + " Bytes\n" +
                            "Discontinuity indicator: " + adaptationFieldHeader.getDI() + (adaptationFieldHeader.getDI()==1 ? " (Discontinuity state)" : " (Continuous state)") + "\n" +
                            "Random access indicator: " + adaptationFieldHeader.getRAI() + (adaptationFieldHeader.getRAI()==1 ? " (Discontinuity state)" : " (Continuous state)") + "\n" +
                            "Elementary stream priority indicator: " + adaptationFieldHeader.getESPI() + (adaptationFieldHeader.getESPI()==1 ? " (Random access info is present)" : " (No random access)") + "\n" +
                            "PCR flag: " + adaptationFieldHeader.getPCRF() + (adaptationFieldHeader.getPCRF()==1 ? " (Program Clock Refercence present)" : " (Not present)") + "\n" +
                            "OPCR flag: " + adaptationFieldHeader.getOPCRF() + (adaptationFieldHeader.getOPCRF()==1 ? " (Original Program Clock Refercence present)" : " (Not present)") + "\n" +
                            "Splicing point flag: " + adaptationFieldHeader.getSplicingPointFlag() + (adaptationFieldHeader.getSplicingPointFlag()==1 ? " (Splicing point present)" : " (Not present)") + "\n" +
                            "Transport private data flag: " + adaptationFieldHeader.getTPDflag() + (adaptationFieldHeader.getTPDflag()==1 ? " (Private data present)" : " (Not present)") + "\n" +
                            "Adaptation field extension flag: " + adaptationFieldHeader.getAFEflag() + (adaptationFieldHeader.getAFEflag()==1 ? " (Extension present)" : " (Not present)") + "\n" +
                            createAdaptationOtionalFieldsOutput(adaptationFieldHeader, adaptationFieldHeader.getOptionalFields()) + "\n\n"
            );
        }
        return "";
    }


    private String createAdaptationOtionalFieldsOutput(AdaptationFieldHeader adaptationField, AdaptationFieldOptionalFields optionalFields) {
        if (optionalFields != null) {
            return (
                    (adaptationField.getPCRF() == 1 ? "PCR: " + getStingDate(optionalFields.getPCR()) + "\n" : "") +
                            (adaptationField.getOPCRF() == 1 ? "OPCR:" + getStingDate(optionalFields.getOPCR()) + "\n" : "") +
                            (adaptationField.getSplicingPointFlag() == 1 ? "Splicing point: " + String.format("0x%06X", optionalFields.getSpliceCoutdown() & 0xFFFFF) + "\n" : "") +
                            (adaptationField.getTPDflag() == 1 ? "TPD length: " + (optionalFields.getTPDlength() & 0xFFFFF) + " Bytes\n" : "") +
                            (adaptationField.getTPDflag() == 1 ? "Transport private data:\n" + createHexOutput(getHexSequence(optionalFields.getTPD())) + "\n" : "")
                    //  (adaptationField.getAFEflag() == 1 ? "Additional copy info: " + String.format("0x%01X", optionalFields.getAFEFlength() & 0xFFFFF) + "\n" : "")
                    //  (adaptationField.getLTWF() == 1 ? "PES CRC: " + String.format("0x%02X", optionalFields.getLTW() & 0xFFFFF) + "\n" : "") +
                    //  (adaptationField.getPRF() == 1 ? "PES CRC: " + String.format("0x%02X", optionalFields.getPiecewise_rate() & 0xFFFFF) + "\n" : "") +
                    //  (adaptationField.getSSF() == 1 ? "PES CRC: " + String.format("0x%02X", optionalFields.getSeamless_splice() & 0xFFFFF) + "\n" : "")
            );
        }
        return "";
    }


    private String createASCIIoutput(byte[] data, int pid){
        if(pid == EIT_STpid){
            StringBuilder stringBuilder = new StringBuilder("\n\nASCII data: \n\n");
            try {
                int index = 0;
                String asciiSequence = new String(data,  "Cp1250" );

                for(char c : asciiSequence.toCharArray()) {
                    stringBuilder.append(c);
                    if (++index % 50 == 0) {
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
            if (++index % 4 == 0) {
                hexBuilder.append(" ");
            }
            if (index % 32 == 0) {
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
            if (++index % 4 == 0) {
                hexBuilder.append(" ");
            }
            if (index % 32 == 0) {
                hexBuilder.append("\n");
                hexBuilder.append(String.format("0x%06X   ", (index / 2) & 0xFFFFF));
            }
        }
        return ( "Data: \n           0001 0203 0405 0607 0809 0A0B 0C0D 0E0F\n\n" + hexBuilder.toString() + "\n" );
    }


    private long midBits(long k, int m, int n){
        return (k >> m) & ((1 << (n-m))-1);
    }


    public String getStingDate(long pts_dts){

        long timestamp = (midBits(pts_dts,17,35) << 15) | midBits(pts_dts,1,16);

        double milliseconds = timestamp / 90.;
        double seconds = (milliseconds / 1000.) % 60.;
        long minutes = ((long)milliseconds / (1000 * 60)) % 60;
        long hours = ((long)milliseconds / (1000 * 60 * 60)) % 24;

        return String.format("0x%05X (%02d:%02d:%06.3f) ", timestamp, hours, minutes, seconds, milliseconds);
    }


    void hideTooltip() {
        if (this.isShowing()) {
            this.hide();
        }
    }


    public void setPackets(ArrayList<TSpacket> packets) {
        this.packets = packets;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
