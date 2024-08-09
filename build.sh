#!/bin/env bash

## 实现一键编译整个项目
mvn -Dmaven.test.skip=true -U clean package
