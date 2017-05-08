package app.streamAnalyzer;

import model.packet.Packet;


/**
 * Trieda obsahuje metódy realizujúce parsovanie jednotlivé bity hlavičky transportného paketu
 */
class HeaderParser extends Parser {

    /**
     * Funkcia parsuje jednotlivé bity hlavičky transportného paketu
     *
     * @param header hlavička transportného paketu v binárnom poli
     * @param packet transportný paket v poli bajtov
     * @param index absolútna pozícia paketu v transportnom toku
     * @return objekt triedy Packet obsahujúci získané údaje
     */
    Packet analyzeHeader(byte[] header, byte[] packet, long index) {

        int position = syncByteBinarySize; //posunutie začiatku o veľkosť synchronizačného bajtu
        byte transportErrorIndicator = header[position++]; //získanie jedného bitu a prevod na celočíselnú hodnotu
        byte payloadStartIndicator = header[position++];
        byte transportPriority = header[position++];
        short PID = (short) binToInt(header, position, position += PIDfieldLength); //získanie n-bitov dĺžky poľa PID a prevod na celočíselnú hodnotu
        byte tranportScramblingControl = (byte) binToInt(header, position, position += TSCfieldLength); //2
        byte adaptationFieldControl = (byte) binToInt(header, position, position += adaptationFieldControlLength); //4
        byte continuityCounter = (byte) binToInt(header, position, continuityCounterLength); //4

        return new Packet(
                index,
                transportErrorIndicator,
                payloadStartIndicator,
                transportPriority,
                PID,
                tranportScramblingControl,
                adaptationFieldControl,
                continuityCounter,
                (short) 0,
                packet
        );
    }

    /**
     * @param packet transportný paket v poli bajtov
     * @return 4 bajtový integer
     */
    int parseHeader(byte[] packet) {
        //získa prvé 4 bajty so vstupného poľa bajtov aplikovaním bitových masiek
        return ((packet[0] << 24) & 0xff000000 |
                (packet[1] << 16) & 0x00ff0000 |
                (packet[2] << 8)  & 0x0000ff00 |
                (packet[3])       & 0x000000ff
        );
    }
}
