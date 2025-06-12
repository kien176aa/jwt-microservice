# Dockerfile cho swiggy-app (đặt trong swiggy-app/)
FROM eclipse-temurin:21-jre

# Tạo user non-root
RUN addgroup --system spring && adduser --system spring --ingroup spring

WORKDIR /app

# Copy JAR file đã build (fat jar có chứa common)
COPY target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

# Swiggy-app thường chạy trên port 8081
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]