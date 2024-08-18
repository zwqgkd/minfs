package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.metaserver.domain.FileType;
import com.ksyun.campus.metaserver.domain.ReplicaData;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.domain.DataServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MetaService {

    private final CuratorService curatorService;

    private final HttpService httpService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public MetaService(CuratorService curatorService, HttpService httpService) {
        this.httpService = httpService;
        this.curatorService = curatorService;
    }

    public List<String> write(Map<String, Object> dataNeedWriting, String fileSystemName) {
        String path = (String) dataNeedWriting.get("path");

        StatInfo statInfo = curatorService.getStatInfo(fileSystemName, path);
        List<String> ipList = getDsNodes(statInfo);
        boolean isSuccessful = true;

        // Todo: 向选择的dataServer发送所写的数据，但此处应该是client完成
//        for (String ip : ipList) {
//            ResponseEntity write = httpService.sendPostRequest(ip, "write", dataNeedWriting);
//            // ResponseEntity write = forwardService.call(ip, "write", dataTransferInfo);
//            if (write.getStatusCode() != HttpStatus.OK) {
//                isSuccessful = false;
//            }
//        }

        return ipList;
//        statInfo.setMtime(System.currentTimeMillis());
//        statInfo.setSize(statInfo.getSize() + dataTransferInfo.getData().length);
//        registService.updateNodeData(dataTransferInfo.getPath(), statInfo);
//        return isSuccessful;
    }

    public boolean commitWrite(Map<String, Object> dataNeedWriting, String fileSystemName) {
        String path = (String) dataNeedWriting.get("path");
        Integer size = (Integer) dataNeedWriting.get("size");

        StatInfo statInfo = getStatInfo(fileSystemName, path);

        String[] pathParts = path.split("/");
        StringBuilder pathBuilder = new StringBuilder();

        for (int i = 0; i < pathParts.length - 1; i++) {
            if (!pathParts[i].isEmpty()) {
                pathBuilder.append("/").append(pathParts[i]);
                String nodePath = pathBuilder.toString();

                try {
                    log.info("dir path: {}", nodePath);
                    StatInfo statInfoInside = curatorService.getStatInfo(fileSystemName, nodePath);
                    if (statInfoInside != null) {
                        statInfoInside.setMtime(System.currentTimeMillis());
                        curatorService.saveMetaData(fileSystemName, statInfoInside);
                        log.info("Update node: {}", statInfoInside);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }


        statInfo.setMtime(System.currentTimeMillis());
        statInfo.setSize(statInfo.getSize() + size);
        curatorService.saveMetaData(fileSystemName, statInfo);
        log.info("Update file node: {}", statInfo);
        return true;
    }

    public boolean mkdir(String path, String fileSystemName) {
        boolean allRequestsSuccessful = true;

        // Todo: 替换成正式功能
        List<DataServerInfo> randomServerInfos = getRandomServerInfos(3);
        String[] pathParts = path.split("/");
        StringBuilder pathBuilder = new StringBuilder();

        for (String part : pathParts) {
            if (!part.isEmpty()) {
                pathBuilder.append("/").append(part);
                String nodePath = pathBuilder.toString();
                // StatInfo statInfo = curatorService.getStatInfo(fileSystemName, part);
                // 检查节点是否存在
                try {
                    log.info("dir path: {}", nodePath);
                    StatInfo statInfo = curatorService.getStatInfo(fileSystemName, nodePath);
                    if (statInfo != null) {continue;}

                    statInfo = createStatInfo(FileType.Directory, nodePath, randomServerInfos);
                    log.info(String.valueOf(statInfo));

                    curatorService.saveMetaData(fileSystemName, statInfo);

                    log.info("Created node: {}", statInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

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
        // Todo: 将元数据信息持久化到zookeeper？

        return allRequestsSuccessful;
    }

    public StatInfo createStatInfo(FileType fileType, String path, List<DataServerInfo> replicaDatas) {
        StatInfo statInfo = new StatInfo();
        statInfo.setPath(path);
        statInfo.setMtime(System.currentTimeMillis());
        statInfo.setSize(0);
        statInfo.setType(fileType);
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

    public boolean create(String path, String fileSystemName) {
        boolean allRequestsSuccessful = true;

        // Todo: 替换成正式功能
        // 获取dataServer的信息
        List<DataServerInfo> randomServerInfos = getRandomServerInfos(3);

        // Todo: 向所有的选中DataServer发送所创建文件信息
        /*
        for (DataServerInfo e : randomServerInfos) {
            Map<String, Object> param = new HashMap<>();
            param.put("path", path);
            HttpStatus statusCode = httpService.sendPostRequest(e.getDsNode(), "create", param).getStatusCode();
            System.out.println(statusCode);
            if (statusCode != HttpStatus.OK) {
                allRequestsSuccessful = false;
                break; // Exit the loop since we encountered a failure
            }
        }
         */

        // 逐级创建节点，并存储数据
        String[] pathParts = path.split("/");
        StringBuilder pathBuilder = new StringBuilder();

        for (int i = 0; i < pathParts.length - 1; i++) {
            if (!pathParts[i].isEmpty()) {
                pathBuilder.append("/").append(pathParts[i]);
                String nodePath = pathBuilder.toString();

                try {
                    log.info("dir path: {}", nodePath);
                    StatInfo statInfoInside = curatorService.getStatInfo(fileSystemName, nodePath);
                    if (statInfoInside != null) {continue;}

                    statInfoInside = createStatInfo(FileType.Directory, nodePath, randomServerInfos);
                    log.info(String.valueOf(statInfoInside));
                    curatorService.saveMetaData(fileSystemName, statInfoInside);
                    log.info("Created node: {}", statInfoInside);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        pathBuilder.append("/").append(pathParts[pathParts.length - 1]);
        String filePath = pathBuilder.toString();

        StatInfo statInfo = createStatInfo(FileType.File, filePath, randomServerInfos);
        log.info(String.valueOf(statInfo));
        curatorService.saveMetaData(fileSystemName, statInfo);

        return allRequestsSuccessful;
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
