# 简单实现一些NAS功能

### 系统架构

![架构](https://github.com/zwqgkd/picx-images-hosting/blob/master/kc/%E6%9E%B6%E6%9E%84.png?raw=true)

### ZK数据结构

- metaServer
  - master
    - `<ip>:<port>`
  - slave
    - `<ip>:<port>`
- dataServer
  - `<zone>-<rack>`(`<DataServerInfo>`)
  - `<zone>-<rack>`(`<DataServerInfo>`)
  - `<zone>-<rack>`(`<DataServerInfo>`)
  - `<zone>-<rack>`(`<DataServerInfo>`)

- fileSystem
  - `<fileSystemName>`
    - `<path>`(`<StatInfo>`)
      - `<path>`(`<StatInfo>`)
      - ...
    - ...

### HOW TO USE

1. 启动服务

   `workpublish/bin/start.sh`

2. 使用示例

   ```java
   import com.ksyun.campus.client.*;
   
   public class GetClusterTest{
           public static void main(String[] args){
                   EFileSystem eFileSystem = new EFileSystem();
                   System.out.println(eFileSystem.getClusterInfo());
           }
   }
   ```



##  开发打包

- 打包jar包到`workpulish`下
	
	`build.sh`
	
- 各模块说明

  - **bin:** 项目一键启动脚本，用于编译完成后，上传至服务器上，可以将minFS服务整体启动起来

  - **dataServer:** 主要提供数据内容存储服务能力，单节点无状态设计，可以横向扩容

  - **metaServer:** 主要提供文件系统全局元数据管理，管理dataserver的负载均衡，主备模式运行

  - **easyClient:** 一个功能逻辑简单的SDK，用来与metaServer、dataServer通信，完成文件数据操作

## 终止服务

- `workpublish/bin/down.sh`