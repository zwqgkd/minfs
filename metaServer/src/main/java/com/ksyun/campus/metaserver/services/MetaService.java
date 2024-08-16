package com.ksyun.campus.metaserver.services;

import com.ksyun.campus.metaserver.domain.FileType;
import com.ksyun.campus.metaserver.domain.ReplicaData;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.domain.DataServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MetaService {

    private final CuratorService curatorService;

    private final HttpService httpService;

    @Autowired
    public MetaService(CuratorService curatorService, HttpService httpService){
        this.httpService=httpService;
        this.curatorService=curatorService;
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
            DataServerInfo data = DataServerInfo.builder().rack("rack" + i).zone("-zone" + i).ip("127.0.0.1").port(900 + i).build();
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

    /**
     * @return 负载均衡，选3个空间最大的DataServer
     */
    public List<DataServerInfo> getThreeDataServerList() throws Exception {
        //选择策略
        return curatorService.getAllDataServerInfo().stream().sorted(Comparator.comparingInt(DataServerInfo::getFreeSpace)).collect(Collectors.toList()).subList(0,3);
    }

    /**
     * @return 文件的元数据信息
     */
    public StatInfo getStatInfo(String fileSystemName, String path){
        return this.curatorService.getStatInfo(fileSystemName, path);
    }

    /**
     * @return 文件夹下所有文件的元数据信息
     */
    public List<StatInfo> listFileStats(String fileSystemName, String path) {
        return this.curatorService.getChildren(fileSystemName, path);
    }
}
