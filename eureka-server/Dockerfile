FROM openjdk:21

WORKDIR /app

COPY target/eureka-server-0.0.1-SNAPSHOT.jar /app/eureka.jar

ENTRYPOINT ["java", "-jar", "eureka.jar"]
