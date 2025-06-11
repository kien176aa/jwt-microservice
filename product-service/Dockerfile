# Stage 1: Build common module
FROM maven:3.9.1-amazoncorretto-19 AS builder

WORKDIR /app
COPY common ./common
RUN cd common && mvn clean install -DskipTests

# Stage 2: Build product-service
FROM maven:3.9.1-amazoncorretto-19 AS product-builder

WORKDIR /app
COPY product-service/src ./src
COPY product-service/pom.xml .

# Copy installed dependencies (from stage 1, ~/.m2 if needed)
COPY --from=builder /root/.m2 /root/.m2

RUN mvn clean package -DskipTests

# Stage 3: Runtime image
FROM openjdk:19-jdk-slim

WORKDIR /app
COPY --from=product-builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]


# # Stage 1: Build common module
# FROM maven:3.9.1-amazoncorretto-19 AS builder

# WORKDIR /app

# # Cache dependencies in Maven to speed up build time for subsequent builds
# COPY common/pom.xml ./common/pom.xml
# RUN mvn dependency:go-offline -B

# # Copy the 'common' module source code and build it
# COPY common/src ./common/src
# RUN cd common && mvn clean install -DskipTests

# # Stage 2: Build product-service
# FROM maven:3.9.1-amazoncorretto-19 AS product-builder

# WORKDIR /app

# # Cache dependencies in Maven for product service to avoid re-downloading
# COPY product-service/pom.xml ./product-service/pom.xml
# RUN mvn dependency:go-offline -B

# # Copy only the src files and build
# COPY product-service/src ./product-service/src
# COPY --from=builder /root/.m2 /root/.m2
# RUN mvn clean package -DskipTests

# # Stage 3: Runtime image
# FROM openjdk:19-jdk-slim

# WORKDIR /app

# # Copy only the built .jar file from the product-builder stage to reduce image size
# COPY --from=product-builder /app/target/*.jar app.jar

# # Run the application
# ENTRYPOINT ["java", "-jar", "app.jar"]
