package model.packet;

import app.streamAnalyzer.TimestampParser;
import model.descriptors.Descriptor;


public abstract class Payload extends TimestampParser {
    protected byte[] data;
    private boolean isPSI;
    private  boolean hasPESheader;

    protected Payload(boolean isPSI,  boolean hasPESheader){
        this.hasPESheader = hasPESheader;
        this.isPSI = isPSI;
        //this.data = data;
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
