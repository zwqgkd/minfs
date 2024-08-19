package com.ksyun.campus.dataserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.dataserver.domain.DataServerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

@Component
public class RegistService {

    private final CuratorFramework curatorClient;

    private final static String DS_ZK_PATH = "/dataServer";

    private final String serverHost;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${server.port}")
    private int serverPort;

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
        registerToCenter();
    }

    @PreDestroy
    public void preDestroy() {
        //关闭curatorFramework
        this.curatorClient.close();
    }

    public void registerToCenter() throws Exception {
        //将本实例信息注册至zk中心，包含信息 ip、port、capacity、rack、zone
        DataServerInfo dataServerInfo = new DataServerInfo(serverHost, serverPort, rack, zone, 100, 0, 0);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dataServerInfo);
        curatorClient.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(DS_ZK_PATH + "/"+ dataServerInfo.getId(), json.getBytes(StandardCharsets.UTF_8));

        // 阻塞当前线程，直到成功连接到 zk
        curatorClient.blockUntilConnected();
    }

    // 获取当前 ds 的数据信息
    public DataServerInfo getServerInfo() throws Exception {
        byte[] serverData = curatorClient.getData().forPath("/dataServer/" + rack + "-" + zone);
        if (serverData != null) {
            String json = new String(serverData, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, DataServerInfo.class);
        }
        return null;
    }

    // 更新当前 ds 的数据信息
    public void updateServerInfo(DataServerInfo serverInfo) throws Exception {
        String node = "/dataServer/" + rack + "-" + zone;
        Stat stat = curatorClient.setData().forPath(node, objectMapper.writeValueAsBytes(serverInfo));
    }
}
