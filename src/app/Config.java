package app;

import javafx.scene.paint.Color;
import model.Stream;

import java.util.Map;

public class Config {

    public final static int tsPacketSize = 188;
    public final static int tsHeaderSize = 4;
    public final static int tsHeaderBitLength = tsHeaderSize * 8;
    public final static int tsAdaptationFieldHeaderSize = 2;
    public final static int tsAdaptationFieldHeaderBitLength = tsAdaptationFieldHeaderSize * 8;

    public final static int tsPayloadLength = tsPacketSize - tsHeaderSize;
    public final static int syncByte = 0x47;
    public final static int syncByteSize = 1;

    public final static int adaptationFieldOnly = 2;
    public final static int adaptationFieldAndPayload = 3;

    public final static int PSImaxPID = 0x001F;

    public final static int nil = -1 ;
    public final static int PSIcommonFieldsLength = 24;
    public final static int tableIDlength = 8;
    public final static int programNumberLength = 16;
    public final static int sectionLengthLength = 12;
    public final static int versionNumLength = 5;
    public final static int sectionNumLength = 8;
    public final static int tsIDlength = 16;
    public final static int CRClength = 32;
    public final static int AFLlength = 1;

    public final static int mandatoryPATfields = 72;

    public final static int PCR_PIDlength = 13;
    public final static int programInfoLengthLength = 12;
    public final static int streamTypeLength = 8;
    public final static int elementaryPIDlength = 13;
    public final static int ESinfoLengthLength = 12;

    public static final int PATpid = 0x00;
    public static final int CATpid = 0x01;
    public static final int TDSTpid = 0x02;
    public static final int NIT_STpid = 0x10;
    public static final int SDT_BAT_STpid = 0x11;
    public static final int EIT_STpid = 0x12;
    public static final int RST_STpid = 0x13;
    public static final int TDT_TOT_STpid = 0x14;
    public static final int netSyncPid = 0x15;
    public static final int DITpid = 0x1E;
    public static final int SITpid = 0x1F;

    public static final int PAStableID = 0x00;
    public static final int CAStableID = 0x01;
    public static final int PMStableID = 0x02;
    public static final int TSDStableID = 0x3;
    public static final int NISactualTableID = 0x40;
    public static final int NISotherTableID = 0x41;
    public static final int SDSactualTableID = 0x42;
    public static final int SDSotherTableID = 0x46;
    public static final int BAStableID = 0x4A;
    public static final int EISactualPresentTableID = 0x4E;
    public static final int EISotherPresentTableID = 0x4F;
    public static final int TDStableID = 0x70;
    public static final int RSStableID = 0x72;
    public static final int TOStableID = 0x73;
    public static final int DIStableID = 0x7E;
    public static final int SIStableID = 0x7F;

    public static final int intBitLength = 32;
    public static final int byteBitLength = 8;


    public Color getPacketColor(int type){
        switch(type){
            case PATpid : return Color.RED;
            case CATpid : return Color.LIGHTGREEN;
            case TDSTpid : return Color.YELLOW;
            case NIT_STpid : return Color.BLUE;
            case SDT_BAT_STpid : return Color.BROWN;
            case EIT_STpid : return Color.ORANGE;
            case RST_STpid : return Color.LIGHTBLUE;
            case TDT_TOT_STpid : return Color.DARKGREEN;
            case netSyncPid : return Color.VIOLET;
            case DITpid : return Color.PINK;
            case SITpid : return Color.DARKBLUE;
            default: return Color.GRAY;
        }
    }

    public String getPacketImageName(int type){
        switch(type){
            case PATpid : return "red.png";
            case CATpid : return "lightgreen.png";
            case TDSTpid : return "yellow.png";
            case NIT_STpid : return "blue.png";
            case SDT_BAT_STpid : return "brown.png";
            case EIT_STpid : return "orange.png";
            case RST_STpid : return "lightblue.png";
            case TDT_TOT_STpid : return "darkgreen.png";
            case netSyncPid : return "violet.png";
            case DITpid : return "pink.png";
            case SITpid : return "darkblue.png";
            default: return "gray.png";
        }
    }

    public String getPacketImageName(Color color) {
        if (color == Color.RED) return "red.png";
        if (color == Color.LIGHTGREEN) return "lightgreen.png";
        if (color == Color.YELLOW) return "yellow.png";
        if (color == Color.BLUE) return "blue.png";
        if (color == Color.BROWN) return "brown.png";
        if (color == Color.ORANGE) return "orange.png";
        if (color == Color.LIGHTBLUE) return "lightblue.png";
        if (color == Color.DARKGREEN) return "darkgreen.png";
        if (color == Color.VIOLET) return "violet.png";
        if (color == Color.PINK) return "pink.png";
        if (color == Color.DARKBLUE) return "darkblue.png";
        return "gray.png";
    }

    public String getPacketName(int pid) {
        switch(pid){
            case PATpid : return "PAT";
            case CATpid : return "CAT";
            case TDSTpid : return "TDST";
            case NIT_STpid : return "NIT";
            case SDT_BAT_STpid : return "SDT or BAT";
            case EIT_STpid : return "EIT";
            case RST_STpid : return "RST";
            case TDT_TOT_STpid : return "TDT";
            case netSyncPid : return "NetSync";
            case DITpid : return "DIT";
            case SITpid : return "SIT";
            default: return "PES";
        }
    }

    public String getProgramName(Stream stream, int pid) {
        Map map = stream.getPrograms();
        Object obj = map.get(pid);
        return obj == null ? "" : obj.toString();
    }

    public boolean isPSI(int pid) {
        return getPacketName(pid) != "PES";
    }

    public static final String userGuideText = "First choose a file to analyse by clicking \"Select file..\" button or drag'n'drop a file to the window." +
            "An error can occure if the file you choose is invalid, unaccessible or does not contain valid TS packets. " +
            "Choose from three tab to display: for detailed specifications of the stream click \"Details\" tab, for graphical " +
            "visualization of the packet distribution in the stream click \"Visualization\" tab and for bitrate chart of " +
            "programmes choose \"Graph\" tab.\n\nVisualization tab:\n To move the packet panes drag them with mouse. To display packet details click " +
            "on it with left mouse. To zoom the packet pane use zoom bar. To apply program filter of packets choose available " +
            "option in combobox. To move packet panes by packet bar drag the looking glass with mouse. To apply PSI filter of " +
            "packet bar click on it and select available option.";
}