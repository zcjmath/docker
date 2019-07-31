FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD docker-1.0.1.jar /opt/app.jar
RUN sh -c 'touch /opt/app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]