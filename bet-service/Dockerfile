FROM openjdk:21

WORKDIR /app

EXPOSE 8300

COPY target/*.jar bet.jar

ENTRYPOINT ["java", "-jar", "bet.jar"]