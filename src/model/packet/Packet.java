package model.packet;

public class Packet {

    private final long index;

    private final byte transportErrorIndicator;
    private final byte payloadStartIndicator;
    private final byte transportPriority;
    private final short PID;
    private final byte transportScramblingControl;
    private final byte adaptationFieldControl;
    private final byte continuityCounter;

    private AdaptationFieldHeader adaptationFieldHeader;
    private Payload payload;

    private byte[] data;

    public Packet(long index, byte transportErrorIndicator, byte payloadStartIndicator, byte transportPriority, short PID, byte transportScramblingControl, byte adaptationFieldControl, byte continuityCounter, short adaptationFieldLength, byte[] data) {
        this.index = index;
        this.transportErrorIndicator =  transportErrorIndicator;
        this.payloadStartIndicator =  payloadStartIndicator;
        this.transportPriority = transportPriority;
        this.PID = PID;
        this.transportScramblingControl =  transportScramblingControl;
        this.adaptationFieldControl =  adaptationFieldControl;
        this.continuityCounter = continuityCounter;
        this.data = data;
    }


    public long getIndex() {
        return index;
    }

    public char getTransportErrorIndicator() {
        return (char) transportErrorIndicator;
    }

    public int getPayloadStartIndicator() {
        return  payloadStartIndicator;
    }

    public int getTransportPriority() {
        return transportPriority;
    }

    public int getPID() {
        return PID;
    }

    public int getTransportScramblingControl() {
        return transportScramblingControl;
    }

    public Integer getAdaptationFieldControl() {
        return Integer.valueOf(adaptationFieldControl);
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

    public byte[] getData() {
        return data;
    }


    public void setAdaptationFieldHeader(AdaptationFieldHeader adaptationFieldHeader) {
        this.adaptationFieldHeader = adaptationFieldHeader;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}