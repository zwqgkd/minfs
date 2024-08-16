package com.ksyun.campus.metaserver.controller;

import com.ksyun.campus.metaserver.services.MetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController("/")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    @RequestMapping("stats")
    public ResponseEntity stats(@RequestHeader String fileSystemName,@RequestParam String path){
        return new ResponseEntity(HttpStatus.OK);
    }
    @RequestMapping("create")
    public ResponseEntity createFile(@RequestHeader String fileSystemName, @RequestParam String path){
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping("mkdir")
    public ResponseEntity mkdir(@RequestHeader String fileSystemName, @RequestParam String path){
        if(metaService.mkdir(path)) {
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
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 保存文件写入成功后的元数据信息，包括文件path、size、三副本信息等
     * @param fileSystemName
     * @param path
     * @param offset
     * @param length
     * @return
     */
    @RequestMapping("write")
    public ResponseEntity commitWrite(@RequestHeader String fileSystemName, @RequestParam String path, @RequestParam int offset, @RequestParam int length, @RequestBody Map<String, Object> bodyData){

        System.out.println(fileSystemName + ":" + path + ":" + offset + ":" + length);
        System.out.println(bodyData);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 根据文件path查询三副本的位置，返回客户端具体ds、文件分块信息
     * @param fileSystemName
     * @param path
     * @return
     */
    @RequestMapping("open")
    public ResponseEntity open(@RequestHeader String fileSystemName,@RequestParam String path){
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 关闭退出进程
     */
    @RequestMapping("shutdown")
    public void shutdownServer(){
        System.exit(-1);
    }

}
