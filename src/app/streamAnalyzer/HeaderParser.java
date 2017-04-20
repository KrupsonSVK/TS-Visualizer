package app.streamAnalyzer;

import model.packet.Packet;


public class HeaderParser extends Parser {


    HeaderParser(){
    }


    Packet analyzeHeader(byte[] header, byte[] packet, long index) {

        int position = syncByteBinarySize;
        byte transportErrorIndicator = header[position++];
        byte payloadStartIndicator = header[position++];
        byte transportPriority = header[position++];
        short PID = (short) binToInt(header, position, 24);
        byte tranportScramblingControl = (byte) binToInt(header, 24, 26);
        byte adaptationFieldControl = (byte) binToInt(header, 24, 28);
        byte continuityCounter = (byte) binToInt(header, 28, 32);
        short adaptationFieldLength =  0;

        return new Packet(index,transportErrorIndicator, payloadStartIndicator, transportPriority, PID, tranportScramblingControl, adaptationFieldControl, continuityCounter, adaptationFieldLength, packet);
    }


    int parseHeader(byte[] packet) {

        return ((packet[0] << 24) & 0xff000000 |
                (packet[1] << 16) & 0x00ff0000 |
                (packet[2] << 8)  & 0x0000ff00 |
                (packet[3])       & 0x000000ff);
    }
}
