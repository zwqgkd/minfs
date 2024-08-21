package com.ksyun.campus.metaserver.services;

import com.ksyun.campus.metaserver.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class FsckServices {

    private final HttpService httpService;

    private final CuratorService curatorService;

    @Autowired
    public FsckServices(HttpService httpService, CuratorService curatorService) {
        this.httpService = httpService;
        this.curatorService = curatorService;
    }

    // @Scheduled(fixedRate = 30 * 60 * 1000) // 每隔 30 分钟执行一次
    @Scheduled(fixedRate = 24000) // test
    public void fsckTask() {
        log.info("start fsck task....................");
        //全量扫描文件列表
        List<StatInfoWithFSName> allFileStatInfo = curatorService.getAllFileStatInfo();

        allFileStatInfo.forEach(statInfo -> {
            List<Boolean> status = new ArrayList<>();
            List<String> dataServerErrorIp = new ArrayList<>();
            boolean dataServerErrorTag = false;

            for(ReplicaData replicaData : statInfo.getReplicaData()) {
                //检查文件副本是否存在
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("path", replicaData.getPath());
                    int responseCode = httpService.sendPostRequest(replicaData.getDsNode(), "checkFileExist", statInfo.getFileSystemName(), data).getStatusCodeValue();
                    if (responseCode == 200) {
                        status.add(true);
                    } else {
                        status.add(false);
                    }
                } catch (Exception e) {
                    log.info("dataServer down!");
                    status.add(false);
                    dataServerErrorTag = true;
                    dataServerErrorIp.add(replicaData.getDsNode());
                }
            }

            if (dataServerErrorTag) {
                if(!status.contains(true)){
                    log.error("all replica is lost, path:{}, update meta data", statInfo.getPath());
                    return;
                }
                log.info("A dataServer has down, file meta path:{}", statInfo.getPath());
                boolean recoverResult = tryToReCoverReplica(statInfo, status, dataServerErrorIp);
                if (recoverResult) {
                    log.info("dataServer down recover success, path:{}", statInfo.getPath());
                } else {
                    log.error("dataServer down recover failed, path:{}", statInfo.getPath());
                }
            } else {
                if(status.contains(false)) {
                    //如果有副本不存在，尝试恢复
                    if(!status.contains(true)){
                        log.error("all replica is lost, path:{}, update meta data", statInfo.getPath());
                        return;
                    }
                    log.info("some replica is lost, file meta path:{}", statInfo.getPath());
                    log.info("try to recover replica..........");
                    boolean recoverResult = tryToReCoverReplica(statInfo, status);
                    if (recoverResult) {
                        log.info("recover success, path:{}", statInfo.getPath());
                    } else {
                        log.error("recover failed, path:{}", statInfo.getPath());
                    }
                }
            }
        });
    }

    private boolean tryToReCoverReplica(StatInfoWithFSName statInfo, List<Boolean> status) {
        //todo 从其他副本恢复
        //get file content
        Map<String, Object> data= new HashMap<>();
        data.put("path", statInfo.getPath());
        data.put("offset", 0);
        data.put("length", statInfo.getSize());

        String ip = "";
        for (int i = 0; i < status.size(); i++) {
            if (status.get(i)) {
                ip = statInfo.getReplicaData().get(i).getDsNode();
                break;
            }
        }

        String content = (String) this.httpService.sendPostRequest(ip, "read",statInfo.getFileSystemName(), data).getBody();
        byte[] contentBytes = {};
        if (content != null) {
            contentBytes = convertData(content);
        }

        for(int i=0;i<statInfo.getReplicaData().size();i++){
            if(status.get(i)){
                continue;
            }
            //write file content
            Map<String, Object> writeData = new HashMap<>();
            writeData.put("path", statInfo.getPath());
            writeData.put("data", Arrays.toString(contentBytes));
            ResponseEntity tryWriteResponse=this.httpService.sendPostRequest(statInfo.getReplicaData().get(i).getDsNode(), "recoveryWrite", statInfo.getFileSystemName(), writeData);
            if(!tryWriteResponse.getStatusCode().is2xxSuccessful()){
                return false;
            }
            log.info("Fsck: Recovery on {}", statInfo.getReplicaData().get(i).getDsNode());
        }
        return true;
    }

    private boolean tryToReCoverReplica(StatInfoWithFSName statInfo, List<Boolean> status, List<String> dataServerErrorIp) {
        //todo 从其他副本恢复
        //get file content
        Map<String, Object> data= new HashMap<>();
        data.put("path", statInfo.getPath());
        data.put("offset", 0);
        data.put("length", statInfo.getSize());

        String ip = "";
        for (int i = 0; i < status.size(); i++) {
            if (status.get(i)) {
                ip = statInfo.getReplicaData().get(i).getDsNode();
            }
        }

        String content = (String) this.httpService.sendPostRequest(ip, "read",statInfo.getFileSystemName(), data).getBody();
        byte[] contentBytes = {};
        if (content != null) {
            contentBytes = convertData(content);
        }

        for(int i=0;i<statInfo.getReplicaData().size();i++){
            if(status.get(i)){
                continue;
            }
            if (dataServerErrorIp.contains(statInfo.getReplicaData().get(i).getDsNode())) {
                // 遇到了down的dataServer
                recoveryDataOnNewDataServer(statInfo, contentBytes, statInfo.getReplicaData().get(i).getDsNode());
                continue;
            }
            //write file content
            Map<String, Object> writeData = new HashMap<>();
            writeData.put("path", statInfo.getPath());
            writeData.put("data", Arrays.toString(contentBytes));
            ResponseEntity tryWriteResponse=this.httpService.sendPostRequest(statInfo.getReplicaData().get(i).getDsNode(), "recoveryWrite", statInfo.getFileSystemName(), writeData);
            if(!tryWriteResponse.getStatusCode().is2xxSuccessful()){
                return false;
            }
            log.info("Fsck: Recovery on {}", statInfo.getReplicaData().get(i).getDsNode());
        }
        return true;
    }

    public boolean recoveryDataOnNewDataServer(StatInfoWithFSName statInfo, byte[] contentBytes, String errorIp) {
        List<DataServerInfo> allDataServerInfo;
        List<String> allDataServerIp = new ArrayList<>();

        try {
            allDataServerInfo = curatorService.getAllDataServerInfo();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (DataServerInfo dataServerInfo : allDataServerInfo) {
            allDataServerIp.add(dataServerInfo.getDsNode());
        }

        List<String> difference = new ArrayList<>(allDataServerIp);
        List<String> curFileReplicas = new ArrayList<>();

        for (ReplicaData replicaData : statInfo.getReplicaData()) {
            curFileReplicas.add(replicaData.getDsNode());
        }

        difference.removeAll(curFileReplicas);

        String newReplicaIp = difference.get(0);
        //write file content
        Map<String, Object> writeData = new HashMap<>();
        writeData.put("path", statInfo.getPath());
        writeData.put("data", Arrays.toString(contentBytes));

        ResponseEntity tryWriteResponse = this.httpService.sendPostRequest(newReplicaIp, "write", statInfo.getFileSystemName(), writeData);
        if(!tryWriteResponse.getStatusCode().is2xxSuccessful()){
            return false;
        }

        StatInfo statInfo1 = new StatInfo();
        statInfo1.setSize(statInfo.getSize());
        statInfo1.setPath(statInfo.getPath());
        statInfo1.setMtime(statInfo.getMtime());
        statInfo1.setType(statInfo.getType());

        List<ReplicaData> tmp = statInfo.getReplicaData();
        ReplicaData needToChange = new ReplicaData();

        String newId = "";
        for (DataServerInfo dataServerInfo : allDataServerInfo) {
            if (dataServerInfo.getDsNode().equals(newReplicaIp)) {
                newId = dataServerInfo.getId();
            }
        }
        needToChange.setDsNode(newReplicaIp);
        needToChange.setPath(statInfo.getPath());
        needToChange.setId(newId);

        List<ReplicaData> toRemove = new ArrayList<>();
        for (ReplicaData replicaData : tmp) {
            if (replicaData.getDsNode().equals(errorIp)) {
                toRemove.add(replicaData);
            }
        }

        // 在循环外进行删除和添加操作
        tmp.removeAll(toRemove);
        tmp.add(needToChange);

        statInfo.setReplicaData(tmp);
        statInfo1.setReplicaData(statInfo.getReplicaData());
        curatorService.saveMetaData(statInfo.getFileSystemName(), statInfo1);

        String[] pathParts = statInfo.getPath().split("/");
        pathParts = Arrays.copyOfRange(pathParts, 1, pathParts.length - 1);
        StringBuilder pathBuilder = new StringBuilder();

        updateRecMetaDataInfo(statInfo.getFileSystemName(), pathParts, pathBuilder, tmp);

        log.info("Fsck: Recovery on online dataServer {}", newReplicaIp);

        return true;
    }

    public void updateRecMetaDataInfo(String fileSystemName, String[] pathParts, StringBuilder pathBuilder, List<ReplicaData> replicaInfo) {

        if (pathParts.length == 0) {
            return;
        }

        String part = pathParts[0];
        String[] remainingParts = Arrays.copyOfRange(pathParts, 1, pathParts.length);

        if (!part.isEmpty()) {
            log.info("part msg: {}", part);
            pathBuilder.append("/").append(part);
            String nodePath = pathBuilder.toString();

            // 递归处理子目录
            updateRecMetaDataInfo(fileSystemName, remainingParts, pathBuilder, replicaInfo);

            StatInfo statInfo = curatorService.getStatInfo(fileSystemName, nodePath);
            if (statInfo != null) {
                for (ReplicaData replicaData : replicaInfo) {
                    replicaData.setPath(nodePath);
                }
                statInfo.setReplicaData(replicaInfo);
                curatorService.saveMetaData(fileSystemName, statInfo);
                log.info("Recovery: Updated metaData with historical time: {}", statInfo);
            }
        }
    }

    public byte[] convertData(String str) {
        String trimmedStr = str.substring(1, str.length() - 1);
        String[] stringValues = trimmedStr.split("\\s*,\\s*");
        byte[] byteArray = new byte[stringValues.length];

        for (int i = 0; i < stringValues.length; i++) {
            byteArray[i] = Byte.parseByte(stringValues[i]);
        }
        return byteArray;
    }
}
