FROM openjdk:21

WORKDIR /app

EXPOSE 8400

COPY target/*.jar wallet.jar

ENTRYPOINT ["java", "-jar", "wallet.jar"]