package model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static model.Config.videoType;


public class Stream {

    private final String name;
    private final String path;
    private final String size;
    private final String creationTime;
    private final String lastAccessTime;
    private final String lastModifiedTime;
    private final boolean isRegularFile;
    private final boolean readonly;
    private final String owner;
    private final int packetSize;
    private final int numOfPackets;
    private final int numOfErrors;
    private final HashMap PIDs;
    private final ArrayList packets;
    private final Map programs;
    private final Map streams;


    public Stream(String name, String path, String size, String creationTime, String lastAccessTime, String lastModifiedTime, boolean isRegularFile, boolean readonly, String owner, int packetSize, int numOfPackets, int numOfErrors, HashMap PIDs, ArrayList packets, Map programs, Map streams) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.lastModifiedTime = lastModifiedTime;
        this.isRegularFile = isRegularFile;
        this.readonly = readonly;
        this.owner = owner;
        this.packetSize = packetSize;
        this.numOfPackets = numOfPackets;
        this.numOfErrors = numOfErrors;
        this.PIDs = PIDs;
        this.packets = packets;
        this.programs = programs;
        this.streams = streams;
    }


    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }
    public String getSize() {
        return size;
    }
    public String getCreationTime() {
        return creationTime;
    }
    public String getLastAccessTime() {
        return lastAccessTime;
    }
    public String getLastModifiedTime() {
        return lastModifiedTime;
    }
    public boolean isRegularFile() {
        return isRegularFile;
    }
    public boolean isReadonly() {
        return readonly;
    }
    public String getOwner() {
        return owner;
    }
    public int getPacketSize() {
        return packetSize;
    }
    public int getNumOfPackets() {
        return numOfPackets;
    }
    public int getNumOfErrors() {
        return numOfErrors;
    }
    public HashMap getPIDs() {
        return PIDs;
    }
    public ArrayList getPackets() {
        return packets;
    }
    public Map getPrograms() {
        return programs;
    }

    public int getPEScode(int pid) {
        if (this.streams.get(pid) == null) {
            return videoType;
        }
        return (int) this.streams.get(pid);
    }
}