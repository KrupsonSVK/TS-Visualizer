package model;

import java.math.BigInteger;
import java.util.*;

import static model.config.DVB.nil;

public class Tables {

    private HashMap<Integer, Integer> PIDmap;
    private HashMap<Integer, Integer> errorMap;
    private ArrayList<TSpacket> packets;
    private Map ESmap;
    private Map PATmap;
    private HashMap PMTmap;
    private Map timeMap;
    private Map streamCodes;
    private Map packetsSizeMap;
    private Map serviceNamesMap;
    private int PATmapVersion;
    private int PMTmapVersion;

    private static final Sorter sorter = new Sorter();


    public Tables() {
        this.streamCodes = new HashMap();
        this.timeMap = new HashMap();
        this.packetsSizeMap = new HashMap();
        this.ESmap = new HashMap();
        this.PMTmap = new HashMap();
    }

    public Tables(HashMap<Integer, Integer> PIDmap, HashMap<Integer, Integer> errorMap, ArrayList<TSpacket> packets, Map streamCodes, Map PATmap, Map timeMap, Map ESmap, HashMap PMTmap, Map serviceNamesMap) {
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
        PATmapVersion = nil;
        PMTmapVersion = nil;
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
        return sorter.sortMapToListByValue(timeMap);
    }

    public HashMap<Integer, Integer> getPIDmap() {
        return PIDmap;
    }

    public HashMap<Integer, Integer> getErrorMap() {
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

    public  HashMap<Integer, Integer> getPMTmap() {
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

    public void setPMTmap(HashMap PMTmap) {
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
}



