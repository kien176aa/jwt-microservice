# Stage 1: Build common module
FROM maven:3.9.9-amazoncorretto-21-debian-bookworm AS builder

WORKDIR /app
COPY common ./common
RUN cd common && mvn clean install -DskipTests

# Stage 2: Build swiggy-app
FROM maven:3.9.9-amazoncorretto-21-debian-bookworm AS order-builder

WORKDIR /app
COPY order-service/src ./src
COPY order-service/pom.xml .

# Copy installed dependencies (from stage 1, ~/.m2 if needed)
COPY --from=builder /root/.m2 /root/.m2

RUN mvn clean package -DskipTests

# Stage 3: Runtime image
FROM openjdk:21-jdk-slim

WORKDIR /app
COPY --from=order-builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
