# Build stage
FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /app

# Copy gradle files first for better layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Environment variables for configuration
ENV SPRING_APPLICATION_NAME=rinha2025
ENV PAYMENT_SERVER_DEFAULT_URL=http://localhost:8081/process-payment
ENV PAYMENT_SERVER_FALLBACK_URL=http://localhost:8082/process-payment
ENV PAYMENT_SERVER_RETRY_MAX_ATTEMPTS=2
ENV PAYMENT_SERVER_RETRY_DELAY_MS=500

# Expose ports
EXPOSE 8080
EXPOSE 6001
EXPOSE 6002

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]