mvn clean package -Dmaven.test.skip=true -U

#docker build
docker buildx build -t minfs-meta-server:latest ./metaServer
docker buildx build -t minfs-data-server:latest ./dataServer

#docker compose
docker compose -p minfs up -d