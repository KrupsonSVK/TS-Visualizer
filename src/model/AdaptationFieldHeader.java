package model;


public class AdaptationFieldHeader {

    private short adaptationFieldLength;
    private final byte DI;
    private final byte RAI;
    private final byte ESPI;
    private final byte PCRflag;
    private final byte OPCRflag;
    private final byte splicingPointFlag;
    private final byte TPDflag;
    private final byte AFEflag;
    private AdaptationFieldOptionalFields adaptationFieldOptionalFields;


    public AdaptationFieldHeader(short adaptationFieldLength, byte DI, byte RAI, byte ESPI, byte OPCRflag, byte PCRflag, byte splicingPointFlag, byte TPDflag, byte AFEflag, AdaptationFieldOptionalFields adaptationFieldOptionalFields) {
        this.adaptationFieldLength = adaptationFieldLength;
        this.DI = DI;
        this.RAI = RAI;
        this.ESPI = ESPI;
        this.PCRflag = PCRflag;
        this.OPCRflag = OPCRflag;
        this.splicingPointFlag = splicingPointFlag;
        this.TPDflag = TPDflag;
        this.AFEflag = AFEflag;
        this.adaptationFieldOptionalFields = adaptationFieldOptionalFields;
    }


    public short getAdaptationFieldLength() {
        return adaptationFieldLength;
    }

    public byte getESPI() {
        return ESPI;
    }

    public byte getRAI() {
        return RAI;
    }

    public byte getDI() {
        return DI;
    }

    public byte getPCRF() {
        return PCRflag;
    }

    public byte getOPCRF() {
        return OPCRflag;
    }

    public byte getSplicingPointFlag() {
        return splicingPointFlag;
    }

    public byte getTPDflag() {
        return TPDflag;
    }

    public byte getAFEflag() {
        return AFEflag;
    }

    public AdaptationFieldOptionalFields getOptionalFields() {
        return adaptationFieldOptionalFields;
    }

    public void setAdaptationFieldOptionalFields(AdaptationFieldOptionalFields adaptationFieldOptionalFields){
        this.adaptationFieldOptionalFields = adaptationFieldOptionalFields;
    }
}