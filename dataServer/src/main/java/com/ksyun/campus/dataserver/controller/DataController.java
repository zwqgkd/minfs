package com.ksyun.campus.dataserver.controller;

import com.ksyun.campus.dataserver.services.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @Autowired
    private DataService dataService;

    @RequestMapping("write")
    public ResponseEntity writeFile(@RequestHeader String fileSystemName, @RequestBody Map<String, Object> fileData){
        String path = fileData.get("path").toString();
        // int offset = (Integer) fileData.get("offset");
        // int length = (Integer) fileData.get("length");
        String str = (String) fileData.get("data");
        System.out.println(str);

        String trimmedStr = str.substring(1, str.length() - 1);

        String[] stringValues = trimmedStr.split("\\s*,\\s*");

        byte[] byteArray = new byte[stringValues.length];

        for (int i = 0; i < stringValues.length; i++) {
            byteArray[i] = Byte.parseByte(stringValues[i]);
        }

        int res = dataService.write(fileSystemName, path, byteArray);
        return ResponseEntity.ok(res);
    }


    @RequestMapping("read")
    public ResponseEntity readFile(@RequestHeader String fileSystemName, @RequestBody Map<String, Object> fileData){
        String path = fileData.get("path").toString();
        int offset = (Integer) fileData.get("offset");
        int length = (Integer) fileData.get("length");
        byte[] res = dataService.read(fileSystemName, path, offset, length);
        if(res == null){
            return new ResponseEntity<>("读取数据失败！", HttpStatus.valueOf(500));
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping("mkdir")
    public ResponseEntity mkdir(@RequestHeader String fileSystemName, @RequestBody Map<String, Object> fileData){
        String path = (String) fileData.get("path");
        boolean res = dataService.mkdir(fileSystemName, path);
        if(!res){
            return new ResponseEntity<>("目录创建失败", HttpStatus.valueOf(500));
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping("create")
    public ResponseEntity create(@RequestHeader String fileSystemName, @RequestBody Map<String, Object> fileData){
        String path = fileData.get("path").toString();
        boolean res = dataService.create(fileSystemName, path);
        if(!res){
            return new ResponseEntity<>("文件创建失败", HttpStatus.valueOf(500));
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping("delete")
    public ResponseEntity delete(@RequestHeader String fileSystemName, @RequestBody Map<String, Object> fileData){
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
