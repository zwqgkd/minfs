package com.ksyun.campus.metaserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.services.CuratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class MetaSyncController {
    private CuratorService curatorService;

    @Autowired
    public MetaSyncController(CuratorService curatorService) {
        this.curatorService = curatorService;
    }

    @PostMapping("/saveMetaData")
    public void saveMetaData(@RequestHeader String fileSystemName, @RequestBody StatInfo statInfo) {
        curatorService.saveMetaData(fileSystemName, statInfo);
    }

    @PostMapping("/deleteMetaData")
    public void deleteMetaData(@RequestHeader String fileSystemName, @RequestBody String path){
        curatorService.deleteMetaData(fileSystemName, path);
    }



}