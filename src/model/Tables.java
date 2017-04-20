package model;

import java.math.BigInteger;
import java.util.*;

import static model.Sorter.sortMapToListByValue;
import static model.config.DVB.nil;

public class Tables {

    private ArrayList<TSpacket> packets;

    private Map PIDmap;
    private Map programMap;
    private Map errorMap;
    private Map ESmap;
    private Map PATmap;
    private Map PMTmap;
    private Map timeMap;
    private Map streamCodes;
    private Map packetsSizeMap;
    private Map PCRmap;
    private Map serviceNamesMap;
    private Map bitrateMap;

    private int PATmapVersion;
    private int PMTmapVersion;


    public Tables() {
        this.PIDmap = new HashMap<>();
        this.streamCodes = new HashMap();
        this.timeMap = new HashMap();
        this.packetsSizeMap = new HashMap();
        this.ESmap = new HashMap();
        this.PMTmap = new HashMap();
        this.PCRmap = new HashMap();
        this.programMap = new HashMap();
        this.bitrateMap = new HashMap();

        PATmapVersion = nil;
        PMTmapVersion = nil;
    }

    public Tables(Map errorMap, ArrayList<TSpacket> packets, Map streamCodes, Map PATmap, Map timeMap, Map ESmap, Map PMTmap, Map serviceNamesMap, Map PIDmap, Map PCRmap, Map programMap, Map bitrateMap) {
        this.PIDmap = PIDmap;
        this.errorMap = errorMap;
        this.packets = packets;
        this.streamCodes = streamCodes;
        this.PATmap = PATmap;
        this.timeMap = timeMap;
        this.packetsSizeMap = timeMap;
        this.ESmap = ESmap;
        this.PMTmap = PMTmap;
        this.serviceNamesMap = serviceNamesMap;
        this.PCRmap = PCRmap;
        this.programMap = programMap;
        this.bitrateMap = bitrateMap;
    }


    public void updatePAT(Map PATmap, short versionNum) {
        if(versionNum > PATmapVersion) {
            this.PATmap = PATmap;
            PATmapVersion = versionNum;
        }
    }


    public void updateESmap(HashMap ESmap, byte versionNum) {
//        if (versionNum > PMTmapVersion) {
        Map newMap = new HashMap();
        newMap.putAll(this.ESmap);
        newMap.putAll(ESmap);
        this.ESmap = newMap;
//            PMTmapVersion = versionNum;
//        }
    }

    public void updatePMT(HashMap PMTmap, byte versionNum) {
//        if (versionNum > PMTmapVersion) {
        HashMap newMap = new HashMap();
        newMap.putAll(this.PMTmap);
        newMap.putAll(PMTmap);
        this.PMTmap = newMap;
//            PMTmapVersion = versionNum;
//        }
    }


    public void updateServiceName(int PID, String serviceName) {
        this.serviceNamesMap.put(PID,serviceName);
    }


    public Map getProgramMap() {
        HashMap<Integer, String> outputMap = new HashMap<>();
        HashMap<Integer, Integer> inputMap = (HashMap<Integer, Integer>) PMTmap;
        Set<Integer> keys = inputMap.keySet(); // The set of keys in the map.

        for (Integer key : keys) {
            Integer value = inputMap.get(key);
            outputMap.put(value, Integer.toString(value));
        }
        return outputMap;
    }


    public void updatePCRmap(AdaptationFieldOptionalFields optionalFields) {
        if(optionalFields != null){
            if(optionalFields.getPCR() != nil){
                //TODO create map(index,optionalFields.getPCRtimestamp())
                PCRmap.put(PCRmap.size()+1L, new HashMap(PIDmap));
            }

        }
    }


    public void updatePIDmap(int PID) {
        Integer value = (Integer) PIDmap.get(PID);
        value = (value == null) ? 1 : value + 1;
        PIDmap.put(PID, value);
    }


    public void updateBitrateTable() {
        bitrateMap.put(bitrateMap.size()+1, new HashMap(PIDmap));
    }


    public void updatePacketsSizeMap(Integer PID, Integer size) {
        packetsSizeMap.put(PID, size);
    }

    public void updateStreamCodes(Integer PID, Integer streamID){
        streamCodes.putIfAbsent(PID, streamID);
    }

    public void updateTimeMap(Integer PID, BigInteger timestamp){
        timeMap.put(PID, timestamp);
    }


    public Map getTimeMap() {
        return timeMap;
    }

    public List getTimeListSorted() {
        return sortMapToListByValue(timeMap);
    }

    public Map getPIDmap() {
        return PIDmap;
    }

    public Map getErrorMap() {
        return errorMap;
    }

    public ArrayList<TSpacket> getPackets() {
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

    public Map getPCRmap() {
        return PCRmap;
    }

    public Map getBitrateMap() {
        return bitrateMap;
    }


    public void setPIDmap(HashMap<Integer, Integer> PIDmap) {
        this.PIDmap = PIDmap;
    }

    public void setErrorMap(HashMap<Integer, Integer> errorMap) {
        this.errorMap = errorMap;
    }

    public void setPackets(ArrayList<TSpacket> packets) {
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

    public void setTimeMap(Map timeMap) {
        this.timeMap = timeMap;
    }

    public void setStreamCodes(Map streamCodes) {
        this.streamCodes = streamCodes;
    }

    public void setPacketsSizeMap(Map packetsSizeMap) {
        this.packetsSizeMap = packetsSizeMap;
    }

    public void setPATmapVersion(int PATmapVersion) {
        this.PATmapVersion = PATmapVersion;
    }

    public void setPMTmapVersion(int PMTmapVersion) {
        this.PMTmapVersion = PMTmapVersion;
    }

    public void setProgramMap(Map programMap) {
        this.programMap = programMap;
    }
}



