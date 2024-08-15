package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.metaserver.domain.DataServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MetaService {

    private final CuratorFramework curatorClient;

    private static final String DS_REGISTER_PATH = "/dataServer";

    Autowired
    public MetaService(CuratorFramework curatorFramework){
        this.curatorClient = curatorFramework;
    }

    public Object pickDataServer(){
        // todo 通过zk内注册的ds列表，选择出来一个ds，用来后续的wirte
        // 需要考虑选择ds的策略？负载

        try{
            getDataServerList();
        }catch (Exception e){
            log.error("get data server list error",e);
        }
        return null;
    }

    // 获取ds列表
    List<DataServerInfo> getDataServerList() throws Exception {
        List<DataServerInfo> dsList=new ArrayList<>();
        curatorClient.getChildren().forPath(DS_REGISTER_PATH).forEach(child->{
            try {
                byte[] data = curatorClient.getData().forPath(DS_REGISTER_PATH+"/"+child);
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
}
