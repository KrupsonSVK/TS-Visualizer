package app.streamAnalyzer;

import javafx.concurrent.Task;
import model.Tables;
import model.packet.AdaptationFieldHeader;
import model.packet.Packet;
import model.pes.PES;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static model.config.Config.snapshotInterval;


public class StreamParser extends Parser {

    private HeaderParser headerParser;
    private AdaptationFieldParser adaptationFieldParser;
    private PSIparser PSIparser;
    private PESparser PESparser;
    private Task<Tables> task;


    public StreamParser(Task task) {
        this.task = task;
        headerParser = new HeaderParser();
        adaptationFieldParser = new AdaptationFieldParser();
        PSIparser = new PSIparser();
        PESparser = new PESparser();
    }

    public static int getTsPacketSize() {
        return tsPacketSize;
    }

    /**
     * Funkcia spracovavá trasportný tok ako pole bajtov, pohybujúc sa sekvenčne po 188 bajtoch (dĺžkapaketu) jednotlivých paketov
     *
     * @param buffer vstupný buffer s transportným tokom
     */
    public void parseStream(byte[] buffer) {

        super.tables =  new Tables(); //objekty triedy Tables nesúce rôzne dátové tabuľky resp. asociácie
        PESparser.tables = super.tables;  //inicializácia objektov podtried
        PSIparser.tables = super.tables;

        this.task = new Task<Tables>() { //parsovanie prebieha ako úloha paralelizovaná do mnohých vlákien
            @Override
            public Tables call() throws InterruptedException, IOException {

                ArrayList<Packet> packets = new ArrayList<>(); //pole objektov triedy Packet predstavujúce pakety
                boolean isPATanalyzed = false; //indikátor analyzovaných PAT tabuliek
                int firstPosition = nil; //pozícia prvého paketu
                long packetIndex = 0; //pozícia paketu v transportnom toku
                int totalPackets; // celkové množstvo paketov transportného toku
                int tickInterval = nil; //interval vytvárania obrazou transportného toku
                int tick = 0; //počítadlo paketov pre interval vytvárania obrazu
                int lost = 0;

                for (int i = 0; i < buffer.length; i += tsPacketSize) { //prechádzanie celého transportného toku v cykle s posuvom veľkosi paketu

                    if(!isPATanalyzed && i>=buffer.length - 2*tsPacketSize) { // ak sa nenašla žiadna PAT tabuľka, predstierame, že sa našla
                        isPATanalyzed = true;
                        i = firstPosition; //vráti sa na pozíciu prvého paketu
                    }
                    if (i == 0) { //na začiatku vyhľadá prvý transportný paket
                        i = seekBeginning(buffer, i);
                        if (i == nil) { //ak sa nenašiel, skončí
                            throw new IOException("File does not contain TS stream!");
                        }
                        firstPosition = i; //zaznamená pozíciu prvého transportného paketu
                        totalPackets = sumPackets(buffer,i); //spočíta celkový počet paketov
                        tickInterval = totalPackets/snapshotInterval; //vypočíta interval ukladania paketového obrazu transportného toku
                    }
                    if (buffer[i] == syncByte) { // paket musí začínať synchronizačným bajtom
                        lost = 0;
                        byte[] packet = Arrays.copyOfRange(buffer, i, i + tsPacketSize); //skopírovanie bajtov paketu
                        int header = headerParser.parseHeader(packet); //získanie 4 bajtov hlavičky
                        byte[] binaryHeader = toBinary(header,tsHeaderBinaryLength); //prevod hlavičky na binárne pole

                        Packet analyzedHeader =  headerParser.analyzeHeader(binaryHeader,packet,packetIndex++); //analýza hlavičky paketu
                        if(isPATanalyzed) {
                            tables.updatePIDmap(analyzedHeader.getPID()); //aktualizácia tabuľky početnosti PIDov
                            if(tick++ == tickInterval){
                                tables.updateIndexSnapshotMap();    //aktualizácia paketového obrazu transportného toku
                                tick=0;
                            }
                            if (adaptationFieldParser.isAdaptationField(analyzedHeader) ) { //ak paket obsahuje transportné pole

                                short adaptationFieldHeader =  adaptationFieldParser.parseAdaptationFieldHeader(packet); //načítanie 2 bajtov adaptačného poľa
                                byte[] binaryAdaptationFieldHeader = toBinary(adaptationFieldHeader,tsAdaptationFieldHeaderBinaryLength); //prevod na binárne pole
                                analyzedHeader.setAdaptationFieldHeader(adaptationFieldParser.analyzeAdaptationFieldHeader(binaryAdaptationFieldHeader)); //analýza adaptačného poľa

                                short adaptationFieldLength = analyzedHeader.getAdaptationFieldHeader().getAdaptationFieldLength(); //získanie dĺžky adaptačného poľa
                                if (adaptationFieldLength > 1 && isOptionalField(analyzedHeader.getAdaptationFieldHeader())) { //ak adaptačné pole obsahuje doplnkové polia

                                    int[] adaptationOptionalFields = parseNfields(packet, tsAdaptationFieldHeaderPosition, adaptationFieldLength-1);
                                    byte[] binaryAdaptationFieldOptional = intToBinary(adaptationOptionalFields, adaptationFieldLength-1);

                                    analyzedHeader.getAdaptationFieldHeader().setAdaptationFieldOptionalFields( //analýza doplnkových polí adaptačného poľa
                                            adaptationFieldParser.analyzeAdaptationFieldOptionalFields(
                                                    analyzedHeader.getAdaptationFieldHeader(), binaryAdaptationFieldOptional, analyzedHeader.getIndex(), analyzedHeader.getPID()
                                            )
                                    );
                                    tables.updatePCRmap(analyzedHeader.getAdaptationFieldHeader().getOptionalFields()); //aktualizácia tabuľky synchronizačných značiek PCR
                                    updateTables(adaptationFieldParser); //aktualizácia tabuliek vytvorených parserom adaptačých polí
                                }
                            }
                        }
                        if(isPayload(analyzedHeader.getAdaptationFieldControl())) { //ak paket obsahuje užitočné dáta
                            if (!isPATanalyzed) {
                                if (analyzedHeader.getPID() == PATpid) {    //ak obsahuje PAT tabuľku
                                    PSIparser.analyzePAT(analyzedHeader, packet); //analyzuje PAT tabuľku
                                    i = firstPosition;
                                    packetIndex = 0;
                                    isPATanalyzed = true;
                                }
                                continue;
                            }
                            if (PSIparser.isPayloadPSI(analyzedHeader.getPID())) { //ak obsahuje PSI tabuľku
                                analyzedHeader.setPayload(PSIparser.analyzePSI(analyzedHeader, packet)); //analyzuje PSI tabuľku
                                updateTables(PSIparser); //aktualizácia tabuliek vytvorených parserom PSI tabuliek
                            }
                            else if (PSIparser.isPMT(analyzedHeader.getPID())) { //ak obsahuje PMT tabuľku
                                analyzedHeader.setPayload(PSIparser.analyzePMT(analyzedHeader, packet)); //analyzuje PMT tabuľku
                                updateTables(PSIparser); //aktualizácia tabuliek vytvorených parserom PSI tabuliek
                            }
                            else {  //ak obsahuje PES paket
                                analyzedHeader.setPayload(PESparser.analyzePES(analyzedHeader, packet)); //analyzuje PES užitočné dáta
                                tables.updatePTSmap(((PES)analyzedHeader.getPayload())); //aktualizácia tabuliek vytvorených parserom PSI tabuliek
                                updateTables(PESparser); //aktualizácia tabuliek vytvorených parserom PES dát
                            }
                            packets.add(analyzedHeader); //pridá kompletne analyzovaný paket do poľa paketov
                            updateProgress(i, buffer.length); //aktualizuje okno s progresom analýzy
                        }
                    }
                    else {
                        lost++; //stráca sa konfigurácia
                        i = seekBeginning(buffer, i); //
                        if(i == nil || lost == syncLost){ // počet stratených paketov presiahol únosnú mieru
                            tables.setSynchronizationLost(true);
                            break; // koniec
                        }
                    }
                }
                tables.setPackets(packets); //uloží do tabuliek pole paketov
                return tables; //vráti tabuľky so získanými dátami
            }


            /**
             * Funkcia zráta počet paketov v transportnom toku
             *
             * @param buffer vstupný buffer transportného toku
             * @param i pozícia prvého paketu
             * @return vráti celkový počet paketov
             */
            private int sumPackets(byte[] buffer, int i) {
                int totalPackets = 0;
                for (; i < buffer.length; i += tsPacketSize) { //prechádzam transportný tok a rátam pakety
                    if (buffer[i] == syncByte) {
                        totalPackets++;
                    }
                }
                return totalPackets-1;
            }
        };
    }

    private byte[] toBinary(int source, int length) {
        byte[] binaryField = new byte[length];
        for (int index = 0; index < length; index++) {
            binaryField[length - index - 1] = getBit(source, index);
        }
        return binaryField;
    }

    private boolean isOptionalField(AdaptationFieldHeader adaptationFieldHeader) {
        if (adaptationFieldHeader.getPCRF() == 0x01 || adaptationFieldHeader.getOPCRF() == 0x01 || adaptationFieldHeader.getSplicingPointFlag() == 0x01 || adaptationFieldHeader.getAFEflag() == 0x01) {
            return true;
        }
        return false;
    }

    private void updateTables(Parser parser) {

        if(parser instanceof PESparser) {
            tables.setStreamCodes(parser.tables.getStreamCodes());
            tables.setPacketsSizeMap(parser.tables.getPacketsSizeMap());

            tables.setPTSsnapshotMap(parser.tables.getPTSsnapshotMap());
            tables.setPTSpidMap(parser.tables.getPTSpidMap());
            tables.setDTSpidMap(parser.tables.getDTSpidMap());

            tables.setPTSpacketMap(parser.tables.getPTSpacketMap());
            tables.setDTSpacketMap(parser.tables.getDTSpacketMap());
        }
        else if(parser instanceof PSIparser) {
            tables.setPMTnumber(parser.tables.getPMTnumber());
            tables.setPATmap(parser.tables.getPATmap());
            tables.setPMTmap(parser.tables.getPMTmap());
            tables.setESmap(parser.tables.getESmap());

            tables.setServiceNamesMap(parser.tables.getServiceNamesMap());
            tables.setPCRpmtMap(parser.tables.getPCRpmtMap());
        }
        else if(parser instanceof AdaptationFieldParser) {
            tables.setPCRpidMap(parser.tables.getPCRpidMap());
            tables.setPCRpacketMap(parser.tables.getPCRpacketMap());

            tables.setOPCRpidMap(parser.tables.getOPCRpidMap());
            tables.setOPCRpacketMap(parser.tables.getOPCRpacketMap());
        }
    }

    private int seekBeginning(byte[] buffer, int i){

        for (; i < buffer.length - tsPacketSize; i++) {
            if (buffer[i] == syncByte && buffer[i + tsPacketSize] == syncByte) {
                return i;
            }
        }
        return nil;
    }

    private boolean isPayload(Integer adaptationFieldControl) {
        return adaptationFieldControl != 2;
    }

    public Task<Tables> getTask() {
        return task;
    }
}
