Java Motivational Quotes App

This project is a simple Java-based HTTP server that serves random motivational quotes via a REST API. The quotes are externalized to a quotes.txt file for easy customization.

Features

Serves random motivational quotes in JSON format.

Uses an external quotes.txt file for configurable quotes.

Lightweight HTTP server using com.sun.net.httpserver.HttpServer.

Dockerized for easy deployment.

Requirements

Java 17+

Maven (if building from source)

Docker (optional, for containerized deployment)

Setup and Usage
Running Locally

Clone the repository:

git clone git@github.com:Satyams-git/motivational-quotes.git
cd motivational-quotes


Ensure quotes.txt exists in the project directory and contains quotes (one per line).

Compile and run the application:

javac src/Main.java -d out
java -cp out Main


The server will start on http://localhost:8000/.

Test the API using:

curl http://localhost:8000/

Running with Docker

Build the Docker image:

docker build -t motivational-quotes-api .


Run the container:

docker run -p 8000:8000 motivational-quotes-api


Access the API at http://localhost:8000/.

File Structure
project-root/
│── src/
│   └── Main.java
│── quotes.txt
│── Dockerfile
│── README.md
│── target/
│   └── myapp.jar (if using Maven build)

Customizing Quotes

To customize the quotes, edit quotes.txt and restart the application. Each quote should be on a new line.

License

This project is licensed under the MIT License.

Author

qualibytes-Satyam