package model;


public class Payload {
    protected byte[] data;
    private boolean isPSI;
    private  boolean hasPESheader;

    protected Payload(byte[] data, boolean isPSI,  boolean hasPESheader){
        this.hasPESheader = hasPESheader;
        this.isPSI = isPSI;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public boolean hasPESheader() {
        return hasPESheader;
    }

    public boolean isPSI() {
        return isPSI;
    }
}
