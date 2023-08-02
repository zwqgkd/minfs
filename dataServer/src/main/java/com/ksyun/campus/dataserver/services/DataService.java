package com.ksyun.campus.dataserver.services;

import org.springframework.stereotype.Service;

@Service
public class DataService {

    public void write(byte[] data){
        //todo 写本地
        //todo 调用远程ds服务写接口，同步副本，已达到多副本数量要求
        //todo 选择策略，按照 az rack->zone 的方式选取，将三副本均分到不同的az下
        //todo 支持重试机制
        //todo 返回三副本位置
    }
    public byte[] read(String path,int offset,int length){
        //todo 根据path读取指定大小的内容
        return null;
    }
}
