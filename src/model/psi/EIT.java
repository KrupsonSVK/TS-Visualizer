package model.psi;


import java.util.List;

public class EIT extends PSI {

    private int serviceID;
    private byte versionNum;
    private byte CNI;
    private short sectionNum;
    private short lastSectionNum;
    private int transportStreamID;
    private int originalNetworkID;
    private short segmentLastSectionNumber;
    private short lastTableID;
    private int eventID;
    private long startTime;
    private int duration;
    private byte runningStatus;
    private short descriptorsLoopLength;
    private List descriptors;
    private long CRC;


    EIT(short tableID, byte SSI, int sectionLength) {
        super(tableID, SSI, sectionLength,null);
    }


    public EIT(short tableID, byte SSI, int sectionLength, int serviceID, byte versionNum, byte CNI, short sectionNum, short lastSectionNum, int transportStreamID, int originalNetworkID, short segmentLastSectionNumber, short lastTableID, int eventID, long startTime, int duration, byte runningStatus, short descriptorsLoopLength, List descriptors, long CRC) {
        super(tableID, SSI, sectionLength, null);
        this.serviceID = serviceID;
        this.versionNum = versionNum;
        this.CNI = CNI;
        this.sectionNum = sectionNum;
        this.lastSectionNum = lastSectionNum;
        this.transportStreamID = transportStreamID;
        this.originalNetworkID = originalNetworkID;
        this.segmentLastSectionNumber = segmentLastSectionNumber;
        this.lastTableID = lastTableID;
        this.eventID = eventID;
        this.startTime = startTime;
        this.duration = duration;
        this.runningStatus = runningStatus;
        this.descriptorsLoopLength = descriptorsLoopLength;
        this.descriptors = descriptors;
        this.CRC = CRC;
    }


    public int getServiceID() {
        return serviceID;
    }

    public byte getVersionNum() {
        return versionNum;
    }

    public byte getCNI() {
        return CNI;
    }

    public short getSectionNum() {
        return sectionNum;
    }

    public short getLastSectionNum() {
        return lastSectionNum;
    }

    public int getTransportStreamID() {
        return transportStreamID;
    }

    public int getOriginalNetworkID() {
        return originalNetworkID;
    }

    public short getSegmentLastSectionNumber() {
        return segmentLastSectionNumber;
    }

    public short getLastTableID() {
        return lastTableID;
    }

    public int getEventID() {
        return eventID;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public byte getRunningStatus() {
        return runningStatus;
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