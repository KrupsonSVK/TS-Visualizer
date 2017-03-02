package model;

public class AdaptationFieldOptionalFields {

    private final long PCR;
    private final long OPCR;
    private final byte spliceCoutdown;
    private final short TPDlength;
    private final byte[] TPD;
    private final short AFEFlength;
    private final byte LTWF;
    private final byte PRF;
    private final byte SSF;

    public AdaptationFieldOptionalFields(long PCR, long OPCR, byte spliceCoutdown, short TPDlength, byte[] TPD, short AFElength, byte LTWF, byte PRF, byte SSF) {
        this.PCR = PCR;
        this.OPCR = OPCR;
        this.spliceCoutdown = spliceCoutdown;
        this.TPDlength = TPDlength;
        this.TPD = TPD;
        this.AFEFlength = AFElength;
        this.LTWF = LTWF;
        this.PRF = PRF;
        this.SSF = SSF;
    }
}
