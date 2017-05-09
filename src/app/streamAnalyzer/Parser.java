package app.streamAnalyzer;


import model.packet.Packet;
import model.Tables;
import model.config.MPEG;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

abstract class Parser extends MPEG {

    Tables tables;


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


    int[] parseNfields(byte[] packet, int pos, int length) {

        int position = pos;
        int[] byteFields = new int[length];
        for (int index = 0; index < length; index++) {
            byteFields[index] = ((packet[position++]) & 0x000000ff);
        }
        return byteFields;
    }

    String parseNchars(byte[] binaryHeader, int position, int length) {
        int end = position + length;
        StringBuilder stringBuilder = new StringBuilder();

        for (; position < end; position += charSize) {
            stringBuilder.append(binToChar(binaryHeader, position));
        }
        return stringBuilder.toString();
    }


    private char binToChar(byte[] binaryHeader, int start) {

        char result = 0;
        for (int i = start; i < start + charSize; i++) {
            result = (char) ((result << 1) | (binaryHeader[i] == 1 ? 1 : 0));
        }
        return result;
    }
}
