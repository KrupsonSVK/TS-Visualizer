package model.config;

import app.Main;
import app.XML;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

import static model.config.MPEG.*;


public class Config {

    public static final String version = " v.2017.5";
    public static final String releaseDate = "08.05.2017";
    public static final String email = "xkrupat@stuba.sk";

    public final static double fontSize = 8.5;
    public final static double labelFontSize = 12.;

    public final static int kiloBit = 1024;
    public final static int MegaBit = kiloBit*1024;
    public final static int GigaBit = MegaBit*1024;

    public final static int kiloByte = byteBinaryLength*kiloBit;
    public final static int MegaByte = byteBinaryLength*MegaBit;
    public final static int GigaByte = byteBinaryLength*GigaBit;

    public final static int tickUnit = 10;
    public final static int snapshotInterval = 100;
    public final static int packetInfoMaxTextLength = 100;

    public final static double packetImageWidth = 100;
    public final static double packetImageHeight = 60;
    public final static double miniPacketImageSize = 10;
    public final static double typeIconSize = 19;
    public final static double specialIconSize = 16;

    public final static int packetDisplayOffset = (int) (packetImageWidth / 2);
    public final static double legendPaneMoveCoeff = packetImageWidth / miniPacketImageSize;

    public final static double packetScrollPaneHeightRatio = 0.54;
    public final static double barScrollPaneHeigthRatio = 0.06;
    public final static double legendScrollPaneHeightRatio = 0.30;

    public final static double windowWidth = 960;
    public final static double windowHeight = 720;
    public final static double aboutWidth = 400;
    public final static double aboutHeight = 300;
    public final static double userGuideWidth = 600;
    public final static double userGuideHeight = 450;
    public final static double dialogStageWidth = 250;
    public final static double dialogStageHeight = 100;

    private final static String rootPath = "src";
    private final static String resourcesPath = "/resources";
    private final static String imagesPath = resourcesPath + "/images/";
    private final static String localePath = resourcesPath + "/locale/";

    public final static String localeEN = localePath + "EN.xml";
    public final static String localeDE = localePath + "DE.xml";
    public final static String localeSK = localePath + "SK.xml";
    public final static String localeRU = localePath + "RU.xml";

    public static Localization localizationEN = readLocale(localeEN);
    public static Localization localizationDE = readLocale(localeDE);
    public static Localization localizationSK = readLocale(localeSK);
    public static Localization localizationRU = readLocale(localeRU);

    public final static String errorTitle = "Error occured!"; //TODO move to localization
    private final static String dragNdrop = "dragndrop.png";
    public final static String windowStyle = "-fx-background-image: url('" + Main.class.getResource(imagesPath + dragNdrop ).toExternalForm() + "'); " + "-fx-background-position: center center; " + "-fx-background-repeat: stretch;";
    public final static String afterWindowStyle ="-fx-background-color: transparent";

    public final static Insets windowInsets =new Insets(25, 25, 25, 25);
    public final static Insets chartHBoxInsets = new Insets(10,10,10,10);
    public final static Insets chartInsets = new Insets(10,40,10,40);
    public final static Insets textInsets = new Insets(10,10,10,10);
    public final static Insets labelInsets = new Insets(20,10,10,10);
    public final static Insets vBoxInsets = new Insets(5);
    public final static Insets hBoxInsets = new Insets(5,5,5,5);
    public final static Insets dialogInsets = new Insets(10);

    public final static int chartHBoxSpacing = 10;
    public final static int tickLabelRotation = 0;
    public final static int windowGripGap = 0;

    public final static double packetScrollPaneHeight = windowHeight * packetScrollPaneHeightRatio;
    public final static double barScrollPaneHeight = windowHeight * barScrollPaneHeigthRatio;
    public final static double legendScrollPaneHeight = windowHeight * legendScrollPaneHeightRatio;;
    public final static double barHeight = windowHeight * barScrollPaneHeigthRatio;

    public final static double visualizationTabInsets = 5;
    public final static double mouseSensitivityVertical = 1.; // 2.5;
    public final static double labelWidth = 150;
    public final static double textAreaMinHeigth = 370;

    public final static int offsetMiniPacket = 2;
    public final static int asciiLineSize = 50;
    public final static int bytePair = 4;
    public final static int hexLine = 32;

    public final static double secondaryFrameSize = 1.25;
    public final static double primaryFrameSize = 2.50;
    public final static double ovalSize = 2.50;

    public final static Color defaultColor = Color.rgb(240,240,240);
    public final static Color adaptationFieldColor = Color.BLACK;
    public final static Color hasPESheaderColor = Color.RED;


    public static Map  packetImages = new ImageHashMap<Integer,Image>(new Image(imagesPath + "grey.png")){
            {
                put(PATpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(PATpid))));
                put(CATpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(CATpid))));
                put(TDSTpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(TDSTpid))));
                put(NIT_STpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(NIT_STpid))));
                put(SDT_BAT_STpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(SDT_BAT_STpid))));
                put(EIT_STpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(EIT_STpid))));
                put(RST_STpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(RST_STpid))));
                put(TDT_TOT_STpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(TDT_TOT_STpid))));
                put(netSyncPid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(netSyncPid))));
                put(DITpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(DITpid))));
                put(SITpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(SITpid))));
                put(PMTpid , new Image(getClass().getResourceAsStream(imagesPath + getPacketImageName(PMTpid))));
            }
        };

    public static Map typeIcons = new ImageHashMap<Integer,Image>(new Image(imagesPath + "dvb.png")){
            {
                put(PSItype, new Image(getClass().getResourceAsStream(imagesPath + "psi.png")));
                put(videoType, new Image(getClass().getResourceAsStream(imagesPath + "video.png")));
                put(audioType, new Image(getClass().getResourceAsStream(imagesPath + "audio.png")));
                put(CAStype, new Image(getClass().getResourceAsStream(imagesPath + "cas.png")));
                put(PSMtype, new Image(getClass().getResourceAsStream(imagesPath + "map.png")));
                put(PSMtype, new Image(getClass().getResourceAsStream(imagesPath + "mheg.png")));
                put(MHEGtype, new Image(getClass().getResourceAsStream(imagesPath + "map.png")));
                put(adaptationFieldIcon, new Image(getClass().getResourceAsStream(imagesPath + "adaptation.png")));
                put(PESheaderIcon, new Image(getClass().getResourceAsStream(imagesPath + "pesheader.png")));
                put(payloadStartIcon, new Image(getClass().getResourceAsStream(imagesPath + "payloadStart.png")));
                put(timestampIcon, new Image(getClass().getResourceAsStream(imagesPath + "timestamp.png")));
                put(PMTicon, new Image(getClass().getResourceAsStream(imagesPath + "pmt.png")));
                put(DVBicon, new Image(getClass().getResourceAsStream(imagesPath + "dvb.png")));
                put(privateType, new Image(getClass().getResourceAsStream(imagesPath + "private.png")));
                put(nullPacketPID, new Image(getClass().getResourceAsStream(imagesPath + "null.png")));
                put(defaultType, new Image(getClass().getResourceAsStream(imagesPath + "default.png")));
            }
        };


    public static  String getPacketImageName(int type){
        switch(type){
            case PATpid :
                return "red.png";
            case CATpid :
                return "lightgreen.png";
            case TDSTpid :
                return "yellow.png";
            case NIT_STpid :
                return "blue.png";
            case SDT_BAT_STpid :
                return "brown.png";
            case EIT_STpid :
                return "orange.png";
            case RST_STpid :
                return "lightblue.png";
            case TDT_TOT_STpid :
                return "darkgreen.png";
            case netSyncPid :
                return "violet.png";
            case DITpid :
                return "pink.png";
            case SITpid :
                return "darkblue.png";
            case PMTpid :
                return "red.png";
            case RNTpid :
                return "darkblue.png";
            case bandSignallingPID :
                return "darkblue.png";
            case measurementPID :
                return "darkblue.png";
            case nullPacketPID :
                return "black.png";
            default: return "grey.png";
        }
    }


    public static  Color getPacketColor(int type){
        switch(type){
            case PATpid :
                return Color.RED;
            case CATpid :
                return Color.LIGHTGREEN;
            case TDSTpid :
                return Color.YELLOW;
            case NIT_STpid :
                return Color.BLUE;
            case SDT_BAT_STpid :
                return Color.BROWN;
            case EIT_STpid :
                return Color.ORANGE;
            case RST_STpid :
                return Color.LIGHTBLUE;
            case TDT_TOT_STpid :
                return Color.DARKGREEN;
            case netSyncPid:
                return Color.VIOLET;
            case DITpid :
                return Color.PINK;
            case SITpid :
                return Color.DARKBLUE;
            case PMTpid :
                return Color.LIGHTGREEN;
            case RNTpid :
                return Color.DARKBLUE;
            case bandSignallingPID :
                return Color.DARKBLUE;
            case measurementPID :
                return Color.DARKBLUE;
            case nullPacketPID :
                return Color.BLACK;
            default:
                return Color.GREY;
        }
    }


    private static Localization readLocale(String name) {
        try {
            return (Localization) XML.read(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


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

    public static final String logoText = (     "████████╗███████╗    ██╗   ██╗██╗███████╗██╗   ██╗ █████╗ ██╗     ██╗███████╗███████╗██████╗ \n" +
                                                "╚══██╔══╝██╔════╝    ██║   ██║██║██╔════╝██║   ██║██╔══██╗██║     ██║╚══███╔╝██╔════╝██╔══██╗\n" +
                                                "   ██║   ███████╗    ██║   ██║██║███████╗██║   ██║███████║██║     ██║  ███╔╝ █████╗  ██████╔╝\n" +
                                                "   ██║   ╚════██║    ╚██╗ ██╔╝██║╚════██║██║   ██║██╔══██║██║     ██║ ███╔╝  ██╔══╝  ██╔══██╗\n" +
                                                "   ██║   ███████║     ╚████╔╝ ██║███████║╚██████╔╝██║  ██║███████╗██║███████╗███████╗██║  ██║\n" +
                                                "   ╚═╝   ╚══════╝      ╚═══╝  ╚═╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝╚══════╝╚══════╝╚═╝  ╚═╝\n" +
                                                "                                                                                             \n");

}
