# Stage 1: Build common module
FROM maven:3.9.9-amazoncorretto-21-debian-bookworm AS builder

WORKDIR /app
COPY common ./common
RUN cd common && mvn clean install -DskipTests

# Stage 2: Build notification-service
FROM maven:3.9.9-amazoncorretto-21-debian-bookworm AS notification-builder

WORKDIR /app
COPY notification-service/src ./src
COPY notification-service/pom.xml .

# Copy installed dependencies (from stage 1, ~/.m2 if needed)
COPY --from=builder /root/.m2 /root/.m2

RUN mvn clean package -DskipTests

# Stage 3: Runtime image
FROM openjdk:21-jdk-slim

WORKDIR /app
COPY --from=notification-builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

