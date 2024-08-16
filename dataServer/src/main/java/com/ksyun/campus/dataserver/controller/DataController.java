package com.ksyun.campus.dataserver.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("/")
public class DataController {
    /**
     * 1、读取request content内容并保存在本地磁盘下的文件内
     * 2、同步调用其他ds服务的write，完成另外2副本的写入
     * 3、返回写成功的结果及三副本的位置
     * @param fileSystemName
     * @param path
     * @param offset
     * @param length
     * @return
     */
    @RequestMapping("write")
    public ResponseEntity writeFile(@RequestHeader String fileSystemName, @RequestParam String path, @RequestParam int offset, @RequestParam int length){
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 在指定本地磁盘路径下，读取指定大小的内容后返回
     * @param fileSystemName
     * @param path
     * @param offset
     * @param length
     * @return
     */
    @RequestMapping("read")
    public ResponseEntity readFile(@RequestHeader String fileSystemName, @RequestParam String path, @RequestParam int offset, @RequestParam int length){
        return new ResponseEntity(HttpStatus.OK);
    }
    /**
     * 关闭退出进程
     */
    @RequestMapping("shutdown")
    public void shutdownServer(){
        System.exit(-1);
    }

    @RequestMapping("mkdir")
    public ResponseEntity mkdir(){
        return new ResponseEntity(HttpStatus.OK);
    }
}
