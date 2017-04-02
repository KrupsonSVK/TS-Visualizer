package model.config;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

import static model.config.dvb.*;


public class config {

    public final static double packetImageWidth = 100;
    public final static double packetImageHeight = 60;
    public final static double miniPacketImageSize = 10;
    public final static double typeIconSize = 19;
    public final static double specialIconSize = 16;
    public final static double legendPaneMoveCoeff = packetImageWidth / miniPacketImageSize;
    public final static double packetScrollPaneHeightRatio = 0.54;
    public final static double barScrollPaneHeigthRatio = 0.06;
    public final static double legendScrollPaneHeightRatio = 0.30;
    public final static double windowWidth = 960;
    public final static double windowHeigth = 720;

    public final static double packetScrollPaneHeight = windowHeigth * packetScrollPaneHeightRatio;
    public final static double barScrollPaneHeight = windowHeigth * barScrollPaneHeigthRatio;
    public final static double legendScrollPaneHeight = windowHeigth * legendScrollPaneHeightRatio;;
    public final static double barHeight = windowHeigth * barScrollPaneHeigthRatio;

    public final static double inset = 5;
    public final static double mouseSensitivityVertical = 1.; // 2.5;
    public final static double labelWidth = 135;
    public final static double fontSize = 8.5;
    public final static int offsetLP = 2;
    public final static int asciiLineSize = 50;
    public static final int bytePair = 4;
    public static final int hexLine = 32;

    public final static double secondaryFrameSize = 1.25;
    public final static double primaryFrameSize = 2.5;

    public final static Color defaultColor = Color.rgb(240,240,240);
    public final static String resourcesPath = "/resources/";
    public final static Color adaptationFieldColor = Color.BLACK;
    public final static Color payloadStartColor = Color.RED;

    public static Map  packetImages = new ImageHashMap<Integer,Image>(new Image(resourcesPath + "grey.png")){
            {
                put(PATpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(PATpid))));
                put(CATpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(CATpid))));
                put(TDSTpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(TDSTpid))));
                put(NIT_STpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(NIT_STpid))));
                put(SDT_BAT_STpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(SDT_BAT_STpid))));
                put(EIT_STpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(EIT_STpid))));
                put(RST_STpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(RST_STpid))));
                put(TDT_TOT_STpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(TDT_TOT_STpid))));
                put(netSyncPid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(netSyncPid))));
                put(DITpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(DITpid))));
                put(SITpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(SITpid))));
                put(PMTpid , new Image(getClass().getResourceAsStream(resourcesPath + getPacketImageName(PMTpid))));
            }
        };

    public static Map typeIcons = new ImageHashMap<Integer,Image>(new Image(resourcesPath + "dvb.png")){
            {
                put(PSItype, new Image(getClass().getResourceAsStream(resourcesPath + "psi.png")));
                put(videoType, new Image(getClass().getResourceAsStream(resourcesPath + "video.png")));
                put(audioType, new Image(getClass().getResourceAsStream(resourcesPath + "audio.png")));
                put(CAStype, new Image(getClass().getResourceAsStream(resourcesPath + "cas.png")));
                put(PSMtype, new Image(getClass().getResourceAsStream(resourcesPath + "map.png")));
                put(PSMtype, new Image(getClass().getResourceAsStream(resourcesPath + "map.png")));
                put(MHEGtype, new Image(getClass().getResourceAsStream(resourcesPath + "map.png")));
                put(adaptationFieldIcon, new Image(getClass().getResourceAsStream(resourcesPath + "adaptation.png")));
                put(PESheaderIcon, new Image(getClass().getResourceAsStream(resourcesPath + "pesheader.png")));
                put(privateType, new Image(getClass().getResourceAsStream(resourcesPath + "private.png")));
                put(nullPacket, new Image(getClass().getResourceAsStream(resourcesPath + "null.png")));
                put(defaultType, new Image(getClass().getResourceAsStream(resourcesPath + "default.png")));
            }
        };


    public static  String getPacketImageName(int type){
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
            case PMTpid : return "black.png";
            default: return "grey.png";
        }
    }


    public static  Color getPacketColor(int type){
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
            case PMTpid : return Color.DARKBLUE;
            default: return Color.GREY;
        }
    }


    public static final String userGuideText = "First choose a file to analyse by clicking \"Select file..\" button or drag'n'drop a file to the window." +
            "An error can occure if the file you choose is invalid, unaccessible or does not contain valid TS packets. " +
            "Choose from three tab to display: for detailed specifications of the stream click \"Details\" tab, for graphical " +
            "visualization of the packet distribution in the stream click \"Visualization\" tab and for bitrate chart of " +
            "programmes choose \"Graph\" tab.\n\nVisualization tab:\n To move the packet panes drag them with mouse. To display packet details click " +
            "on it with left mouse. To zoom the packet pane use zoom bar. To apply program filter of packets choose available " +
            "option in combobox. To move packet panes by packet bar drag the looking glass with mouse. To apply PSI filter of " +
            "packet bar click on it and select available option.";


    public static class ImageHashMap<K,V> extends HashMap<K,V> {
        protected V defaultValue;

        public ImageHashMap(V defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public V get(Object k) {
            return containsKey(k) ? super.get(k) : defaultValue;
        }
    }

}
