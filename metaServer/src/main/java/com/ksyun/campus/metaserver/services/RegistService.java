package com.ksyun.campus.metaserver.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public class RegistService {

    private final CuratorFramework curatorClient;

    private static final String META_ZK_PATH = "/metaServer";

    private final String serverHost;

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    public RegistService(CuratorFramework curatorFramework) throws UnknownHostException {
        this.curatorClient = curatorFramework;
        this.serverHost = InetAddress.getLocalHost().getHostAddress();
    }

    @PostConstruct
    public void postConstruct() throws Exception {
        //注册到zk中心
        registerToCenter();
    }

    @PreDestroy
    public void preDestroy() {
        //关闭curatorFramework
        this.curatorClient.close();
    }

    public void registerToCenter() throws Exception {
        String role="";
        if(curatorClient.checkExists().forPath(META_ZK_PATH+"/master")==null
                ||curatorClient.getChildren().forPath(META_ZK_PATH + "/master").isEmpty())
            role = "master";
        else
            role = "slave";
        log.info("register to zk center, role:{}", role);
        curatorClient.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL).forPath(META_ZK_PATH + "/" + role + "/"+ serverHost + ":" + serverPort);
    }
}
