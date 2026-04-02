# eclipse-temurin is the official successor to openjdk on Docker Hub
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy source and quotes file
COPY src/Main.java Main.java
COPY quotes.txt quotes.txt

# Compile the Java source
RUN javac Main.java

# Expose the HTTP server port
EXPOSE 8000

# Run the application
CMD ["java", "Main"]
