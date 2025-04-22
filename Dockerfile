# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build

# Set Gradle version
ENV GRADLE_VERSION=8.13

# Install necessary packages
RUN apk add --no-cache \
    wget \
    unzip \
    bash

# Download and install Gradle
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    rm /tmp/gradle-${GRADLE_VERSION}-bin.zip

# Add Gradle to PATH
ENV PATH=$PATH:/opt/gradle/gradle-${GRADLE_VERSION}/bin

# Set working directory
WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build the application
RUN gradle build -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built artifact from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

