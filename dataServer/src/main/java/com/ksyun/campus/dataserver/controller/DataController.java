package com.ksyun.campus.dataserver.controller;

import com.ksyun.campus.dataserver.services.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("/")
public class DataController {

    @Autowired
    private DataService dataService;

    @RequestMapping("write")
    public ResponseEntity writeFile(@RequestHeader String fileSystemName, @RequestParam Map<String, Object> fileData){
        String path = fileData.get("path").toString();
        int offset = (Integer) fileData.get("offset");
        int length = (Integer) fileData.get("length");
        byte[] data = (byte[]) fileData.get("data");
        int res = dataService.write(fileSystemName, path, offset, length, data);
        if(res != 0){
            return new ResponseEntity<>("插入数据失败！", HttpStatus.valueOf(500));
        }
        return new ResponseEntity(HttpStatus.OK);
    }


    @RequestMapping("read")
    public ResponseEntity readFile(@RequestHeader String fileSystemName, @RequestParam Map<String, Object> fileData){
        String path = fileData.get("path").toString();
        int offset = (Integer) fileData.get("offset");
        int length = (Integer) fileData.get("length");
        byte[] res = dataService.read(fileSystemName, path, offset, length);
        if(res == null){
            return new ResponseEntity<>("读取数据失败！", HttpStatus.valueOf(500));
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping("mkdir")
    public ResponseEntity mkdir(@RequestHeader String fileSystemName, @RequestParam Map<String, Object> fileData){
        String path = fileData.get("path").toString();
        boolean res = dataService.mkdir(fileSystemName, path);
        if(!res){
            return new ResponseEntity<>("目录创建失败", HttpStatus.valueOf(500));
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping("create")
    public ResponseEntity create(@RequestHeader String fileSystemName, @RequestParam Map<String, Object> fileData){
        String path = fileData.get("path").toString();
        boolean res = dataService.create(fileSystemName, path);
        if(!res){
            return new ResponseEntity<>("文件创建失败", HttpStatus.valueOf(500));
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping("delete")
    public ResponseEntity delete(@RequestHeader String fileSystemName, @RequestParam Map<String, Object> fileData){
        String path = fileData.get("path").toString();
        boolean res = dataService.delete(fileSystemName, path);
        if(!res){
            return new ResponseEntity<>("删除失败", HttpStatus.valueOf(500));
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping("shutdown")
    public void shutdownServer(){
        System.exit(-1);
    }
}
