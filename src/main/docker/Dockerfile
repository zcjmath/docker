FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD docker-1.0.1.jar app.jar
ADD classes/application.properties application.properties
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]