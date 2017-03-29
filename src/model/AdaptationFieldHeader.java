package model;


public class AdaptationFieldHeader {

    private short adaptationFieldLength;
    private final byte DI;
    private final byte RAI;
    private final byte ESPI;
    private final byte PF;
    private final byte OF;
    private final byte SPF;
    private final byte TPDF;
    private final byte AFEF;
    private final AdaptationFieldOptionalFields adaptationFieldOptionalFields;
    private Payload payload;

    public AdaptationFieldHeader(short adaptationFieldLength, byte DI, byte RAI, byte ESPI, byte OF, byte PF, byte SPF, byte TPDF, byte AFEF, AdaptationFieldOptionalFields adaptationFieldOptionalFields) {
        this.adaptationFieldLength = adaptationFieldLength;
        this.DI = DI;
        this.RAI = RAI;
        this.ESPI = ESPI;
        this.PF = PF;
        this.OF = OF;
        this.SPF = SPF;
        this.TPDF = TPDF;
        this.AFEF = AFEF;
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
        return PF;
    }
    public byte getOPCRF() {
        return OF;
    }
    public byte getSPF() {
        return SPF;
    }
    public byte getTPDF() {
        return TPDF;
    }
    public byte getAFEF() {
        return AFEF;
    }
    public AdaptationFieldOptionalFields getOptionalField() {
        return adaptationFieldOptionalFields;
    }
}