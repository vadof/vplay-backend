FROM openjdk:21

WORKDIR /app

EXPOSE 8200

COPY target/*.jar clicker.jar

ENTRYPOINT ["java", "-jar", "clicker.jar"]