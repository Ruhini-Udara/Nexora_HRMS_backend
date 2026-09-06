# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application (skip compiling tests and limit memory to prevent EC2 OOM crashes)
ENV MAVEN_OPTS="-Xmx512m"
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Create the lightweight runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port (assuming default 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
