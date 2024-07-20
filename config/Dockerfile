FROM openjdk:17

WORKDIR /app

COPY target/config-0.0.1-SNAPSHOT.jar /app/config.jar

ENTRYPOINT ["java", "-jar", "config.jar"]
