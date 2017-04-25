package model;

import model.packet.AdaptationFieldOptionalFields;
import model.packet.Packet;
import model.pes.PES;

import java.util.*;

import static model.config.MPEG.nil;

public class Tables {

    private ArrayList<Packet> packets;

    private Map PIDmap;
    private Map errorMap;
    private Map programMap;

    private Map ESmap;
    private Map PATmap;
    private Map PMTmap;

    private Map streamCodes;
    private Map packetsSizeMap;
    private Map serviceNamesMap;

    private Map PCRpmtMap;
    private Map PCRpacketMap;
    private Map OPCRpacketMap;
    private Map PTSpacketMap;
    private Map DTSpacketMap;

    private Map PTSpidMap;
    private Map DTSpidMap;
    private Map PTSpidDurationMap;
    private Map PTSsizeMap;
    private Map PTSdurationMap;

    private Map PCRpidMap;
    private Map OPCRpidMap;
    private Map PCRpidDurationMap;
    private Map PCRdurationMap;
    private Map PCRsizeMap;

    private Map indexSnapshotMap;
    private Map PCRsnapshotMap;
    private Map PTSsnapshotMap;
    private Map bitrateMap;
    private Map minBitrateMap;
    private Map avgPCRBitrateMap;
    private Map avgPTSBitrateMap;
    private Map maxBitrateMap;

    private Map serviceTimestampMap;


    public Tables() {
        this.PIDmap = new HashMap<>();
        this.errorMap = new HashMap<>();
        this.programMap = new HashMap();

        this.ESmap = new HashMap();
        this.PATmap = new HashMap();
        this.PMTmap = new HashMap();

        this.PCRpmtMap = new HashMap();
        this.PCRpacketMap = new HashMap();
        this.OPCRpacketMap = new HashMap();
        this.PTSpacketMap = new HashMap();
        this.DTSpacketMap = new HashMap();

        this.DTSpidMap = new LinkedHashMap();
        this.PTSpidMap = new LinkedHashMap();
        this.OPCRpidMap = new LinkedHashMap();
        this.PCRpidMap = new LinkedHashMap();

        this.PCRsnapshotMap = new HashMap();
        this.PTSsnapshotMap = new HashMap();
        this.PCRpidDurationMap = new HashMap();
        this.PTSpidDurationMap = new HashMap();
        this.PCRdurationMap = new HashMap();
        this.PCRsizeMap = new HashMap();
        this.PTSdurationMap = new HashMap();
        this.PTSsizeMap = new HashMap();

        this.streamCodes = new HashMap();
        this.packetsSizeMap = new HashMap();
        this.serviceNamesMap = new HashMap();

        this.indexSnapshotMap = new HashMap();
        this.bitrateMap = new HashMap();
        this.minBitrateMap = new HashMap();
        this.avgPCRBitrateMap = new HashMap();
        this.avgPTSBitrateMap = new HashMap();
        this.maxBitrateMap = new HashMap();
    }


    public void updateESmap(HashMap ESmap, byte versionNum) {
        Map newMap = new HashMap();
        newMap.putAll(this.ESmap);
        newMap.putAll(ESmap);
        this.ESmap = newMap;
    }


    public void updatePMT(HashMap PMTmap, byte versionNum) {
        HashMap newMap = new HashMap();
        newMap.putAll(this.PMTmap);
        newMap.putAll(PMTmap);
        this.PMTmap = newMap;
    }

//
//    public Map createProgramMap() {
//        HashMap<Integer, String> outputMap = new HashMap<>();
//        HashMap<Integer, Integer> inputMap = (HashMap<Integer, Integer>) PMTmap;
//        Set<Integer> keys = inputMap.keySet(); // The set of keys in the map.
//
//        for (Integer key : keys) {
//            Integer value = inputMap.get(key);
//            outputMap.put(value, "Service: " + Integer.toString(value));
//        }
//        return outputMap;
//    }
//

    public void updatePCRmap(AdaptationFieldOptionalFields optionalFields) {
        if(optionalFields != null){
            if(optionalFields.getPCR() != nil){
                PCRsnapshotMap.put(optionalFields.getPCR(), new HashMap(PIDmap));
            }
        }
    }

    public void updatePTSmap( PES header) {
        if(header != null){
            if(header.getPTSdtsFlags() > 0){
                PTSsnapshotMap.put(header.getPTS(), new HashMap(PIDmap));
            }
        }
    }


    public void updatePIDmap(int PID) {
        Integer value = (Integer) PIDmap.get(PID);
        value = (value == null) ? 1 : value + 1;
        PIDmap.put(PID, value);
    }


    public void updateBitrateMap(int PID, long bitrate) {
        if(bitrateMap.get(PID) == null){
            List<Long> bitrates = new ArrayList();
            bitrates.add(bitrate);
            bitrateMap.put(PID,bitrates);
        }
        List bitrates = (ArrayList<Long>) bitrateMap.get(PID);
        bitrates.add(bitrate);
        bitrateMap.put(PID,bitrates);
    }


    public void updateMinBitrateMap(int PID, long bitrate) {
        if(minBitrateMap.get(PID) == null){
            minBitrateMap.put(PID,bitrate);
        }
        Long minBitrate = (Long) minBitrateMap.get(PID);
        if(bitrate < minBitrate) {
            minBitrateMap.put(PID, bitrate);
        }
    }


    public void updateMaxBitrateMap(int PID, long bitrate) {
        if(maxBitrateMap.get(PID) == null){
            maxBitrateMap.put(PID,bitrate);
        }
        Long maxBitrate = (Long) maxBitrateMap.get(PID);
        if(bitrate > maxBitrate) {
            maxBitrateMap.put(PID, bitrate);
        }
    }


    public void updatePCRsizeMap(Integer PID, long size) {
        if (PCRsizeMap.get(PID) != null) {
            Long totalSize = (Long) PCRsizeMap.get(PID);
            size += totalSize;
        }
        PCRsizeMap.put(PID,size);
    }


    public void updatePCRdurationMap(Integer PID, long duration) {
        if (PCRdurationMap.get(PID) != null) {
            Long totalSize = (Long) PCRdurationMap.get(PID);
            duration += totalSize;
        }
        PCRdurationMap.put(PID,duration);
    }


    public void updatePTSsizeMap(Integer PID, long size) {
        if (PTSsizeMap.get(PID) != null) {
            Long totalSize = (Long) PTSsizeMap.get(PID);
            size += totalSize;
        }
        PTSsizeMap.put(PID,size);
    }


    public void updatePTSdurationMap(Integer PID, long duration) {
        if (PTSdurationMap.get(PID) != null) {
            Long totalSize = (Long) PTSdurationMap.get(PID);
            duration += totalSize;
        }
        PTSdurationMap.put(PID,duration);
    }


    public void updatePCRpidDurationMap(Integer PID, long duration) {
        if(PCRpidDurationMap.get(PID) == null) {
            if (duration > 0) {
                PCRpidDurationMap.put(PID,duration);
            }
        }
        else if(duration > (Long)PCRpidDurationMap.get(PID)) {
            PCRpidDurationMap.put(PID,duration);
        }
    }

    public void updatePTSpidDurationMap(Integer PID, long duration) {
        if(PTSpidDurationMap.get(PID) == null) {
            if (duration > 0) {
                PTSpidDurationMap.put(PID,duration);
            }
        }
        else if(duration > (Long)PTSpidDurationMap.get(PID)) {
            PTSpidDurationMap.put(PID,duration);
        }
    }


    public void updatePCRpmtMap(int PID, short PCRpid) {
        PCRpmtMap.put(PID,PCRpid);
    }

    public void updatePTSpacketMap(long timeStamp, long index) {
        PTSpacketMap.put(timeStamp,index);
    }

    public void updateDTSpacketMap(long timeStamp, long index) {
        DTSpacketMap.put(timeStamp,index);
    }

    public void updatePCRpacketMap(long timeStamp, long index) {
        PCRpacketMap.put(timeStamp,index);
    }

    public void updateOPCRpacketMap(long timeStamp, long index) {
        OPCRpacketMap.put(timeStamp,index);
    }

    public void updateAvgPCRBitrateMap(Integer PID, long avgBitrate) {
        avgPCRBitrateMap.put(PID, avgBitrate);
    }

    public void updateAvgPTSBitrateMap(Integer PID, long avgBitrate) {
        avgPTSBitrateMap.put(PID, avgBitrate);
    }


    public void updatePAT(Map PATmap, short versionNum) {
        this.PATmap = PATmap;
    }

    public void updateServiceName(int PID, String serviceName) {
        this.serviceNamesMap.put(PID,serviceName);
    }

    public void updatePCRpidMap(long pcr, int pid) {
        PCRpidMap.put(pcr, pid);
    }

    public void updateOPCRpidMap(long opcr, int pid) {
        OPCRpidMap.put(opcr, pid);
    }

    public void updateIndexSnapshotMap() {
        indexSnapshotMap.put(indexSnapshotMap.size(), new HashMap(PIDmap));
    }

    public void updatePacketsSizeMap(Integer PID, Integer size) {
        packetsSizeMap.put(PID, size);
    }

    public void updateStreamCodes(Integer PID, Integer streamID){
        streamCodes.putIfAbsent(PID, streamID);
    }

    public void updatePTSpidMap(Integer PID, long timestamp){
        PTSpidMap.put(timestamp,PID);
    }

    public void updateDTSpidMap(Integer PID, long timestamp){
        DTSpidMap.put(timestamp,PID);
    }



    public Map getProgramMap() {
        return programMap;
    }

    public Map getBitrateMap() {
        return bitrateMap;
    }

    public Map getPCRdurationMap() {
        return PCRdurationMap;
    }

    public Map getPCRpidMap() {
        return PCRpidMap;
    }

    public Map getMinBitrateMap() {
        return minBitrateMap;
    }

    public Map getAvgPCRBitrateMap() {
        return avgPCRBitrateMap;
    }

    public Map getMaxBitrateMap() {
        return maxBitrateMap;
    }

    public Map getPIDmap() {
        return PIDmap;
    }

    public Map getErrorMap() {
        return errorMap;
    }

    public ArrayList<Packet> getPackets() {
        return packets;
    }

    public Map getPATmap() {
        return PATmap;
    }

    public Map getStreamCodes() {
        return streamCodes;
    }

    public Map getPMTmap() {
        return PMTmap;
    }

    public HashMap getPacketsSizeMap() {
        return (HashMap) packetsSizeMap;
    }

    public Map getESmap() {
        return ESmap;
    }

    public Map getServiceNamesMap() {
        return serviceNamesMap;
    }

    public Map getPCRsnapshotMap() {
        return PCRsnapshotMap;
    }

    public Map getIndexSnapshotMap() {
        return indexSnapshotMap;
    }

    public Map getPCRpidDurationMap() {
        return PCRpidDurationMap;
    }

    public Map getPTSpidDurationMap() {
        return PTSpidDurationMap;
    }

    public Map getPCRsizeMap() {
        return PCRsizeMap;
    }

    public Map getPCRpmtMap() {
        return PCRpmtMap;
    }

    public Map getDTSpidMap() {
        return DTSpidMap;
    }

    public Map getOPCRpidMap() {
        return OPCRpidMap;
    }

    public Map getPCRpacketMap() {
        return PCRpacketMap;
    }

    public Map getOPCRpacketMap() {
        return OPCRpacketMap;
    }

    public Map getPTSpacketMap() {
        return PTSpacketMap;
    }

    public Map getDTSpacketMap() {
        return DTSpacketMap;
    }

    public Map getPTSpidMap() {
        return PTSpidMap;
    }


    public void setPIDmap(Map PIDmap) {
        this.PIDmap = PIDmap;
    }

    public void setErrorMap(Map errorMap) {
        this.errorMap = errorMap;
    }

    public void setServiceNamesMap(Map serviceNamesMap) {
        this.serviceNamesMap = serviceNamesMap;
    }

    public void setPCRpmtMap(Map PCRpmtMap) {
        this.PCRpmtMap = PCRpmtMap;
    }

    public void setPCRpacketMap(Map PCRpacketMap) {
        this.PCRpacketMap = PCRpacketMap;
    }

    public void setOPCRpacketMap(Map OPCRpacketMap) {
        this.OPCRpacketMap = OPCRpacketMap;
    }

    public void setPTSpacketMap(Map PTSpacketMap) {
        this.PTSpacketMap = PTSpacketMap;
    }

    public void setDTSpacketMap(Map DTSpacketMap) {
        this.DTSpacketMap = DTSpacketMap;
    }

    public void setPCRpidMap(Map PCRpidMap) {
        this.PCRpidMap = PCRpidMap;
    }

    public void setOPCRpidMap(Map OPCRpidMap) {
        this.OPCRpidMap = OPCRpidMap;
    }

    public void setPCRpidDurationMap(Map PCRpidDurationMap) {
        this.PCRpidDurationMap = PCRpidDurationMap;
    }

    public void setPCRdurationMap(Map PCRdurationMap) {
        this.PCRdurationMap = PCRdurationMap;
    }

    public void setPCRsizeMap(Map PCRsizeMap) {
        this.PCRsizeMap = PCRsizeMap;
    }

    public void setIndexSnapshotMap(Map indexSnapshotMap) {
        this.indexSnapshotMap = indexSnapshotMap;
    }

    public void setPCRsnapshotMap(Map PCRsnapshotMap) {
        this.PCRsnapshotMap = PCRsnapshotMap;
    }

    public void setBitrateMap(Map bitrateMap) {
        this.bitrateMap = bitrateMap;
    }

    public void setMinBitrateMap(Map minBitrateMap) {
        this.minBitrateMap = minBitrateMap;
    }

    public void setAvgPCRBitrateMap(Map avgPCRBitrateMap) {
        this.avgPCRBitrateMap = avgPCRBitrateMap;
    }

    public void setMaxBitrateMap(Map maxBitrateMap) {
        this.maxBitrateMap = maxBitrateMap;
    }

    public void setPIDmap(HashMap<Integer, Integer> PIDmap) {
        this.PIDmap = PIDmap;
    }

    public void setErrorMap(HashMap<Integer, Integer> errorMap) {
        this.errorMap = errorMap;
    }

    public void setPackets(ArrayList<Packet> packets) {
        this.packets = packets;
    }

    public void setPATmap(HashMap PATmap) {
        this.PATmap = PATmap;
    }

    public void setESmap(Map ESmap) {
        this.ESmap = ESmap;
    }

    public void setPATmap(Map PATmap) {
        this.PATmap = PATmap;
    }

    public void setPMTmap(Map PMTmap) {
        this.PMTmap = PMTmap;
    }

    public void setPTSpidMap(Map PTSpidMap) {
        this.PTSpidMap = PTSpidMap;
    }

    public void setDTSpidMap(Map DTSpidMap) {
        this.DTSpidMap = DTSpidMap;
    }

    public void setStreamCodes(Map streamCodes) {
        this.streamCodes = streamCodes;
    }

    public void setPacketsSizeMap(Map packetsSizeMap) {
        this.packetsSizeMap = packetsSizeMap;
    }

    public void setProgramMap(Map programMap) {
        this.programMap = programMap;
    }

    public void setServiceTimestampMap(Map serviceTimestampMap) {
        this.serviceTimestampMap = serviceTimestampMap;
    }

    public Map getServiceTimestampMap() {
        return serviceTimestampMap;
    }

    public Map getPTSsizeMap() {
        return PTSsizeMap;
    }

    public Map getPTSdurationMap() {
        return PTSdurationMap;
    }

    public Map getAvgPTSBitrateMap() {
        return avgPTSBitrateMap;
    }

    public Map getPTSsnapshotMap() {
        return PTSsnapshotMap;
    }

    public void setPTSsnapshotMap(Map PTSsnapshotMap) {
        this.PTSsnapshotMap = PTSsnapshotMap;
    }
}



