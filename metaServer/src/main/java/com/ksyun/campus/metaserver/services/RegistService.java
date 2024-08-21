package com.ksyun.campus.metaserver.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Value;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.*;

@Slf4j
@Component
public class RegistService {

    private final CuratorFramework curatorRegisterClient;

    private static final String META_ZK_PATH = "/metaServer";

    private static final String MASTER_META_ZK_PATH="/metaServer/master";

    private static final String SLAVE_META_ZK_PATH="/metaServer/slave";

    private final String serverHost;

    private String role;

    @Value("${server.port}")
    private String serverPort;

    public String getRole(){return role;}

    @Autowired
    public RegistService(CuratorFramework curatorFramework) throws UnknownHostException {
        this.curatorRegisterClient = curatorFramework;
        this.serverHost = InetAddress.getLocalHost().getHostAddress();
    }

    /**
     * 获取从metaServer地址
     * @return 获取当前salve metaServer地址
     */
    public String getSlaveMetaAddress() throws Exception{
        if(!curatorRegisterClient.getChildren().forPath(SLAVE_META_ZK_PATH).isEmpty()) {
            log.info("get slave address from zk");
            return curatorRegisterClient.getChildren().forPath(SLAVE_META_ZK_PATH).get(0);
        }else{
            log.error("no slave address in zk");
            return null;
        }
    }

    /**
     * 监听metaServer，当master挂掉时，换master,尝试恢复老的master
     */
    protected void metaServerListener() throws Exception {
        CuratorCache cache = CuratorCache.build(curatorRegisterClient, MASTER_META_ZK_PATH);
        cache.listenable().addListener((type, oldData, data) -> {
            if (type == CuratorCacheListener.Type.NODE_CREATED) {
                log.info("Listening.....Node created: {} ", data.getPath());
            } else if (type == CuratorCacheListener.Type.NODE_CHANGED) {
                log.info("Listening.....Node changed: {} ", data.getPath());
            } else if (type == CuratorCacheListener.Type.NODE_DELETED) {
                log.info("Listening.....Node removed: {}", oldData.getPath());
                try {
                    if(this.curatorRegisterClient.getChildren().forPath(MASTER_META_ZK_PATH).isEmpty()
                    && this.getRole().equals("slave")){
                        //change master
                        curatorRegisterClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(MASTER_META_ZK_PATH+"/"+this.getSlaveMetaAddress());
                        curatorRegisterClient.delete().forPath(SLAVE_META_ZK_PATH+"/"+this.getSlaveMetaAddress());
                        //set role
                        role = "master";
                        log.info("recover master success");
                        //docker compose try to recover old master
                    }
                } catch (Exception e) {
                    log.error("recover master error",e);
                    throw new RuntimeException(e);
                }
            }
        });
        cache.start();
    }

    @PostConstruct
    public void postConstruct() throws Exception {
        //注册到zk中心
        registerToCenter();
        //监听metaServer
        metaServerListener();
    }

    @PreDestroy
    public void preDestroy() {
        //关闭curatorFramework
        this.curatorRegisterClient.close();
    }

    public void registerToCenter() throws Exception {
        if(curatorRegisterClient.checkExists().forPath(META_ZK_PATH+"/master")==null
                ||curatorRegisterClient.getChildren().forPath(META_ZK_PATH + "/master").isEmpty())
            role = "master";
        else
            role = "slave";
        log.info("register to zk center, role:{}, addr:{}", role, serverHost + ":" + serverPort);
        curatorRegisterClient.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL).forPath(META_ZK_PATH + "/" + role + "/"+ serverHost + ":" + serverPort);
    }

}
