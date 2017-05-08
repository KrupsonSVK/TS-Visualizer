package app.streamAnalyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static app.Main.localization;
import static model.MapHandler.getByValue;
import static model.MapHandler.sortHashMapByKey;
import static model.config.MPEG.*;

/**
 * Abstraktná trieda obsahujúca metódy na prácu s časovými značkami
 */
public abstract class TimestampParser{

    /**
     * Metóda vyparsuje z poľa časovej značky adaptačného poľa čas v milisekundách
     *
     * @param PCRopcr synchronizačná časová PCR alebo OPCR značka
     * @return čas v milisekundách
     */
    static long parsePCRopcr(long PCRopcr){

        final int PCRextStart = 0;
        final int PCRextEnd = PCRextStart + PCRextLength; // PCR rozšírenie dĺžky 9 bitov
        long PCRextension = midBits(PCRopcr, PCRextStart, PCRextEnd); //získanie PCR rozšírenia

        long timestampFirst = midBits(PCRopcr, PCRstartBit, PCRendBit /2); //prvých 9 bitov PCR
        long timestampSecond = midBits(PCRopcr, PCRendBit /2, PCRendBit); //zvyšných 24 bitov PCR
        long timestamp = (timestampSecond << PCRextLength) | timestampFirst ; //spojenie 33 bitov dokopy

        return timestamp / PCRsampleRate; //vracia milisekundy
    }

    /**
     * Metóda extrahuje zo vstupného čísla n-bitov zo žiadanej pozície
     *
     * @param source pôvodná hodnota
     * @param startPos začiatočná pozícia v pôvodnej hodnote
     * @param length počet žiadaných bitov
     * @return
     */
    private static long midBits(long source, int startPos, int length){
        //posun pôvodnej hodnoty doprava o začiatočnú pozíciu a logický súčin s maskou z length-startPo jednotiek
        return (source >> startPos) & ((1 << (length-startPos))-1);
    }

    /**
     * Metóda prevedie čas v milisekundách na reťazec vo formáte HH:mm:ss:SSS
     *
     * @param milliseconds vstupný čas v milisekundách
     * @return formátovaný časový reťazec
     */
    public String parseTimestamp(long milliseconds){

        final double seconds = (milliseconds / 1000.) % 60.; //získanie sekundovej zložky času
        final long minutes = (milliseconds / (1000 * 60)) % 60; //získanie minútovej zložky
        final long hours = (milliseconds / (1000 * 60 * 60)) % 24; //získanie hodinovej zložky
        //vytvorenie výstupného časového reťazca
        return String.format("%02d:%02d:%06.3f", hours, minutes, seconds, milliseconds);
    }


    static long parsePTSdts(long pts, long PTSdts){ //TODO bad implementation
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
    }


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

        if (selectedProgram.equals(localization.getAllText())){
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
