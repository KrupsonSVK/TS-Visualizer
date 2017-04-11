package model.psi;


import java.util.Map;

public class PAT extends PSI {


    private int transportStreamID;
    private short versionNum;
    private byte CNI;
    private int sectionNum;
    private int lastSectionNum;
    private Map PATmap;
    private long CRC;

    public PAT(short tableID, byte SSI, int sectionLength) {
        super(tableID, SSI, sectionLength,null);
    }


    public PAT(short tableID, byte ssi, int sectionLength, int transportStreamID, short versionNum, byte CNI, int sectionNum, int lastSectionNum, Map PATmap, long CRC) {
        super(tableID, ssi, sectionLength,null);
        this.transportStreamID = transportStreamID;
        this.versionNum = versionNum;
        this.CNI = CNI;
        this.sectionNum = sectionNum ;
        this.lastSectionNum = lastSectionNum;
        this.PATmap = PATmap;
        this.CRC = CRC;
    }

    public PAT(short i, byte ssi, int sectionLength, int programNum, byte versionNum, byte currentNextIndicator, byte sectionNum, byte lastSectionNum, short pcr_pid, short programInfoLength, byte[] descriptors, long crc) {
    }

    public int getTransportStreamID() {
        return transportStreamID;
    }

    public short getVersionNum() {
        return versionNum;
    }

    public byte getCNI() {
        return CNI;
    }

    public int getSectionNum() {
        return sectionNum;
    }

    public int getLastSectionNum() {
        return lastSectionNum;
    }

    public Map getPATmap() {
        return PATmap;
    }

    public long getCRC() {
        return CRC;
    }
}
