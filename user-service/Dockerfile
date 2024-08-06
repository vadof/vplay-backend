FROM openjdk:17

WORKDIR /app

COPY target/user-0.0.1-SNAPSHOT.jar /app/user.jar

ENTRYPOINT ["java", "-jar", "user.jar"]
