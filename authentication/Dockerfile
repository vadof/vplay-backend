FROM openjdk:17

WORKDIR /app

COPY target/authentication-0.0.1-SNAPSHOT.jar /app/authentication.jar

ENTRYPOINT ["java", "-jar", "authentication.jar"]
