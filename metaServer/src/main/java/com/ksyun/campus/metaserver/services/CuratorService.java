package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.metaserver.domain.DataServerInfo;
import com.ksyun.campus.metaserver.domain.StatInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CuratorService {
    private final CuratorFramework curatorClient;

    private static final String DS_ZK_PATH = "/dataServer";

    private static final String FS_ZK_PATH = "/fileSystem";

    @Autowired
    public CuratorService(CuratorFramework curatorClient){
        this.curatorClient=curatorClient;
    }

    /**
     * 获取所有ds的信息
     * @return ds列表
     */
    public List<DataServerInfo> getAllDataServerInfo() throws Exception {
        List<DataServerInfo> dsList=new ArrayList<>();
        curatorClient.getChildren().forPath(DS_ZK_PATH).forEach(child->{
            try {
                byte[] data = curatorClient.getData().forPath(DS_ZK_PATH+"/"+child);
                ObjectMapper mapper = new ObjectMapper();
                DataServerInfo ds = mapper.readValue(data, DataServerInfo.class);
                dsList.add(ds);
            } catch (Exception e) {
                log.error("get data server info error",e);
                throw new RuntimeException(e);
            }
        });
        return dsList;
    }

    /**
     * 保存元数据
     * @param fileSystemName 文件系统名称
     * @param statInfo 元数据
     */
    public void saveMetaData(String fileSystemName, StatInfo statInfo){
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(statInfo);
            curatorClient.create().creatingParentsIfNeeded()
                    .forPath(FS_ZK_PATH + "/" + fileSystemName + "/" + statInfo.getPath(), json.getBytes());
        } catch (Exception e) {
            log.error("save meta data error",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除元数据
     * @param fileSystemName 文件系统名称
     * @param path 文件路径
     */
    public void deleteMetaData(String fileSystemName, String path){
        try {
            curatorClient.delete().forPath(FS_ZK_PATH + "/" + fileSystemName + path);
        } catch (Exception e) {
            log.error("delete meta data error",e);
            throw new RuntimeException(e);
        }
    }

    public StatInfo getStatInfo(String fileSystemName, String path){
        try {
            byte[] data = curatorClient.getData().forPath(FS_ZK_PATH + "/" + fileSystemName + path);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(data, StatInfo.class);
        } catch (Exception e) {
            log.error("get file:{} stat info error",path,e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得文件夹下一层的元数据
     * @param fileSystemName 文件系统名称
     * @param path 文件夹路径
     * @return 文件夹下一层的元数据(文件及文件夹)
     */
    public List<StatInfo> getChildren(String fileSystemName, String path){
        List<StatInfo> statInfoList = new ArrayList<>();
        try {
            curatorClient.getChildren().forPath(FS_ZK_PATH + "/" + fileSystemName + path).forEach(child->{
                try {
                    byte[] data = curatorClient.getData().forPath(FS_ZK_PATH + "/" + fileSystemName + path + "/" + child);
                    ObjectMapper mapper = new ObjectMapper();
                    StatInfo statInfo = mapper.readValue(data, StatInfo.class);
                    statInfoList.add(statInfo);
                } catch (Exception e) {
                    log.error("list dir:{} children error",path,e);
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.error("list dir children error", e);
            throw new RuntimeException(e);
        }
        return statInfoList;
    }

}
