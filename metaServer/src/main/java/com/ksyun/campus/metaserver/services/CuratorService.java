package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ksyun.campus.metaserver.domain.DataServerInfo;
import com.ksyun.campus.metaserver.domain.FileType;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.domain.StatInfoWithFSName;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class CuratorService {

    private final RegistService registService;

    @Value("${spring.zookeeper-address.master-meta}")
    private String masterAddress;

    @Value("${spring.zookeeper-address.slave-meta}")
    private String slaveAddress;

    private final CuratorFramework curatorRegisterClient;

    private CuratorFramework curatorMetaClient;

    private static final String DS_ZK_PATH = "/dataServer";

    private static final String FS_ZK_PATH = "/fileSystem";

    @Autowired
    public CuratorService(RegistService registService, CuratorFramework curatorRegisterClient) {
        this.registService = registService;
        this.curatorRegisterClient = curatorRegisterClient;
    }

    @PostConstruct
    public void init() throws Exception {
        String metaAddr = registService.getRole().equals("master") ? masterAddress : slaveAddress;
        log.info("debuggggggggggggggggggggggg:role:{} metaAddr:{}",registService.getRole(),metaAddr);
        //重试策略：初始sleep时间1s，最大重试3次
        ExponentialBackoffRetry backOff = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(metaAddr)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(backOff)
                .build();
        client.start();
        //check is connected
        this.curatorMetaClient = client;
        //
        if(this.curatorMetaClient.checkExists().forPath(FS_ZK_PATH)==null)
            this.curatorMetaClient.create().forPath(FS_ZK_PATH);
    }

    private void dfs(String zkPath, List<StatInfoWithFSName> statInfoWithFSNameList, String fileSystemName){
        try{
            curatorMetaClient.getChildren().forPath(zkPath).forEach(child->{
                byte[] data= null;
                try {
                    data = curatorMetaClient.getData().forPath(zkPath+"/"+child);
                    if(data.length==0){
                        dfs(zkPath+"/"+child, statInfoWithFSNameList, child);
                    }else{
                        ObjectMapper mapper = new ObjectMapper();
                        StatInfo curStatInfo = mapper.readValue(data, StatInfo.class);
                        if(curStatInfo.getType()== FileType.File)
                            statInfoWithFSNameList.add(new StatInfoWithFSName(curStatInfo, fileSystemName));
                        else
                            dfs(zkPath+"/"+child, statInfoWithFSNameList, fileSystemName);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });
        }catch (Exception e){
            log.error("dfs error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取所有ds的信息
     *
     * @return ds列表
     */
    public List<DataServerInfo> getAllDataServerInfo() throws Exception {
        List<DataServerInfo> dsList = new ArrayList<>();
        curatorRegisterClient.getChildren().forPath(DS_ZK_PATH).forEach(child -> {
            try {
                byte[] data = curatorRegisterClient.getData().forPath(DS_ZK_PATH + "/" + child);
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

            if(this.registService.getRole().equals("master")){
                //sync metaData
                syncMetaData("saveMetaData", fileSystemName, statInfo);
            }

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
            //sync metaData
            if(this.registService.getRole().equals("master")){
                syncMetaData("deleteMetaData", fileSystemName, path);
            }
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
            log.info("get file:{} stat info null", path, e);
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

    /**
     * 获取所有文件系统的元数据
     *
     * @return 文件系统元数据列表
     */
    public List<StatInfoWithFSName> getAllFileStatInfo(){
        List<StatInfoWithFSName> statInfoWithFSNameList = new ArrayList<>();
        dfs(FS_ZK_PATH, statInfoWithFSNameList, null);
        return statInfoWithFSNameList;
    }

    /**
     * 同步元数据
     *
     * @param interfaceName 接口名称
     * @param fileSystemName 文件系统名称
     * @param param 参数
     */
    public <T> void syncMetaData(String interfaceName, String fileSystemName, T param) throws Exception {
        String slaveUrl=this.registService.getSlaveMetaAddress();
        if(slaveUrl==null){
            log.info("no slave address in zk when sync meta data");
        }else{
            log.error("slave address in zk when sync meta data:{}",slaveUrl);
//            this.httpService.sendPostRequest(slaveUrl, interfaceName, fileSystemName, param);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers= new HttpHeaders();
            headers.add("fileSystemName", fileSystemName);
            headers.add("Content-Type", "application/json");

            String url = "http://" + slaveUrl + "/" + interfaceName;

            HttpEntity<T> httpEntity=new HttpEntity<>(param, headers);
            restTemplate.postForObject(url, httpEntity, String.class);
        }


    }

}
