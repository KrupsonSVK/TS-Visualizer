package model.packet;

import model.Timestamp;

import static model.config.DVB.nil;

public class AdaptationFieldOptionalFields extends Timestamp {

    private final long PCR;
    private final long OPCR;
    private final byte spliceCoutdown;
    private final short TPDlength;
    private final byte[] TPD;
    private final short AFEFlength;
    private final byte LTW;
    private final byte piecewise_rate;
    private final byte seamless_splice;

    private long PCRtimestamp;
    private long OPCRtimestamp;

    public AdaptationFieldOptionalFields(long PCR, long OPCR, byte spliceCoutdown, short TPDlength, byte[] TPD, short AFElength, byte LTW, byte piecewise_rate, byte seamless_splice) {
        this.PCR = PCR;
        this.OPCR = OPCR;
        this.spliceCoutdown = spliceCoutdown;
        this.TPDlength = TPDlength;
        this.TPD = TPD;
        this.AFEFlength = AFElength;
        this.LTW = LTW;
        this.piecewise_rate = piecewise_rate;
        this.seamless_splice = seamless_splice;

        PCRtimestamp = PCR==nil ? nil : parsePCRopcr(PCR);
        OPCRtimestamp = OPCR==nil ? nil :parsePCRopcr(OPCR);
    }


    public long getPCR() {
        return PCR;
    }

    public long getOPCR() {
        return OPCR;
    }

    public byte getSpliceCoutdown() {
        return spliceCoutdown;
    }

    public short getTPDlength() {
        return TPDlength;
    }

    public byte[] getTPD() {
        return TPD;
    }

    public short getAFEFlength() {
        return AFEFlength;
    }

    public byte getLTW() {
        return LTW;
    }

    public byte getPiecewise_rate() {
        return piecewise_rate;
    }

    public byte getSeamless_splice() {
        return seamless_splice;
    }

    public long getPCRtimestamp() {
        return PCRtimestamp;
    }

    public long getOPCRtimestamp() {
        return OPCRtimestamp;
    }
}
