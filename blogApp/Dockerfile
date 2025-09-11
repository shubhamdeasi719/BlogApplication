# Start from a Java runtime image
FROM eclipse-temurin:21-jdk

# Set the working directory
WORKDIR /app

# Copy the jar file (build your jar before using this Dockerfile)
COPY target/blogApp-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app will run on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
