FROM openjdk:21

WORKDIR /app

EXPOSE 8761

COPY target/*.jar eureka.jar

ENTRYPOINT ["java", "-jar", "eureka.jar"]