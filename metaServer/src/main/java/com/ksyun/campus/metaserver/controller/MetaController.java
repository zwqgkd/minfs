package com.ksyun.campus.metaserver.controller;

import com.ksyun.campus.metaserver.domain.DataServerInfo;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.services.CuratorService;
import com.ksyun.campus.metaserver.services.MetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RestController("/")
public class MetaController {

    private final MetaService metaService;
    private final CuratorService curatorService;

    @Autowired
    public MetaController(MetaService metaService, CuratorService curatorService) {
        this.metaService = metaService;
        this.curatorService = curatorService;
    }

    @RequestMapping("stats")
    public ResponseEntity stats(@RequestHeader String fileSystemName, @RequestParam String path){
//        StatInfo statInfo = metaService.getStats(path);
//        if(statInfo == null) {
//            return new ResponseEntity<>("无stats", HttpStatus.valueOf(500));
//
//        }
//        return new ResponseEntity(statInfo, HttpStatus.OK);
        return ResponseEntity.ok(true);
    }
    @RequestMapping("create")
    public ResponseEntity createFile(@RequestHeader String fileSystemName, @RequestParam String path){
        if(metaService.create(path, fileSystemName)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("mkdir")
    public ResponseEntity mkdir(@RequestHeader String fileSystemName, @RequestParam String path){
        if(metaService.mkdir(path, fileSystemName)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("zookeeper连接失败或找不到对应结点", HttpStatus.valueOf(500));
        }
    }

    @RequestMapping("listdir")
    public ResponseEntity listdir(@RequestHeader String fileSystemName,@RequestParam String path){
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping("delete")
    public ResponseEntity delete(@RequestHeader String fileSystemName, @RequestParam String path){
        if(metaService.delete(path, fileSystemName)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("存在子节点无法删除", HttpStatus.valueOf(500));
        }
    }

    /**
     * client请求写文件，metaServer返回对应存数据的3个dataServer的ip地址
     * @param fileSystemName
     * @param bodyData
     * @return
     */
    @RequestMapping("write")
    public ResponseEntity write(@RequestHeader String fileSystemName, @RequestBody Map<String, Object> bodyData){
        List<String> ipList = metaService.write(bodyData, fileSystemName);
        return ResponseEntity.ok(ipList);
    }

    /**
     * 保存文件写入成功后的元数据信息，包括文件path、size、三副本信息等
     * @param fileSystemName
     * @param bodyData
     * @return
     */
    @RequestMapping("commitWrite")
    public ResponseEntity commitWrite(@RequestHeader String fileSystemName, @RequestBody Map<String, Object> bodyData){
        if(metaService.commitWrite(bodyData, fileSystemName)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据文件path查询三副本的位置，返回客户端具体ds、文件分块信息
     * @param fileSystemName
     * @param path
     * @return
     */
    @RequestMapping("open")
    public ResponseEntity open(@RequestHeader String fileSystemName,@RequestParam String path){

        StatInfo statInfo = curatorService.getStatInfo(fileSystemName, path);
        if(statInfo == null) {
            return new ResponseEntity<>("No exist status", HttpStatus.valueOf(500));

        }

        Random random = new Random();
        List<String> res = new ArrayList<>();
        statInfo.getReplicaData().forEach(e -> {
            res.add(e.dsNode);
        });
        String ip = res.get(random.nextInt(res.size()));

        return ResponseEntity.ok(ip);
    }

    /**
     * 关闭退出进程
     */
    @RequestMapping("shutdown")
    public void shutdownServer(){
        System.exit(-1);
    }

    /**
     * 获取文件元数据信息
     * @param fileSystemName
     * @param path
     * @return StatInfo
     */
    @RequestMapping("getFileStats")
    public ResponseEntity<StatInfo> getFileStats(@RequestHeader String fileSystemName, @RequestParam String path){
        return new ResponseEntity<>(metaService.getStatInfo(fileSystemName,path), HttpStatus.OK);
    }

    /**
     * 获取文件夹下的文件元数据信息
     * @param fileSystemName
     * @param path
     * @return List<StatInfo>
     */
    @RequestMapping("listFileStats")
    public ResponseEntity<List<StatInfo>> listFileStats(@RequestHeader String fileSystemName, @RequestParam String path){
        return new ResponseEntity<>(metaService.listFileStats(fileSystemName,path), HttpStatus.OK);
    }
}
