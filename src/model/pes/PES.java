package model.pes;


import model.packet.Payload;

import static model.config.MPEG.nil;

public class PES extends Payload {

    private int streamID;
    private int PESpacketLength;
    private byte PESscramblingControl;
    private byte PESpriority;
    private byte dataAlignmentIndicator;
    private byte copyright;
    private byte originalOrCopy;

    private byte PTSdtsFlags;
    private byte ESCRflag;
    private byte ESrateFlag;
    private byte DSMtrickModeFlag;
    private byte AdditionalCopyInfoFlag;
    private byte PEScrcFlag;
    private byte PESextensionFlag;

    private int PESheaderDataLength;

    private long PTS;
    private long DTS;
    private long ESCR;
    private long ESrate;
    private int DSMtrickMode;
    private int AdditionalCopyInfo;
    private long PEScrc;

    private long PTStimestamp;
    private long DTStimestamp;

    public PES(PES header, byte PTSdtsFlags, byte ESCRflag, byte ESrateFlag, byte DSMtrickModeFlag, byte additionalCopyInfoFlag, byte PEScrcFlag, byte PESextensionFlag, int PESheaderDataLength, long PTS, long DTS, long ESCR, long ESrate, int DSMtrickMode, int AdditionalCopyInfo, long PEScrc) {

        super( false, true);

        this.streamID = header.streamID;
        this.PESpacketLength = header.PESpacketLength;
        this.PESscramblingControl = header.PESscramblingControl;
        this.PESpriority = header.PESpriority;
        this.dataAlignmentIndicator = header.dataAlignmentIndicator;
        this.copyright = header.copyright;
        this.originalOrCopy = header.originalOrCopy;

        this.PTSdtsFlags = PTSdtsFlags;
        this.ESCRflag = ESCRflag;
        this.ESrateFlag = ESrateFlag;
        this.DSMtrickModeFlag = DSMtrickModeFlag;
        this.AdditionalCopyInfoFlag = additionalCopyInfoFlag;
        this.PEScrcFlag = PEScrcFlag;
        this.PESextensionFlag = PESextensionFlag;

        this.PESheaderDataLength = PESheaderDataLength;

        this.PTS = PTS;
        this.DTS = DTS;

        if(DTS == nil){
            PTStimestamp = PTS;
            DTStimestamp = nil;
        }
        else {
            PTStimestamp = PTS;
            DTStimestamp = DTS;
        }

        this.ESCR = ESCR;
        this.ESrate = ESrate;
        this.DSMtrickMode = DSMtrickMode;
        this.AdditionalCopyInfo = AdditionalCopyInfo;
        this.PEScrc = PEScrc;
    }


    public PES(int streamID, int PESpacketLength, byte PESscramblingControl, byte PESpriority, byte dataAlignmentIndicator, byte copyright, byte originalOrCopy) {

        super( false, true);

        this.streamID = streamID;
        this.PESpacketLength = PESpacketLength;
        this.PESscramblingControl = PESscramblingControl;
        this.PESpriority = PESpriority;
        this.dataAlignmentIndicator = dataAlignmentIndicator;
        this.copyright = copyright;
        this.originalOrCopy = originalOrCopy;
    }


    public PES() {
        super(false, false);
    }

    public int getStreamID() {
        return streamID;
    }

    public int getPESpacketLength() {
        return PESpacketLength;
    }

    public byte getPESscramblingControl() {
        return PESscramblingControl;
    }

    public byte getPESpriority() {
        return PESpriority;
    }

    public byte getDataAlignmentIndicator() {
        return dataAlignmentIndicator;
    }

    public byte getCopyright() {
        return copyright;
    }

    public byte getOriginalOrCopy() {
        return originalOrCopy;
    }

    public byte getPTSdtsFlags() {
        return PTSdtsFlags;
    }

    public byte getESrateFlag() {
        return ESrateFlag;
    }

    public byte getDSMtrickModeFlag() {
        return DSMtrickModeFlag;
    }

    public byte getAdditionalCopyInfoFlag() {
        return AdditionalCopyInfoFlag;
    }

    public byte getPEScrcFlag() {
        return PEScrcFlag;
    }

    public byte getPESextensionFlag() {
        return PESextensionFlag;
    }

    public byte getESCRflag() {
        return ESCRflag;
    }

    public int getPESheaderDataLength() {
        return PESheaderDataLength;
    }

    public long getPTS() {
        return PTS;
    }

    public long getPTStimestamp() {
        return PTStimestamp;
    }

    public long getDTStimestamp() {
        return DTStimestamp;
    }

    public long getDTS() {
        return DTS;
    }

    public long getESCR() {
        return ESCR;
    }

    public long getESrate() {
        return ESrate;
    }

    public int getDSMtrickMode() {
        return DSMtrickMode;
    }

    public int getAdditionalCopyInfo() {
        return AdditionalCopyInfo;
    }

    public long getPEScrc() {
        return PEScrc;
    }
}