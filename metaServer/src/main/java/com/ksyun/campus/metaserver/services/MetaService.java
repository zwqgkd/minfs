package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.metaserver.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MetaService {

    private final CuratorService curatorService;

    @Autowired
    public MetaService(CuratorService curatorService) {
        this.curatorService=curatorService;
    }

    // 获取ds列表
    List<DataServerInfo> getThreeDataServerList() throws Exception {
        //todo 选择策略
        return curatorService.getAllDataServerInfo().stream().sorted((ds1,ds2)->
             ds1.getFreeSpace()>ds2.getFreeSpace()?1:-1
        ).collect(Collectors.toList()).subList(0,3);
    }
}
