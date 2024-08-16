package com.ksyun.campus.metaserver.services;

import com.ksyun.campus.metaserver.domain.FileType;
import com.ksyun.campus.metaserver.domain.ReplicaData;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.entry.DataServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MetaService {

    @Autowired
    HttpService httpService = new HttpService();

    public Object pickDataServer(){
        // todo 通过zk内注册的ds列表，选择出来一个ds，用来后续的wirte
        // 需要考虑选择ds的策略？负载
        return null;
    }

//    public boolean write(Map<String, Object> dataNeedWriting) {
//        String path = (String) dataNeedWriting.get("path");
//        StatInfo statInfo = getStats(path);
//        List<String> ipList = getDsNodes(statInfo);
//        boolean isSuccessful = true;
//        for (String ip : ipList) {
//            ResponseEntity write = forwardService.call(ip, "write", dataTransferInfo);
//            if (write.getStatusCode() != HttpStatus.OK) {
//                isSuccessful = false;
//            }
//        }
//        statInfo.setMtime(System.currentTimeMillis());
//        statInfo.setSize(statInfo.getSize() + dataTransferInfo.getData().length);
//        registService.updateNodeData(dataTransferInfo.getPath(), statInfo);
//        return isSuccessful;
//    }

    public boolean mkdir(String path) {
        boolean allRequestsSuccessful = true;

        // Todo: 替换成正式功能
        List<DataServerInfo> randomServerInfos = getRandomServerInfos(3);

        // Todo: 向所有的选中DataServer发送文件目录信息
        /*
        for (DataServerInfo e : randomServerInfos) {
            Map<String, Object> param = new HashMap<>();
            param.put("path", path);
            HttpStatus statusCode = httpService.sendPostRequest(e.getDsNode(), "mkdir", param).getStatusCode();
            System.out.println(statusCode);
            if (statusCode != HttpStatus.OK) {
                allRequestsSuccessful = false;
                break; // Exit the loop since we encountered a failure
            }
        }
         */

        // Todo: 将存储成功的信息保存到MetaServer中
        StatInfo statInfo = creatDirStatInfo(path, randomServerInfos);
        log.info("StatInfo = {}", statInfo.toString());

        return allRequestsSuccessful;
    }

    public StatInfo creatDirStatInfo(String path, List<DataServerInfo> replicaDatas) {
        StatInfo statInfo = new StatInfo();
        statInfo.setPath(path);
        statInfo.setMtime(System.currentTimeMillis());
        statInfo.setSize(0);
        statInfo.setType(FileType.Directory);
        List<ReplicaData> replicaDataList = new ArrayList<>();

        replicaDatas.forEach(e -> {
            ReplicaData replicaData = new ReplicaData();
            replicaData.setId(e.getId());
            replicaData.setDsNode(e.getDsNode());
            replicaData.setPath(path);
            replicaDataList.add(replicaData);
        });

        statInfo.setReplicaData(replicaDataList);
        return statInfo;
    }

    public List<DataServerInfo> getRandomServerInfos(int count) {

        List<DataServerInfo> serverInfos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DataServerInfo data = DataServerInfo.builder().id("zack" + i + "-zone" + i).dsNode("127.0.0.1:900" + i).build();
            serverInfos.add(data);
        }
        return serverInfos;
    }

    public List<String> getDsNodes(StatInfo statInfo) {
        List<String> res = new ArrayList<>();
        statInfo.getReplicaData().forEach(e -> {
            res.add(e.dsNode);
        });
        return res;
    }

//    public StatInfo getStats(String path) {
//        String res = "";
//        StatInfo statInfo = null;
//        try {
//            byte[] data = client.getData().forPath(ZK_REGISTRY_PATH + path);
//            res = new String(data);
//            System.out.println(res);
//            statInfo = objectMapper.readValue(res, StatInfo.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return statInfo;
//    }
}
