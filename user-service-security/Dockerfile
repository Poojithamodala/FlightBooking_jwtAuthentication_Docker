FROM eclipse-temurin:17-jre-alpine

WORKDIR /user-service

COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8765

ENTRYPOINT ["java","-jar","app.jar"]
