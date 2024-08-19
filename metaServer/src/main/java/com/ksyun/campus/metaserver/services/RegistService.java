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

    private final CuratorFramework curatorRegisterClient;

    private static final String META_ZK_PATH = "/metaServer";

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

    @PostConstruct
    public void postConstruct() throws Exception {
        //注册到zk中心
        registerToCenter();
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
