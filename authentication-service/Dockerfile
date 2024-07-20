FROM openjdk:17

WORKDIR /app

COPY target/authentication-service-0.0.1-SNAPSHOT.jar /app/authentication-service.jar

ENTRYPOINT ["java", "-jar", "authentication-service.jar"]
