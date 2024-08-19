FROM openjdk:8-jdk-alpine

WORKDIR /deploy

COPY ./target/dataServer-1.0.jar /deploy

EXPOSE 9000

ENTRYPOINT ["sh", "-c", "exec java -jar /deploy/dataServer-1.0.jar"]
