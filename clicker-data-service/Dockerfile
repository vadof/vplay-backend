FROM openjdk:21

WORKDIR /app

EXPOSE 8250

COPY target/*.jar clicker-data.jar

ENTRYPOINT ["java", "-jar", "clicker-data.jar"]