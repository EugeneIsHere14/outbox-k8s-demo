# Starting with a base image containing Java runtime
FROM eclipse-temurin:21-jdk-jammy

# Metadata
LABEL maintainer="com.demo" version="1.0" description="Payment Service Application"

# Working directory inside the container
WORKDIR /app

# Add application JAR file to the image
COPY target/*.jar payment.jar

# Execute the application
ENTRYPOINT ["java", "-jar", "/app/payment.jar"]
