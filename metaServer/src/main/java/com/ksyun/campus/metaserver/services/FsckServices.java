package com.ksyun.campus.metaserver.services;

import com.ksyun.campus.metaserver.domain.StatInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FsckServices {

    private final CuratorService curatorService;

    @Autowired
    public FsckServices(CuratorService curatorService) {
        this.curatorService = curatorService;
    }

//    //@Scheduled(cron = "0 0 0 * * ?") // 每天 0 点执行
//    @Scheduled(fixedRate = 30*60*1000) // 每隔 30 分钟执行一次
//    public void fsckTask() {
//        //全量扫描文件列表
//        List<StatInfo> allFileStatInfo = curatorService.getAllFileStatInfo();
//
//        allFileStatInfo.forEach(statInfo -> {
//            //检查文件副本数量是否正常
//            if(statInfo.getReplicaData().size()<3 || statINfo){
//
//            }
//        }
//        // todo 更新文件副本数：3副本、2副本、单副本
//        // todo 记录当前检查结果
//    }
//
//    boolean isDsAlive(String path){
//
//    }
}
