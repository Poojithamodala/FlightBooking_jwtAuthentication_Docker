FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/flight-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8092

ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=docker"]
