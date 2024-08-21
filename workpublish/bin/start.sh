cd /root/project/minfs/workpublish
## kill java
ps -ef | grep "java -jar" | grep -v grep | awk '{print $2}' | xargs kill -9
## run zks
docker container stop zk-register zk-master-meta zk-slave-meta
docker container prune -f
docker volume rm zk-register-v zk-master-meta-v zk-slave-meta-v

docker volume create zk-register-v
docker volume create zk-master-meta-v
docker volume create zk-slave-meta-v

docker run --name zk-register -p 2181:2181 -v zk-register-v:/data --rm -d zookeeper:3.9
docker run --name zk-master-meta -p 2182:2181 -v zk-master-meta-v:/data --rm -d zookeeper:3.9
docker run --name zk-slave-meta -p 2183:2181 -v zk-slave-meta-v:/data --rm -d zookeeper:3.9

start_jar() {
  jar_id=$1
  jar_name=$2
  log_name=$3
  args=$4

  echo "Starting $jar_id..."
  nohup java -jar $jar_name $args > $log_name &
  echo "$jar_id started successfully. pid: $!"
}

mkdir -p ./dataServer/log
start_jar "dataServer1" "./dataServer/dataServer-1.0.jar" "./dataServer/log/dataServer1.log" "--server.port=9000 --az.rack=rack0 --az.zone=zone0"
start_jar "dataServer2" "./dataServer/dataServer-1.0.jar" "./dataServer/log/dataServer2.log" "--server.port=9001 --az.rack=rack1 --az.zone=zone1"
start_jar "dataServer3" "./dataServer/dataServer-1.0.jar" "./dataServer/log/dataServer3.log" "--server.port=9002 --az.rack=rack2 --az.zone=zone2"
start_jar "dataServer4" "./dataServer/dataServer-1.0.jar" "./dataServer/log/dataServer4.log" "--server.port=9003 --az.rack=rack3 --az.zone=zone3"

mkdir -p ./metaServer/log
start_jar "metaServer-master" "./metaServer/metaServer-1.0.jar" "./metaServer/log/metaServer-master.log" "--server.port=8000"
sleep 3
start_jar "metaServer-slave" "./metaServer/metaServer-1.0.jar" "./metaServer/log/metaServer-slave.log" "--server.port=8001"

