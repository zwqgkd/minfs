package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.ClusterInfo;
import com.ksyun.campus.client.domain.StatInfo;
import org.springframework.http.HttpStatus;

import java.util.List;

public class EFileSystem extends FileSystem{

    public EFileSystem() {
        this("default");
    }

    public EFileSystem(String fileSystemName) {
        this.fileSystemName = fileSystemName;
    }

    public FSInputStream open(String path){
        return null;
    }

    public FSOutputStream create(String path){


        String url = acquireIpAddress("metaServer");
        HttpStatus status = sendGetRequest(url, path, "create").getStatusCode();
        if(status != HttpStatus.OK) {
            return null;
        }
        FSOutputStream fsOutputStream = new FSOutputStream(path, this);
        return fsOutputStream;
    }

    public boolean mkdir(String path) {
        String url = zkUtil.getMasterMetaDataServerUrl();
        HttpStatus status = this.sendGetRequest(url, path, "mkdir").getStatusCode();
        return status == HttpStatus.OK ? true : false;
    }

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
