# Learning Management System (LMS) - Microservices

A web-based Learning Management System (LMS) built using Spring Boot microservices architecture to facilitate online learning, assessments, and progress tracking. This system supports multiple user roles (Admin, Instructor, Student) and provides essential features like secure authentication (JWT), course management, assessments, grading, and notifications.

## Microservices Architecture

The system is composed of the following microservices:

1. **Discovery Service** (Port: 8761)
   - Service registry using Netflix Eureka
   - Central service discovery for all microservices

2. **Config Service** (Port: 8888)
   - Centralized configuration server using Spring Cloud Config
   - Stores all configuration in a Git repository
   - Enables dynamic configuration changes without redeploying services

3. **API Gateway** (Port: 8080)
   - Acts as a single entry point for all client requests
   - Routes requests to appropriate microservices
   - Handles JWT token validation

4. **Auth Service** (Port: 8083)
   - Handles user authentication and authorization
   - Issues JWT tokens
   - Manages user registration, login, and profile management

## Prerequisites

Ensure you have the following installed before running the application:
- Java 17+
- MySQL 8.0+
- Maven 3.6+

## Setup Instructions

### 1. Clone the repository
```bash
git clone https://github.com/yourusername/lms-microservices.git
cd lms-microservices
```

### 2. Set up the databases
Create MySQL databases for each service that requires it:
```sql
CREATE DATABASE auth_service_db;
```

### 3. Build all microservices
```bash
mvn clean install
```

### 4. Run the services in the following order:

#### a. Start Config Service
```bash
cd config-service
mvn spring-boot:run
```

#### b. Start Discovery Service
```bash
cd ../discovery-service
mvn spring-boot:run
```

#### c. Start Auth Service
```bash
cd ../auth-service
mvn spring-boot:run
```

#### d. Start API Gateway
```bash
cd ../api-gateway
mvn spring-boot:run
```

### 5. Access the services

- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Auth Service (direct, not recommended for production): http://localhost:8083

## API Endpoints

### Auth Service

- `POST /auth/register` - Register a new user
- `POST /auth/login` - Authenticate a user and get JWT token
- `POST /auth/refresh` - Refresh an expired JWT token
- `GET /auth/me` - Get current user info
- `POST /auth/forgot-password` - Request password reset
- `POST /auth/reset-password` - Reset password with token

### User Service (implemented in Auth Service)

- `GET /api/users` - Get all users (Admin only)
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user profile
- `DELETE /api/users/{id}` - Delete user (Admin only)
- `GET /api/users/by-role/{role}` - Get users by role (Admin or Instructor only)

## Future Enhancements

In the next phase, we'll add the following microservices:
- Course Service
- Content Service
- Assessment Service
- Notification Service
