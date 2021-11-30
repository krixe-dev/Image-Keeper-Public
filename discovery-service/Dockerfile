#FROM maven:3.6.1-jdk-8-slim AS build
#RUN mkdir -p /workspace
#WORKDIR /workspace
#COPY pom.xml /workspace
#COPY src /workspace/src
#RUN mvn -f pom.xml clean package

FROM openjdk:8-alpine
COPY /target/*.jar discovery-service.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","discovery-service.jar"]