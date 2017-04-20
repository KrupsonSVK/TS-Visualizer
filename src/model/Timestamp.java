package model;

import static model.config.DVB.nil;

public abstract class Timestamp {

    public String parseTimestamp(long milliseconds){

        final double seconds = (milliseconds / 1000.) % 60.;
        final long minutes = (milliseconds / (1000 * 60)) % 60;
        final long hours = (milliseconds / (1000 * 60 * 60)) % 24;

        return String.format("(%02d:%02d:%06.3f) ", hours, minutes, seconds, milliseconds);
    }


    protected long midBits(long k, int m, int n){
        return (k >> m) & ((1 << (n-m))-1);
    }


    protected long midBits(long k, long m, long n){
        return (k >> m) & ((1 << (n-m))-1);
    }


    long parsePCRopcr(long PCRopcr){

        final int extStart = 0;
        final int extEnd = 9;
        long PCRextension = midBits(PCRopcr, extStart, extEnd);

        final int PCRstart = 15;
        final int PCRend = 48;

        long timestampFirst = midBits(PCRopcr, PCRstart, PCRend/2);
        long timestampSecond = midBits(PCRopcr, PCRend/2, PCRend);
        long timestamp = (timestampSecond << 9) | timestampFirst ;

        return timestamp / 90; //milliseconds
    };


    protected long parsePTSdts(long pts, long PTSdts){
        long timestamp;
        if(pts != nil) {
            timestamp = (midBits(pts, 17, 35) << 15) | midBits(pts, 1, 16);
            return timestamp / 90; //milliseconds
        }
        else if (PTSdts != nil) {
            timestamp = (midBits(PTSdts, 17, 33) << 15) | midBits(PTSdts, 1, 16);
            return timestamp / 90;
        }
        return 0;
    };
}
