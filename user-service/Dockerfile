FROM openjdk:21

WORKDIR /app

EXPOSE 8100

COPY target/*.jar user.jar

ENTRYPOINT ["java", "-jar", "user.jar"]