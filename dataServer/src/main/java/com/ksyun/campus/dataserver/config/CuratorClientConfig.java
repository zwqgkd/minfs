package com.ksyun.campus.dataserver.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CuratorClientConfig {
    @Value("${spring.zookeeper-address.register}")
    private String zookeeperAddress;

    @Bean
    public CuratorFramework curatorFramework() {
        //重试策略：初始sleep时间1s，最大重试3次
        ExponentialBackoffRetry backOff=new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client= CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(backOff)
                .build();
        client.start();
        return client;
    }
}
