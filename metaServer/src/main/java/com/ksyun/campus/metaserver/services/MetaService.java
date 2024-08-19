package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.metaserver.domain.FileType;
import com.ksyun.campus.metaserver.domain.ReplicaData;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.domain.DataServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
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

        return ipList;
    }

    public boolean commitWrite(Map<String, Object> dataNeedWriting, String fileSystemName) {
        String path = (String) dataNeedWriting.get("path");
        Integer size = (Integer) dataNeedWriting.get("size");

        StatInfo statInfo = getStatInfo(fileSystemName, path);

        String[] pathParts = path.split("/");
        StringBuilder pathBuilder = new StringBuilder();

        // write功能，写存在的文件则不修改前置目录的修改时间
//        for (int i = 0; i < pathParts.length - 1; i++) {
//            if (!pathParts[i].isEmpty()) {
//                pathBuilder.append("/").append(pathParts[i]);
//                String nodePath = pathBuilder.toString();
//
//                try {
//                    log.info("dir path: {}", nodePath);
//                    StatInfo statInfoInside = curatorService.getStatInfo(fileSystemName, nodePath);
//                    if (statInfoInside != null) {
//                        statInfoInside.setMtime(System.currentTimeMillis());
//                        curatorService.saveMetaData(fileSystemName, statInfoInside);
//                        log.info("Update node: {}", statInfoInside);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            }
//        }

        statInfo.setMtime(System.currentTimeMillis());
        statInfo.setSize(statInfo.getSize() + size);
        curatorService.saveMetaData(fileSystemName, statInfo);
        log.info("Update file node: {}", statInfo);
        return true;
    }

    public boolean mkdir(String path, String fileSystemName) {
        boolean allRequestsSuccessful = true;

        List<DataServerInfo> randomServerInfos = null;
        try {
            randomServerInfos = getThreeDataServerList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (DataServerInfo e : randomServerInfos) {
            Map<String, Object> param = new HashMap<>();
            param.put("path", path);
            HttpStatus statusCode = httpService.sendPostRequest(e.getDsNode(), "mkdir", fileSystemName, param).getStatusCode();
            System.out.println(statusCode);
            if (statusCode != HttpStatus.OK) {
                allRequestsSuccessful = false;
                break;
            }
        }

        if (!allRequestsSuccessful) {
            return false;
        }

        // 更新新创建的dir信息
        String[] pathParts = path.split("/");
        pathParts = Arrays.copyOfRange(pathParts, 1, pathParts.length);
        StringBuilder pathBuilder = new StringBuilder();

        // 先更新里面文件/文件夹的修改时间，然后更新外面的
        StatInfo statInfo = createStatInfo(FileType.File, path, randomServerInfos);
        log.info(String.valueOf(statInfo));
        curatorService.saveMetaData(fileSystemName, statInfo);

        updateRecMetaDataInfo(fileSystemName, pathParts, pathBuilder, randomServerInfos);

        return allRequestsSuccessful;
    }

    public boolean delete(String path, String fileSystemName) {

        boolean allRequestsSuccessful = true;

        // 不存在子节点，删除当前节点
        try {
            List<StatInfo> childrenInfo = curatorService.getChildren(fileSystemName, path);
            int childrenCount = childrenInfo.size();

            // dataServer进行递归删除操作
            StatInfo statInfo = curatorService.getStatInfo(fileSystemName, path);
            System.out.println(statInfo);
            statInfo.getReplicaData().forEach(e -> {
                Map<String, Object> data = new HashMap<>();
                data.put("path", e.getPath());
                httpService.sendPostRequest(e.dsNode, "delete", fileSystemName, data);
            });

            if (childrenCount == 0) {
                curatorService.deleteMetaData(fileSystemName, path);
                log.info("Delete File: {}", statInfo.getPath());
            } else {
                // 递归删除
                for (StatInfo child : childrenInfo) {
                    String childPath = child.getPath(); // 拼接子节点路径
                    boolean tmp =  delete(childPath, fileSystemName);
                    if (!tmp) {
                        allRequestsSuccessful = false;
                    }
                }
                curatorService.deleteMetaData(fileSystemName, path);
                log.info("Delete Parent File: {}", statInfo.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    public boolean create(String path, String fileSystemName) {
        boolean allRequestsSuccessful = true;

        // 获取dataServer的信息
        List<DataServerInfo> randomServerInfos = null;
        try {
            randomServerInfos = getThreeDataServerList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Todo: 向所有的选中DataServer发送所创建文件信息
        for (DataServerInfo e : randomServerInfos) {
            Map<String, Object> param = new HashMap<>();
            param.put("path", path);
            HttpStatus statusCode = httpService.sendPostRequest(e.getDsNode(), "create", fileSystemName, param).getStatusCode();
            System.out.println(statusCode);
            if (statusCode != HttpStatus.OK) {
                allRequestsSuccessful = false;
                break;
            }
        }

        // 逐级创建节点，并存储数据
        String[] pathParts = path.split("/");
        pathParts = Arrays.copyOfRange(pathParts, 1, pathParts.length - 1);
        StringBuilder pathBuilder = new StringBuilder();

        // 先更新里面文件/文件夹的修改时间，然后更新外面的
        StatInfo statInfo = createStatInfo(FileType.File, path, randomServerInfos);
        log.info(String.valueOf(statInfo));
        curatorService.saveMetaData(fileSystemName, statInfo);

        updateRecMetaDataInfo(fileSystemName, pathParts, pathBuilder, randomServerInfos);

        return allRequestsSuccessful;
    }

    public void updateRecMetaDataInfo(String fileSystemName, String[] pathParts, StringBuilder pathBuilder, List<DataServerInfo> randomServerInfos) {

        if (pathParts.length == 0) {
            return;
        }

        String part = pathParts[0];
        String[] remainingParts = Arrays.copyOfRange(pathParts, 1, pathParts.length);

        if (!part.isEmpty()) {
            pathBuilder.append("/").append(part);
            String nodePath = pathBuilder.toString();

            // 递归处理子目录
            updateRecMetaDataInfo(fileSystemName, remainingParts, pathBuilder, randomServerInfos);

            StatInfo statInfo = curatorService.getStatInfo(fileSystemName, nodePath);
            if (statInfo != null) {
                statInfo.setMtime(System.currentTimeMillis());
                curatorService.saveMetaData(fileSystemName, statInfo);
                log.info("Create: Updated metaData with historical time: {}", statInfo);
            } else {
                // 如果节点不存在，则创建它
                statInfo = createStatInfo(FileType.Directory, nodePath, randomServerInfos);
                curatorService.saveMetaData(fileSystemName, statInfo);
                log.info("Create: Created dir node: {}", statInfo);
            }
        }
    }

    public List<String> getDsNodes(StatInfo statInfo) {
        List<String> res = new ArrayList<>();
        statInfo.getReplicaData().forEach(e -> {
            res.add(e.dsNode);
        });
        return res;
    }

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
