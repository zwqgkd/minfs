cd /root/project/minfs/workpublish
## kill java
ps -ef | grep "java -jar" | grep -v grep | awk '{print $2}' | xargs kill -9
## run zks
docker container stop zk-register zk-master-meta zk-slave-meta
docker container prune -f
docker volume rm zk-register-v zk-master-meta-v zk-slave-meta-v