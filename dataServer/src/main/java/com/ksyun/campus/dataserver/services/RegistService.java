package com.ksyun.campus.dataserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.dataserver.model.DataServerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

@Component
public class RegistService {

    private final CuratorFramework curatorClient;

    private final String registerPath = "/dataServer";

    private final String serverHost;

    @Value("${server.port}")
    private String serverPort;

    @Value("${az.rack}")
    private String rack;

    @Value("${az.zone}")
    private String zone;

    @Autowired
    public RegistService(CuratorFramework curatorFramework) throws UnknownHostException {
        this.curatorClient = curatorFramework;
        this.serverHost = InetAddress.getLocalHost().getHostAddress();
    }

    @PostConstruct
    public void postConstruct() throws Exception {
        //注册到zk中心
        registToCenter();
    }

    @PreDestroy
    public void preDestroy() {
        //关闭curatorFramework
        this.curatorClient.close();
    }

    public void registToCenter() throws Exception {
        //将本实例信息注册至zk中心，包含信息 ip、port、capacity、rack、zone
        DataServerInfo dataServerInfo=new DataServerInfo(serverHost, serverPort, rack, zone, 100);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dataServerInfo);
        curatorClient.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL).forPath(registerPath + "/1", json.getBytes());
    }

    public List<Map<String, Integer>> getDslist() {
        return null;
    }
}
