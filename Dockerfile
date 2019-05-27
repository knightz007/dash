FROM openjdk:8-jdk-alpine
VOLUME /tmp
# ARG JAR_FILE
# COPY ${JAR_FILE} app.jar
COPY dash-1.0-SNAPSHOT.jar /opt/app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/opt/app.jar"]
