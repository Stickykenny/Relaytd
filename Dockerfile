FROM eclipse-temurin:21-jdk-ubi9-minimal
ARG JAR_FILE=target/*.jar
COPY ./target/relaytd-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar","/app.jar"]