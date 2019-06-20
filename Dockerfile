FROM knights007/alpine-jdk:latest
ARG JAR_FILE
COPY ${JAR_FILE} /opt/app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/opt/app.jar"]