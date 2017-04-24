package app.streamAnalyzer;


import javafx.concurrent.Task;
import model.Stream;
import model.Tables;
import model.config.MPEG;
import model.packet.Packet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static model.config.MPEG.TimestampType.*;
import static model.config.MPEG.fieldPresent;
import static model.config.MPEG.nil;
import static model.config.MPEG.tsPacketSize;

public class StreamAnalyzer {

    private Task<Stream> task;

    public StreamAnalyzer(Task task) {
        this.task = task;
    }


    public void analyzeStream(File file, Tables tables) throws IOException {

        this.task = new Task<Stream>() {
            @Override
            public Stream call() throws InterruptedException, IOException {

                Integer packets = 0, errors = 0;
                BasicFileAttributes attr = Files.readAttributes(Paths.get(file.getAbsolutePath()), BasicFileAttributes.class);

                String size;
                if (attr.size() > 1000000) {
                    size = String.format("%.2f MB", (double) attr.size() / (double) 1000000);
                } else if (attr.size() > 1000) {
                    size = String.format("%.2f kB", (double) attr.size() / (double) 1000);
                } else {
                    size = String.format("%.2f Bytes", (double) attr.size());
                }
                for (Object value : tables.getPIDmap().values()) {
                    packets += (Integer) value; //TODO remove redundant
                }
                for (Integer value : ((HashMap<Integer, Integer>) tables.getErrorMap()).values()) {
                    errors += value;
                }
                tables.setProgramMap(createPrograms(tables.getPMTmap()));

                //createPCRmap(tables);
                tables.setServiceTimestampMap(createServiceTimestampMap(tables, tables.getPATmap(), tables.getPMTmap(), tables.getPCRpidMap(),tables.getPCRpmtMap()));
                analyzeBitrate(tables, tables.getPIDmap(), tables.getPCRsnapshotMap(), tables.getPCRpidMap());
                long duration = calculateDuration(tables.getPCRpidDurationMap());
                long bitrate = calculateBitrate(tables.getAvgBitrateMap());
                return new Stream(
                        file.getName(),
                        file.getAbsolutePath(),
                        size,
                        attr.creationTime().toString(),
                        attr.lastAccessTime().toString(),
                        attr.lastModifiedTime().toString(),
                        attr.isRegularFile(),
                        file.canWrite(),
                        Files.getOwner(file.toPath()).toString(),
                        duration,
                        bitrate,
                        StreamParser.getTsPacketSize(),
                        packets,
                        errors,
                        tables
                );
            }
        };
    }



    private Map createServiceTimestampMap(Tables tables, Map<Integer, Integer> PATmap, Map<Integer, Integer> PMTmap, Map<Long, Integer> PCRpidMaps, Map<Long, Integer> PCRpmtMap) {

        Map<MPEG.TimestampType, Map<Long, Integer>> timestampPIDmaps = new HashMap();
        timestampPIDmaps.put(PCR, tables.getPCRpidMap());
        timestampPIDmaps.put(OPCR, tables.getOPCRpidMap());
        timestampPIDmaps.put(PTS, tables.getPTSpidMap());
        timestampPIDmaps.put(DTS, tables.getDTSpidMap());

        Map<MPEG.TimestampType, Map<Long, Long>> timestampPacketMaps = new HashMap();
        timestampPacketMaps.put(PCR, tables.getPCRpacketMap());
        timestampPacketMaps.put(OPCR, tables.getOPCRpacketMap());
        timestampPacketMaps.put(PTS, tables.getPTSpacketMap());
        timestampPacketMaps.put(DTS, tables.getDTSpacketMap());


        Map serviceTimetampMap = new HashMap<Integer, Map>();

        for (Integer program : ((Map<Integer, String>)tables.getProgramMap()).keySet() ) {
            for (Map.Entry<Integer, Integer> service : PATmap.entrySet()) {

                Integer serviceNum = service.getKey();
                if (program.equals(serviceNum)) {
                    Map timestampTypeMap = new HashMap<MPEG.TimestampType, Map>();

                    for (Map.Entry<MPEG.TimestampType, Map<Long, Integer>> timestampPIDmap : timestampPIDmaps.entrySet()) {
                        MPEG.TimestampType type = timestampPIDmap.getKey();
                        Map timestampPositionMap = new HashMap<Long, Long>();

                        Integer servicePCRpid = null;
                        Integer servicePID = service.getValue();
                        if (tables.getPCRpmtMap().get(servicePID) != null) {
                            servicePCRpid = Integer.valueOf((short) tables.getPCRpmtMap().get(servicePID));
                        }
                        for (Map.Entry<Long, Integer> timestampPIDentry : timestampPIDmap.getValue().entrySet()) {
                            Long timestamp = timestampPIDentry.getKey();
                            Integer PID = timestampPIDentry.getValue();
                            if (servicePCRpid != null && servicePCRpid.equals(PID)) {
                                for (Map.Entry<MPEG.TimestampType, Map<Long, Long>> timestampPacketMap : timestampPacketMaps.entrySet()) {
                                    Long position = timestampPacketMap.getValue().get(timestamp);
                                    if(position != null) {
                                        timestampPositionMap.put(timestamp, position);
                                    }
                                }
                            }
                            if (type.equals(PTS) || type.equals(DTS)) {
                                for (Map.Entry<Integer, Integer> pmtEntry : ((Map<Integer, Integer>) tables.getPMTmap()).entrySet()) {
                                    Integer pmtService = pmtEntry.getValue();
                                    if (serviceNum.equals(pmtService)) {
                                        Integer pmtPID = pmtEntry.getKey();
                                        if (pmtPID.equals(PID)) {
                                            for (Map.Entry<MPEG.TimestampType, Map<Long, Long>> timestampPacketMap : timestampPacketMaps.entrySet()) {
                                                Long position = timestampPacketMap.getValue().get(timestamp);
                                                if(position != null) {
                                                    timestampPositionMap.put(timestamp, position);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        timestampTypeMap.put(type, timestampPositionMap);
                    }
                    serviceTimetampMap.put(serviceNum, timestampTypeMap);
                }
            }
        }
        return serviceTimetampMap;
    }


    private void createPCRmap(Tables tables) {

        for (Packet packet : tables.getPackets()) {
            if (packet.getAdaptationFieldHeader() != null) {
                if (packet.getAdaptationFieldHeader().getPCRF() == fieldPresent) {
                    tables.updatePCRpidMap(packet.getAdaptationFieldHeader().getOptionalFields().getPCR(), packet.getPID());
                }
                if (packet.getAdaptationFieldHeader().getOPCRF() == fieldPresent) {
                    tables.updateOPCRpidMap(packet.getAdaptationFieldHeader().getOptionalFields().getOPCR(), packet.getPID());
                }
            }
        }
    }


    private long calculateBitrate(Map<Integer, Long> avgBitrateMap) {
        long size = 0L;
        for (Long avgPIDbitrate :  avgBitrateMap.values()){
            size += avgPIDbitrate;
        }
        return size;
    }


    private long calculateDuration(Map<Integer, Long> PCRdurationMap) {
        Long maxDuration = 0L;
        for (Long PIDduration :  PCRdurationMap.values()){
            if (PIDduration.compareTo(maxDuration) > 0){
                maxDuration = PIDduration;
            }
        }
        return maxDuration;
    }


    private void analyzeBitrate(Tables tables, Map<Integer, Integer> PIDmap, Map<Long, Map> PCRsnapshotMap, Map<Long, Integer> PCRpidMap) {

        for (Integer PID : PIDmap.keySet()) {
            long firstPCR = nil;
            long prevPCR = nil;
            long lastPCR = nil;

            for (Map.Entry<Long, Integer> PCRpidEntry : PCRpidMap.entrySet()) {
                long totalDuration = 0;
                if (PCRpidEntry.getValue().equals(PID)) {
                    if (firstPCR == nil) {
                        firstPCR = PCRpidEntry.getKey().longValue();
                        prevPCR = firstPCR;
                    } else {
                        lastPCR = PCRpidEntry.getKey().longValue();
                    }
                    if (prevPCR != nil && lastPCR != nil) {
                        long duration = lastPCR - prevPCR;
                        for (Integer currentPID : PIDmap.keySet()) {
                            long beginningSize = 0;
                            Map<Integer, Integer> prevMap = PCRsnapshotMap.get(prevPCR);
                            if (prevMap != null) {
                                for (Map.Entry<Integer, Integer> PIDentry : prevMap.entrySet()) {
                                    if (currentPID.equals(PIDentry.getKey())) {
                                        beginningSize += PIDentry.getValue();
                                    }
                                }
                                long endSize = 0;
                                Map<Integer, Integer> lastMap = PCRsnapshotMap.get(lastPCR);
                                if (lastMap != null) {
                                    for (Map.Entry<Integer, Integer> PIDentry : lastMap.entrySet()) {
                                        if (currentPID.equals(PIDentry.getKey())) {
                                            endSize += PIDentry.getValue();
                                        }
                                    }
                                    long size = endSize - beginningSize;
                                    long bitrate = (long) ((double) size * tsPacketSize / ((double) duration / 1000f));
                                    if (bitrate > 0) {
                                        tables.updatePCRsizeMap(currentPID, size);
                                        tables.updatePCRdurationMap(currentPID, duration);
                                        tables.updateBitrateMap(currentPID, bitrate);
                                        tables.updateMinBitrateMap(currentPID, bitrate);
                                        tables.updateMaxBitrateMap(currentPID, bitrate);
                                    }
                                }
                            }
                            totalDuration = lastPCR - firstPCR;
                        }
                    }
                    if (lastPCR != nil) {
                        prevPCR = lastPCR;
                    }
                }
                if(tables.getPCRpidDurationMap().get(PID) == null) {
                    if (totalDuration > 0) {
                        tables.updatePCRpidDurationMap(PID, totalDuration);
                    }
                }
                else if(totalDuration > (Long)tables.getPCRpidDurationMap().get(PID)) {
                    tables.updatePCRpidDurationMap(PID, totalDuration);
                }
            }
        }
        analyzeAvgBitrate(tables, tables.getPCRsizeMap(), tables.getPCRdurationMap());
    }

    private void analyzeAvgBitrate(Tables tables, Map<Integer, Long> PCRsizeMap, Map<Integer, Long> PCRdurationMap) {

        for (Map.Entry<Integer, Long> PCRsizeEntry : PCRsizeMap.entrySet()) {
            for (Map.Entry<Integer, Long> PCRdurationEntry : PCRdurationMap.entrySet()) {
                if (PCRsizeEntry.getKey().equals(PCRdurationEntry.getKey())) {
                    long avgBitrate = (long) (PCRsizeEntry.getValue() * tsPacketSize / (PCRdurationEntry.getValue() / 1000f));
                    tables.updateAvgBitrateMap(PCRsizeEntry.getKey(), avgBitrate);
                }
            }
        }
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

    public Task<Stream> getTask() {
        return task;
    }
}
