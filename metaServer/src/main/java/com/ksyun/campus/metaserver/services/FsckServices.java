package com.ksyun.campus.metaserver.services;

import com.ksyun.campus.metaserver.domain.ReplicaData;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.domain.StatInfoWithFSName;
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
    @Scheduled(fixedRate = 20000) // test
    public void fsckTask() {
        log.info("start fsck task....................");
        //全量扫描文件列表
        List<StatInfoWithFSName> allFileStatInfo = curatorService.getAllFileStatInfo();

        allFileStatInfo.forEach(statInfo -> {
            List<Boolean> status = new ArrayList<>();
            for(ReplicaData replicaData : statInfo.getReplicaData()) {
                //检查文件副本是否存在
                Map<String, Object> data = new HashMap<>();
                data.put("path", replicaData.getPath());
                boolean isExist = httpService.sendPostRequest(replicaData.getDsNode(), "checkFileExist", statInfo.getFileSystemName(), data).getStatusCode().is2xxSuccessful();
                status.add(isExist);
            }
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
            }
        }

        String content = (String) this.httpService.sendPostRequest(ip, "read",statInfo.getFileSystemName(), data).getBody();
        byte[] contentBytes = content.getBytes();

        for(int i=0;i<statInfo.getReplicaData().size();i++){
            if(status.get(i)){
                continue;
            }
            //write file content
            Map<String, Object> writeData = new HashMap<>();
            writeData.put("path", statInfo.getPath());
            writeData.put("data", Arrays.toString(contentBytes));
            ResponseEntity tryWriteResponse=this.httpService.sendPostRequest(statInfo.getReplicaData().get(i).getDsNode(), "write", statInfo.getFileSystemName(), writeData);
            if(!tryWriteResponse.getStatusCode().is2xxSuccessful()){
                return false;
            }
            log.info("Fsck: Recovery on {}", statInfo.getReplicaData().get(i).getDsNode());
        }
        return true;
    }
}
