package app.streamAnalyzer;


import model.packet.Packet;
import model.Tables;
import model.config.DVB;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Parser extends DVB {

    public Tables tables;


    Parser(){
        this.tables = new Tables();
    }


    byte[] intToBinary(int[] intFields, int length) {
        int size = length * byteBinaryLength;
        byte[] binaryFields = new byte[size];
        int offset = 0;

        for (int index = length-1 ; index >= 0  ; index--) {
            for (int i = 0; i < byteBinaryLength; i++, offset++) {
                binaryFields[size - offset - 1] = getBit(intFields[index], i);
            }
        }
        return binaryFields;
    }


    int calculatePosition(Packet analyzedHeader) {
        int position=tsHeaderSize;

        if(isAdaptationField(analyzedHeader)) {
            position += analyzedHeader.getAdaptationFieldHeader().getAdaptationFieldLength();
        }
        return position;
    }


    public byte getBit(int source, int position) {
        return (byte) ((source >> position) & 1);
    }


    long binToInt(byte[] binaryHeader, int start, int end) {

        long result = 0;
        for (int i = start; i < end; i++) {
            result = (result << 1) | (binaryHeader[i] == 1 ? 1 : 0);
        }
        return result;
    }


    boolean isAdaptationField(Packet header) {
        int adaptationFieldControl = header.getAdaptationFieldControl();

        return (adaptationFieldControl == adaptationFieldOnly || adaptationFieldControl == adaptationFieldAndPayload);
    }


    protected Map createPrograms(Map inputMap) {
        HashMap<Integer, String> outputMap = new HashMap<>();
        Set<Integer> keys = inputMap.keySet(); // The set of keys in the map.

        for (Integer key : keys) {
            Integer value = (Integer) inputMap.get(key);
            outputMap.put(value, Integer.toString(value));
        }
        return outputMap;
    }


    int[] parseNfields(byte[] packet, int pos, int length) {

        int position = pos;
        int[] byteFields = new int[length];
        for (int index = 0; index < length; index++) {
            byteFields[index] = ((packet[position++]) & 0x000000ff);
        }
        return byteFields;
    }
}
