package model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static model.config.DVB.videoType;


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
    private final Tables tables;


    public Stream(String name, String path, String size, String creationTime, String lastAccessTime, String lastModifiedTime, boolean isRegularFile, boolean readonly, String owner, int packetSize, int numOfPackets, int numOfErrors, Tables tables) {
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
        this.tables = tables;
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

    public Tables getTables() {
        return tables;
    }

    public int getNumOfErrors() {
        return numOfErrors;
    }

    public int getPEScode(int pid) {
        if (tables.getStreamCodes().get(pid) == null) {
            return videoType;
        }
        return (int)tables.getStreamCodes().get(pid);
    }
}