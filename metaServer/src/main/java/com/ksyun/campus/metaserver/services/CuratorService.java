package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.metaserver.domain.DataServerInfo;
import com.ksyun.campus.metaserver.domain.StatInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CuratorService {

//    @Value("${spring.zookeeper-address.master-meta}")
//    private String masterAddress;
//
//    @Value("${spring.zookeeper-address.slave-meta}")
//    private String slaveAddress;

    @Value("${spring.zookeeper-address.master-meta}")
    private String masterAddress="localhost:2182";

    @Value("${spring.zookeeper-address.slave-meta}")
    private String slaveAddress="localhost:2183";

    private final CuratorFramework curatorMetaClient;

    private static final String DS_ZK_PATH = "/dataServer";

    private static final String FS_ZK_PATH = "/fileSystem";

    @Autowired
    public CuratorService(RegistService registService) {
        String metaAddr = registService.getRole().equals("master") ? masterAddress : slaveAddress;
        //重试策略：初始sleep时间1s，最大重试3次
        ExponentialBackoffRetry backOff = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(metaAddr)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(backOff)
                .build();
        client.start();
        this.curatorMetaClient = client;
    }

    /**
     * 获取所有ds的信息
     *
     * @return ds列表
     */
    public List<DataServerInfo> getAllDataServerInfo() throws Exception {
        List<DataServerInfo> dsList = new ArrayList<>();
        curatorMetaClient.getChildren().forPath(DS_ZK_PATH).forEach(child -> {
            try {
                byte[] data = curatorMetaClient.getData().forPath(DS_ZK_PATH + "/" + child);
                ObjectMapper mapper = new ObjectMapper();
                DataServerInfo ds = mapper.readValue(data, DataServerInfo.class);
                dsList.add(ds);
            } catch (Exception e) {
                log.error("get data server info error", e);
                throw new RuntimeException(e);
            }
        });
        return dsList;
    }

    /**
     * 保存元数据
     *
     * @param fileSystemName 文件系统名称
     * @param statInfo       元数据
     */
    public void saveMetaData(String fileSystemName, StatInfo statInfo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(statInfo);
            if (curatorMetaClient.checkExists().forPath(FS_ZK_PATH + "/" + fileSystemName + statInfo.getPath()) == null)
                curatorMetaClient.create().creatingParentsIfNeeded()
                        .forPath(FS_ZK_PATH + "/" + fileSystemName + statInfo.getPath(), json.getBytes());
            else
                curatorMetaClient.setData().forPath(FS_ZK_PATH + "/" + fileSystemName + statInfo.getPath(), json.getBytes());
        } catch (Exception e) {
            log.error("save meta data error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除元数据
     *
     * @param fileSystemName 文件系统名称
     * @param path           文件路径
     */
    public void deleteMetaData(String fileSystemName, String path) {
        try {
            curatorMetaClient.delete().forPath(FS_ZK_PATH + "/" + fileSystemName + path);
        } catch (Exception e) {
            log.error("delete meta data error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件元数据
     *
     * @param fileSystemName 文件系统名称
     * @param path           文件路径
     * @return 文件元数据
     */
    public StatInfo getStatInfo(String fileSystemName, String path) {
        try {
            byte[] data = curatorMetaClient.getData().forPath(FS_ZK_PATH + "/" + fileSystemName + path);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(data, StatInfo.class);
        } catch (Exception e) {
            // todo: 改一下，一直报错误码
            log.info("get file:{} stat info null",path,e);
            return null;
        }
    }

    /**
     * 获得文件夹下一层的元数据
     *
     * @param fileSystemName 文件系统名称
     * @param path           文件夹路径
     * @return 文件夹下一层的元数据(文件及文件夹)
     */
    public List<StatInfo> getChildren(String fileSystemName, String path) {
        List<StatInfo> statInfoList = new ArrayList<>();
        try {
            curatorMetaClient.getChildren().forPath(FS_ZK_PATH + "/" + fileSystemName + path).forEach(child -> {
                try {
                    byte[] data = curatorMetaClient.getData().forPath(FS_ZK_PATH + "/" + fileSystemName + path + "/" + child);
                    ObjectMapper mapper = new ObjectMapper();
                    StatInfo statInfo = mapper.readValue(data, StatInfo.class);
                    statInfoList.add(statInfo);
                } catch (Exception e) {
                    log.error("list dir:{} children error", path, e);
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
