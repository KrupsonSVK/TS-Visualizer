package model.psi;


import model.Payload;

public class PSI extends Payload {

    private short tableID;
    private byte SSI;
    private int sectionLength;

    public PSI(short tableID, byte SSI, int sectionLength) {

        this.tableID = tableID;
        this.SSI = SSI;
        this.sectionLength = sectionLength;
    }

    public PSI() {

    }

    public short tableID() {
        return tableID;
    }
    public byte getSSI() {
        return SSI;
    }
    public int getSectionLength() {
        return sectionLength;
    }

}


