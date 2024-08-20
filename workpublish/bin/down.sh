# debug
cd /mnt/c/Projects/kc/minfs_student/workpublish/bin
cd ..
## kill java
ps -ef | grep java | grep -v grep | awk '{print $2}' | xargs kill -9
## run zks
docker volume rm zk-register-v zk-master-meta-v zk-slave-meta-v
docker container stop zk-register zk-master-meta zk-slave-meta