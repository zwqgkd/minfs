package com.ksyun.campus.client.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.client.domain.ClusterInfo;
import com.ksyun.campus.client.domain.DataServerInfo;
import com.ksyun.campus.client.domain.DataServerMsg;
import com.ksyun.campus.client.domain.MetaServerMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class ZkUtil {

    private static final String ZK_ADDR="localhost:2181";

    private static final String MASTER_META_ZK_PATH="/metaServer/master";

    private static final String SLAVE_META_ZK_PATH="/metaServer/slave";

    private static final String DS_ZK_PATH="/dataServer";

    private CuratorFramework curatorClient;

    /**
     * 监听metaServer，当master挂掉时，换master,尝试恢复老的master
     */
    protected void metaServerListener() {
        CuratorCache cache = CuratorCache.build(curatorClient, MASTER_META_ZK_PATH);
        cache.listenable().addListener((type, oldData, data) -> {
            if (data != null) {
                log.info("Node changed: {} = {}", data.getPath(), new String(data.getData()));
            } else {
                log.info("Node removed: {}", oldData.getPath());
                try {
                    //change master
                    curatorClient.create().creatingParentsIfNeeded().forPath(MASTER_META_ZK_PATH+"/"+this.getSlaveMetaAddress());
                    curatorClient.delete().forPath(SLAVE_META_ZK_PATH);
                    //docker compose try to recover old master
                } catch (Exception e) {
                    log.error("recover master error",e);
                    throw new RuntimeException(e);
                }

            }
        });
    }

    public ZkUtil() {
        try {
            postCons();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 连接zk;
     * 注册监听路径;
     */
    @PostConstruct
    public void postCons() throws Exception {
        // todo 初始化，与zk建立连接，注册监听路径，当配置有变化随时更新
        //重试策略：初始sleep时间1s，最大重试3次
        ExponentialBackoffRetry backOff=new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client= CuratorFrameworkFactory.builder()
                .connectString(ZK_ADDR)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(backOff)
                .build();
        client.start();
        this.curatorClient = client;
        //监听metaServer

    }

    @PreDestroy
    public void preDestroy() {
        // todo 关闭curatorFramework
        this.curatorClient.close();
    }

    /**
     * 获取主metaServer地址
     * @return 获取当前master metaServer地址
     */
    public String getMasterMetaAddress() throws Exception {
         if(!curatorClient.getChildren().forPath(MASTER_META_ZK_PATH).isEmpty()) {
             log.info("get master address from zk");
             return curatorClient.getChildren().forPath(MASTER_META_ZK_PATH).get(0);
         }else {
             log.error("no master address in zk");
             return null;
         }
    }

    /**
     * 获取从metaServer地址
     * @return 获取当前salve metaServer地址
     */
    public String getSlaveMetaAddress() throws Exception{
        if(curatorClient.checkExists().forPath(SLAVE_META_ZK_PATH)!=null) {
            log.info("get slave address from zk");
            return curatorClient.getChildren().forPath(SLAVE_META_ZK_PATH).get(0);
        }else{
            log.error("no slave address in zk");
            return null;
        }
    }

    /**
     * 获取所有ds的信息
     * @return ds列表
     */
    protected List<DataServerInfo> getAllDataServerInfo() throws Exception {
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
     * 获取集群信息
     * @return 集群信息
     */
    public ClusterInfo getClusterInfo() throws Exception {
        ClusterInfo clusterInfo = new ClusterInfo();

        //get master address
        clusterInfo.setMasterMetaServer(
                new MetaServerMsg(
                        this.getMasterMetaAddress().split(":")[0],
                        Integer.parseInt(this.getMasterMetaAddress().split(":")[1])
                )
        );
        //get slave address
        clusterInfo.setSlaveMetaServer(
                new MetaServerMsg(
                        this.getSlaveMetaAddress().split(":")[0],
                        Integer.parseInt(this.getSlaveMetaAddress().split(":")[1])
                )
        );
        //get ds list
        if (curatorClient.checkExists().forPath(DS_ZK_PATH) != null) {
            log.info("get ds list from zk");
            List<DataServerInfo> dataServerInfoList = this.getAllDataServerInfo();
            clusterInfo.setDataServer(dataServerInfoList.stream()
                    .map(ds -> new DataServerMsg(
                            ds.getIp(),
                            ds.getPort(),
                            ds.getFileTotal(),
                            ds.getCapacity(),
                            ds.getUseCapacity()
                    )).collect(Collectors.toList()));
        } else {
            log.error("no data server in zk");
        }
        return clusterInfo;
    }

}
