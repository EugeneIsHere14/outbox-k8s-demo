# Starting with a base image containing Java runtime
FROM openjdk:21-jdk-slim

# Metadata
LABEL maintainer="com.demo" version="1.0" description="Order Service Application"

# Working directory inside the container
WORKDIR /app

# Add application JAR file to the image
COPY target/*.jar order.jar

# Execute the application
ENTRYPOINT ["java", "-jar", "/app/order.jar"]
