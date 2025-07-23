FROM openjdk:21

WORKDIR /app

EXPOSE 8350

COPY target/*.jar odds.jar

ENTRYPOINT ["java", "-jar", "odds.jar"]