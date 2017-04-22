package model;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static model.Sorter.getByValue;
import static model.Sorter.sortHashMapByKey;
import static model.config.DVB.nil;

public abstract class Timestamp{


    public String parseTimestamp(long milliseconds){

        final double seconds = (milliseconds / 1000.) % 60.;
        final long minutes = (milliseconds / (1000 * 60)) % 60;
        final long hours = (milliseconds / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%06.3f", hours, minutes, seconds, milliseconds);
    }


    protected long midBits(long k, int m, int n){
        return (k >> m) & ((1 << (n-m))-1);
    }


    protected long midBits(long k, long m, long n){
        return (k >> m) & ((1 << (n-m))-1);
    }


    protected long parsePCRopcr(long PCRopcr){

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


    protected <K,V extends Map<K, V>> Map createDeltaBitrateMap(Map<K, V> bitrateMap) {

        NavigableMap<K, V> navigablePCRmap = new TreeMap<>(bitrateMap);
        Map bitratePCRmap = new HashMap<K,V>();

        for (Map.Entry<K, V> currentPIDmap : navigablePCRmap.entrySet()) {
            Map.Entry<K, V> previousPIDmap = navigablePCRmap.lowerEntry(currentPIDmap.getKey());
            V previousPIDmapValue = (previousPIDmap==null) ? null : previousPIDmap.getValue();
            bitratePCRmap.put(currentPIDmap.getKey(),calculateBitrateDelta(currentPIDmap.getValue(),previousPIDmapValue));
        }

        return sortHashMapByKey(bitratePCRmap);
    }


    private<K,V> Map calculateBitrateDelta(Map<K,V> current, Map<K,V> previous) {
        if (previous==null) {
            return current;
        }
        Map deltaMap = new HashMap<K, V>();
        for (Map.Entry<K, V> currentEntry : current.entrySet()) {
            K currentKey = currentEntry.getKey();

            Integer previousValue = (Integer) previous.get(currentKey);
            previousValue = (previousValue==null) ? 0 : previousValue;

            Integer currentValue = (currentEntry.getValue()==null) ? 0 : (Integer)currentEntry.getValue();
            int delta = currentValue - previousValue;

            deltaMap.put(currentKey,delta);
        }
        return deltaMap;
    }


    protected <K, V> Map filterProgram(String selectedProgram, Map<K, V> PMTmap, Map<K, V> programMap) {

        if (selectedProgram.equals("All")){
            return null;
        }
        Integer programPID = (Integer)getByValue(programMap, (V) selectedProgram);

        Map filteredMap = new HashMap();
        for (Map.Entry<K, V> entry : PMTmap.entrySet()) {
            if(entry.getValue().equals(programPID)) {
                filteredMap.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredMap;
    }

}
