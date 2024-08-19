FROM openjdk:8-jdk-alpine

WORKDIR /deploy

COPY ./target/metaServer-1.0.jar /deploy

EXPOSE 8000

ENTRYPOINT ["sh", "-c", "exec java -jar /deploy/metaServer-1.0.jar"]
