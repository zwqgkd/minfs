package com.ksyun.campus.client.util;

import javax.annotation.PostConstruct;

public class ZkUtil {

    public ZkUtil() {
    }

    @PostConstruct
    public void postCons() throws Exception {
        // todo 初始化，与zk建立连接，注册监听路径，当配置有变化随时更新
    }

    public String getMasterMetaDataServerUrl() {
        return "localhost:8000";
    }

    public String getSlaveMetaDataServerUrl() {
        return "";
    }

    public String getDataServerUrl() {
        return "localhost:9000";
    }

}
