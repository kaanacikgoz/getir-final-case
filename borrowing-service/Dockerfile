FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/borrowing-service-0.0.1-SNAPSHOT.jar borrowing-service.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "borrowing-service.jar"]