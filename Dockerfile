# Stage 1: Build the application
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /
COPY --from=builder /app/target/natega-bot.jar /natega-bot.jar
ENTRYPOINT ["java", "-jar", "/natega-bot.jar"]