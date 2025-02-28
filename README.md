# Task Manager API with Kubernetes

A Spring Boot application that provides a REST API for managing and executing shell commands as tasks in a Kubernetes pod environment. The application uses MongoDB for data persistence.

## Technologies Used
- Java 17
- Spring Boot 3.2.2
- MongoDB 4.4
- Kubernetes
- Docker
- Maven

## Implementation Details
1. **Task Management**
   - Create, read, and execute tasks
   - Tasks are executed in isolated Kubernetes pods
   - Results are stored in MongoDB

2. **Kubernetes Integration**
   - Separate pods for MongoDB and TaskManager
   - Persistent volume for MongoDB data
   - LoadBalancer service for API access

3. **MongoDB Configuration**
   - Persistent storage using PVC
   - Secure authentication
   - Database: taskmanager

## Screenshots

### 1. Kubernetes Deployment Status
![image](https://github.com/user-attachments/assets/14ed6ab7-efcf-4c50-a452-91c34ea7b992)

- Shows both MongoDB and TaskManager pods running
- Date: 28-02-2024
- Name: Sugash S M

### 2. Services Configuration
![image](https://github.com/user-attachments/assets/4078d708-4214-42d7-a75e-098bd01e79f1)

- Shows MongoDB and TaskManager services
- LoadBalancer configuration
- Date: 28-02-2024
- Name: Sugash S M

### 3. Persistent Volume
![image](https://github.com/user-attachments/assets/70ea6644-31ef-4145-91b8-0d29c004299f)

- Shows MongoDB PVC status
- Date: 28-02-2024
- Name: Sugash S M

### 4. API Testing
![API Test](screenshots/api.png)
- Shows successful API response
- Date: 28-02-2024
- Name: Sugash S M

## Project Structure
```
kubernetes-task-manager/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/taskmanager/
│       │       ├── TaskManagerApplication.java
│       │       ├── controller/
│       │       ├── model/
│       │       ├── repository/
│       │       └── service/
│       └── resources/
│           └── application.properties
├── k8s/
│   ├── mongodb-deployment.yaml
│   ├── mongodb-pvc.yaml
│   ├── mongodb-secret.yaml
│   ├── mongodb-service.yaml
│   ├── taskmanager-deployment.yaml
│   └── taskmanager-service.yaml
├── Dockerfile
├── pom.xml
└── README.md
```

## Author
- Name: Sugash S M
- Date: 28-02-2024
