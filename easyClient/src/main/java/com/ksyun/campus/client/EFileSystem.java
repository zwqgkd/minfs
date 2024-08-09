package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.ClusterInfo;
import com.ksyun.campus.client.domain.StatInfo;

import java.util.List;

public class EFileSystem extends FileSystem{

    public EFileSystem() {
        this("default");
    }

    public EFileSystem(String fileSystemName) {
        this.defaultFileSystemName = fileSystemName;
    }

    public FSInputStream open(String path){
        return null;
    }
    public FSOutputStream create(String path){
        return null;
    }
    public boolean mkdir(String path){return false;}
    public boolean delete(String path){return false;}
    public StatInfo getFileStats(String path){
        return null;
    }
    public List<StatInfo> listFileStats(String path){
        return null;
    }
    public ClusterInfo getClusterInfo(){
        return null;
    }
}
