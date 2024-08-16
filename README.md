# 简单实现一些NAS功能
## 各模块说明

- **bin:** 项目一键启动脚本，用于编译完成后，上传至服务器上，可以将minFS服务整体启动起来
- **dataServer:** 主要提供数据内容存储服务能力，单节点无状态设计，可以横向扩容
- **metaServer:** 主要提供文件系统全局元数据管理，管理dataserver的负载均衡，主备模式运行
- **easyClient:** 一个功能逻辑简单的SDK，用来与metaServer、dataServer通信，完成文件数据操作



## 大致流程

### 写流程

![写流程](https://github.com/zwqgkd/picx-images-hosting/raw/master/kc/photo_2024-08-15_13-13-32.wihaa1jjc.jpg)

## HOW TO USE

1. 准备工作

   `docker run --name zookeeper-dev --restart always -p 2181:2181 -d zookeeper:3.9 `