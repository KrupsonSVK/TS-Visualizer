package model.psi;


import java.util.List;

public class PMT_ extends PSI{
    private int programNum;
    private byte versionNum;
    private byte currentNextIndicator;
    private byte sectionNum;
    private byte lastSectionNum;
    private short PCR_PID;
    private short programInfoLength;
    private byte[] descriptors;
    private List PMTloop;
    private long CRC;

    public PMT_(short tableID, byte SSI, int sectionLength) {
        super(tableID, SSI, sectionLength, null);
    }

    public PMT_() {
        super();
    }

    public PMT_(short tableID, byte ssi, int sectionLength, int programNum, byte versionNum, byte currentNextIndicator, byte sectionNum, byte lastSectionNum, short pcr_pid, short programInfoLength, byte[] descriptors, long crc) {
        super(tableID, ssi, sectionLength, null);
        this.programNum = programNum;
        this.versionNum = versionNum;
        this.currentNextIndicator = currentNextIndicator;
        this.sectionNum = sectionNum;
        this.lastSectionNum = lastSectionNum;
        this.PCR_PID = pcr_pid;
        this.programInfoLength = programInfoLength;
        this.descriptors = descriptors;
        this.CRC = crc;
    }

    public int getProgramNum() {
        return programNum;
    }
    public byte getVersionNum() {
        return versionNum;
    }
    public byte getCurrentNextIndicator() {
        return currentNextIndicator;
    }
    public byte getSectionNum() {
        return sectionNum;
    }
    public byte getLastSectionNum() {
        return lastSectionNum;
    }
    public short getPCR_PID() {
        return PCR_PID;
    }
    public short getProgramInfoLength() {
        return programInfoLength;
    }
    public List getPMTloop() {
        return PMTloop;
    }
    public long getCRC() {
        return CRC;
    }

    private class PMTloop{
        private int streamType;
        private int elementaryPID;
        private int ESinfoLength;
        private List descriptors;
    }
}
