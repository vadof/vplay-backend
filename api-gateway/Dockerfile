FROM openjdk:21

WORKDIR /app

EXPOSE 8080

COPY target/*.jar /app/api-gateway.jar

ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
