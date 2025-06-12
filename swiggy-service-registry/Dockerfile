FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR /app

# Copy common module trước
COPY ../common ./common/

# Copy service hiện tại
COPY pom.xml .
COPY src ./src

# Build common trước
RUN cd common && mvn clean install -DskipTests

# Build service hiện tại
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]