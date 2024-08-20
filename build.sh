# !/bin/env bash

## 实现一键编译整个项目
mvn -Dmaven.test.skip=true -U clean package

cp ./metaServer/target/metaServer-1.0.jar ./workpublish/metaServer
cp ./dataServer/target/dataServer-1.0.jar ./workpublish/dataServer
cp ./easyClient/target/easyClient-1.0.jar ./workpublish/easyClient