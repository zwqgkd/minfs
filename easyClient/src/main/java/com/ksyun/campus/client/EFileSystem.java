package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.ClusterInfo;
import com.ksyun.campus.client.domain.StatInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

@Slf4j
public class EFileSystem extends FileSystem{

    public EFileSystem() {
        this("default");
    }

    public EFileSystem(String fileSystemName) {
        this.fileSystemName = fileSystemName;
    }

    public FSInputStream open(String path) throws Exception {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (getFileStats(path) == null) {
            throw new IOException("File does not exist!");
        }

        String url = zkUtil.getMasterMetaAddress();
        ResponseEntity<String> open = sendGetRequest(url, "open", path, String.class);
        FSInputStream fsInputStream = new FSInputStream(path, this, open.getBody());
        return fsInputStream;
    }

    public FSOutputStream create(String path) throws Exception {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (getFileStats(path) != null) {
            throw new IOException("File has already existed!");
        }

        String url = zkUtil.getMasterMetaAddress();
        HttpStatus status = sendGetRequest(url, "create", path, Void.class).getStatusCode();
        if(status != HttpStatus.OK) {
            return null;
        }
        FSOutputStream fsOutputStream = new FSOutputStream(path, this);
        return fsOutputStream;
    }

    public boolean mkdir(String path) throws Exception {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (getFileStats(path) != null) {
            throw new IOException("Dir has already existed!");
        }

        String url = zkUtil.getMasterMetaAddress();
        HttpStatus status = this.sendGetRequest(url, "mkdir", path, Void.class).getStatusCode();
        return status == HttpStatus.OK ? true : false;
    }

    public boolean delete(String path) throws Exception {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (getFileStats(path) == null) {
            throw new IOException("File or dir does not exist!");
        }

        String url = zkUtil.getMasterMetaAddress();
        HttpStatus status = sendGetRequest(url, "delete", path, Void.class).getStatusCode();
        return status == HttpStatus.OK ? true : false;
    }

    /**
     * @param path 文件路径
     * @return 返回文件的元数据信息
     */
    public StatInfo getFileStats(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        try{
            return sendGetRequest(zkUtil.getMasterMetaAddress(), "stats", path, StatInfo.class).getBody();
        }catch (Exception e){
            log.error("get file stats error",e);
            return null;
        }
    }

    /**
     * @param path 文件路径
     * @return 返回文件夹下的文件元数据信息
     */
    public List<StatInfo> listFileStats(String path){
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        try{
            return sendGetRequest(zkUtil.getMasterMetaAddress(), "listdir", path, new ParameterizedTypeReference<List<StatInfo>>(){}).getBody();
        }catch(Exception e){
            log.error("list file stats error",e);
            return null;
        }
    }

    /**
     * @return 返回集群信息
     */
    public ClusterInfo getClusterInfo(){
        try{
            return zkUtil.getClusterInfo();
        }catch(Exception e) {
            log.error("get cluster info error", e);
            return null;
        }
    }
}
