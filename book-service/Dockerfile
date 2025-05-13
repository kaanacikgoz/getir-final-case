FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/book-service-0.0.1-SNAPSHOT.jar book-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "book-service.jar"]