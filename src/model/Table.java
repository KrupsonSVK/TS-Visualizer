package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static app.Config.nil;

public class Table {

    private HashMap<Integer, Integer> PIDmap;
    private HashMap<Integer, Integer> errorMap;
    private ArrayList<TSpacket> packets;
    private Map PATmap, PMTmap;
    private int PATmapVersion = nil;
    private int PMTmapVersion = nil;
    private Map streams;

    public Table() {
        this.streams = new HashMap();
    }

    public Table(HashMap<Integer, Integer> PIDmap, HashMap<Integer, Integer> errorMap, ArrayList<TSpacket> packets, Map streams, Map PATmap) {
        this.PIDmap = PIDmap;
        this.errorMap = errorMap;
        this.packets = packets;
        this.streams = streams;
        this.PATmap = PATmap;
    }




    public HashMap<Integer, Integer> getPIDmap() {
        return PIDmap;
    }
    public void setPIDmap(HashMap<Integer, Integer> PIDmap) {
        this.PIDmap = PIDmap;
    }

    public HashMap<Integer, Integer> getErrorMap() {
        return errorMap;
    }
    public void setErrorMap(HashMap<Integer, Integer> errorMap) {
        this.errorMap = errorMap;
    }

    public ArrayList<TSpacket> getPackets() {
        return packets;
    }
    public void setPackets(ArrayList<TSpacket> packets) {
        this.packets = packets;
    }

    public Map getPATmap() {
        return PATmap;
    }
    public void setPATmap(HashMap PATmap) {
        this.PATmap = PATmap;
    }

    public void updateStream(Map PATmap, short versionNum) {

        if(versionNum > PATmapVersion) {
            this.PATmap = PATmap;
            PATmapVersion = versionNum;
        }
    }

    public void updateStreamPMT(HashMap PMTmap, byte versionNum) {
        if(versionNum > PMTmapVersion) {
            this.PMTmap = PMTmap;
            PMTmapVersion = versionNum;
        }
    }

    public void updateStreamCodes(Integer PID, Integer streamID) {
        if(this.streams.get(PID) == null) {
            this.streams.put(PID,streamID);
        }
    }

    public Map getStreams() {
        return streams;
    }
}


