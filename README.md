# Kubernetes Task Manager

A Spring Boot application that manages tasks using Kubernetes and MongoDB.

## Features
- Task creation and management
- Kubernetes-based task execution
- MongoDB persistence
- RESTful API

## Technologies
- Spring Boot
- Kubernetes
- MongoDB
- Docker

## Setup
1. Build the application:
   ```bash
   mvn clean package
   docker build -t taskmanager:latest .
   ```

2. Deploy to Kubernetes:
   ```bash
   kubectl apply -f k8s/
   ```

3. Access the API:
   ```bash
   curl http://localhost/api/tasks
   ```

## Project Structure
- `src/` - Source code
- `k8s/` - Kubernetes configuration files
- `Dockerfile` - Docker build configuration