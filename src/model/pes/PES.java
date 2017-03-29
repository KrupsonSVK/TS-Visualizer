package model.pes;


import model.Payload;

public class PES extends Payload {

    private int streamID;
    private int PESpacketLength;
    private byte PESscramblingControl;
    private byte PESpriority;
    private byte DataAlignmentIndicator;
    private byte copyright;
    private byte OriginalOrCopy;
    private byte PTSdtsFlags;
    private byte ESCRflag;
    private byte ESrateFlag;
    private byte DSMtrickModeFlag;
    private byte AdditionalCopyInfoFlag;
    private byte PEScrcFlag;
    private byte PESextensionFlag;
    private int PESheaderDataLength;
    private PESoptionalHeader optionalPESheader;


    public PES(int streamID, int PESpacketLength, byte PESscramblingControl, byte PESpriority, byte dataAlignmentIndicator, byte copyright, byte originalOrCopy, byte PTSdtsFlags, byte ESCRflag, byte ESrateFlag, byte DSMtrickModeFlag, byte additionalCopyInfoFlag, byte PEScrcFlag, byte PESextensionFlag, int PESheaderDataLength, PESoptionalHeader optionalPESheader, byte[] PESpacketData) {
        super(PESpacketData, false, true);
        this.streamID = streamID;
        this.PESpacketLength = PESpacketLength;
        this.PESscramblingControl = PESscramblingControl;
        this.PESpriority = PESpriority;
        this.DataAlignmentIndicator = dataAlignmentIndicator;
        this.copyright = copyright;
        this.OriginalOrCopy = originalOrCopy;
        this.PTSdtsFlags = PTSdtsFlags;
        this.ESCRflag = ESCRflag;
        this.ESrateFlag = ESrateFlag;
        this.DSMtrickModeFlag = DSMtrickModeFlag;
        this.AdditionalCopyInfoFlag = additionalCopyInfoFlag;
        this.PEScrcFlag = PEScrcFlag;
        this.PESextensionFlag = PESextensionFlag;
        this.PESheaderDataLength = PESheaderDataLength;
        this.optionalPESheader = optionalPESheader;
    }


    public PES(byte[] PESpacketData) {
        super(PESpacketData,false, false);
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
        return DataAlignmentIndicator;
    }
    public byte getCopyright() {
        return copyright;
    }
    public byte getOriginalOrCopy() {
        return OriginalOrCopy;
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
    public int getPESheaderDataLength() {
        return PESheaderDataLength;
    }
    public PESoptionalHeader getOptionalPESheader() {
        return optionalPESheader;
    }
    public byte getESCRflag() {
        return ESCRflag;
    }


    public class PESoptionalHeader {
    }
}