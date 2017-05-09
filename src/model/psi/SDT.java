package model.psi;


import java.util.List;

public class SDT extends PSI {

    private int transportStreamID;
    private byte versionNum;
    private byte currentNextIndicator;
    private short sectionNum;
    private short lastSectionNum;
    private int originalNetworkID;
    private int serviceID;
    private byte EITscheduleFlag;
    private byte EITpresentFollowingFlag;
    private byte runningStatus;
    private byte freeCAmode;
    private short descriptorsLoopLength;
    private List descriptors;
    private long CRC;

    public SDT(short tableID, byte SSI, int sectionLength, int transportStreamID, byte versionNum, byte currentNextIndicator, short sectionNum, short lastSectionNum, int originalNetworkID, int serviceID, byte EITscheduleFlag, byte EITpresentFollowingFlag, byte runningStatus, byte freeCAmode, short descriptorsLoopLength, List descriptors, long CRC) {
        super(tableID, SSI, sectionLength, null);
        this.transportStreamID = transportStreamID;
        this.versionNum = versionNum;
        this.currentNextIndicator = currentNextIndicator;
        this.sectionNum = sectionNum;
        this.lastSectionNum = lastSectionNum;
        this.originalNetworkID = originalNetworkID;
        this.serviceID = serviceID;
        this.EITscheduleFlag = EITscheduleFlag;
        this.EITpresentFollowingFlag = EITpresentFollowingFlag;
        this.runningStatus = runningStatus;
        this.freeCAmode = freeCAmode;
        this.descriptorsLoopLength = descriptorsLoopLength;
        this.descriptors = descriptors;
        this.CRC = CRC;

    }

    public SDT(short tableID, byte SSI, int sectionLength, int transportStreamID, byte versionNum, byte currentNextIndicator, short sectionNum, short lastSectionNum, int originalNetworkID, List descriptors, long CRC) {
        super(tableID, SSI, sectionLength, null);
        this.transportStreamID = transportStreamID;
        this.versionNum = versionNum;
        this.currentNextIndicator = currentNextIndicator;
        this.sectionNum = sectionNum;
        this.lastSectionNum = lastSectionNum;
        this.originalNetworkID = originalNetworkID;
        this.serviceID = serviceID;
        this.EITscheduleFlag = EITscheduleFlag;
        this.EITpresentFollowingFlag = EITpresentFollowingFlag;
        this.runningStatus = runningStatus;
        this.freeCAmode = freeCAmode;
        this.descriptorsLoopLength = descriptorsLoopLength;
        this.descriptors = descriptors;
    }

    public int getTransportStreamID() {
        return transportStreamID;
    }

    public byte getVersionNum() {
        return versionNum;
    }

    public byte getCurrentNextIndicator() {
        return currentNextIndicator;
    }

    public short getSectionNum() {
        return sectionNum;
    }

    public short getLastSectionNum() {
        return lastSectionNum;
    }

    public int getOriginalNetworkID() {
        return originalNetworkID;
    }

    public int getServiceID() {
        return serviceID;
    }

    public byte getEITscheduleFlag() {
        return EITscheduleFlag;
    }

    public byte getEITpresentFollowingFlag() {
        return EITpresentFollowingFlag;
    }

    public byte getRunningStatus() {
        return runningStatus;
    }

    public byte getFreeCAmode() {
        return freeCAmode;
    }

    public short getDescriptorsLoopLength() {
        return descriptorsLoopLength;
    }

    public List getDescriptors() {
        return descriptors;
    }

    public long getCRC() {
        return CRC;
    }
}
