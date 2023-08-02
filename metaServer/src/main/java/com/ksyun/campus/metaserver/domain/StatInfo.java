package com.ksyun.campus.metaserver.domain;

import java.util.List;

public class StatInfo
{
    public String path;
    public long size;
    public long mtime;
    public FileType type;
    private List<ReplicaData> replicaData;
    public StatInfo() {}

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getMtime() {
        return mtime;
    }

    public void setMtime(long mtime) {
        this.mtime = mtime;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

}
