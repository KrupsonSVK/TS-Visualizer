package model;

public class TSpacket {

    private final byte transportErrorIndicator;
    private final byte payloadStartIndicator;
    private final byte transportPriority;
    private final short PID;
    private final byte tranportScramblingControl;
    private final byte adaptationFieldControl;
    private final byte continuityCounter;
    private AdaptationFieldHeader adaptationFieldHeader;
    private Payload payload;

    public TSpacket(byte transportErrorIndicator, byte payloadStartIndicator, byte transportPriority, short PID, byte tranportScramblingControl, byte adaptationFieldControl, byte continuityCounter, short adaptationFieldLength) {
        this.transportErrorIndicator = (byte) transportErrorIndicator;
        this.payloadStartIndicator = (byte) payloadStartIndicator;
        this.transportPriority = (byte) transportPriority;
        this.PID = (short) PID;
        this.tranportScramblingControl = (byte) tranportScramblingControl;
        this.adaptationFieldControl = (byte) adaptationFieldControl;
        this.continuityCounter = (byte) continuityCounter;
    }

    public char getTransportErrorIndicator() {
        return (char) transportErrorIndicator;
    }
    public char getPayloadStartIndicator() {
        return (char) payloadStartIndicator;
    }
    public char getTransportPriority() {
        return (char) transportPriority;
    }
    public int getPID() {
        return PID;
    }
    public int getTranportScramblingControl() {
        return tranportScramblingControl;
    }
    public int getAdaptationFieldControl() {
        return adaptationFieldControl;
    }
    public int getContinuityCounter() {
        return continuityCounter;
    }
    public AdaptationFieldHeader getAdaptationFieldHeader() {
        return adaptationFieldHeader;
    }
    public Payload getPayload() {
        return payload;
    }

    public void setAdaptationFieldHeader(AdaptationFieldHeader adaptationFieldHeader) {
        this.adaptationFieldHeader = adaptationFieldHeader;
    }
    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}






