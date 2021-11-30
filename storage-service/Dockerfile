#FROM maven:3.6.1-jdk-8-slim AS build
#RUN mkdir -p /workspace
#WORKDIR /workspace
#COPY pom.xml /workspace
#RUN mvn dependency:go-offline
#
#COPY src /workspace/src
#RUN mvn -f pom.xml clean package
#
#FROM openjdk:8-alpine
#COPY --from=build /workspace/target/*.jar app.jar
## EXPOSE 9000
#ENTRYPOINT ["java","-jar","app.jar"]

FROM openjdk:8-alpine
COPY /target/*.jar storage-service.jar
#EXPOSE 9000
ENTRYPOINT ["java","-jar","storage-service.jar"]