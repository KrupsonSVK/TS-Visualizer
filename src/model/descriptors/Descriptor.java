package model.descriptors;


import model.packet.Payload;


public abstract class Descriptor extends Payload{

    protected Descriptor(boolean isPSI, boolean hasPESheader) {
        super(isPSI, hasPESheader);
    }
}
