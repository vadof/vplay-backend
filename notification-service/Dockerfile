FROM openjdk:21

WORKDIR /app

EXPOSE 8500

COPY target/*.jar notification.jar

ENTRYPOINT ["java", "-jar", "notification.jar"]