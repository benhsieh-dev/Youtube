# YouTube-like Application

## Quick Start

### 1. Start SQL Server
```bash
docker-compose up -d
```

### 2. Create Database
Connect to SQL Server and create the database:
```sql
CREATE DATABASE youtube_db;
```

### 3. Run Application
```bash
mvn spring-boot:run
```

## Development Setup

### Prerequisites
- Java 20
- Maven 3.6+
- Docker

### Database Access
- **Host**: localhost:1433
- **Username**: sa
- **Password**: YoutubeApp123!
- **Database**: youtube_db

### Stopping Services
```bash
docker-compose down
```

Starting the Application

1. Start SQL Server Database

docker-compose up -d

2. Start Backend API (Spring Boot)

export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-20.jdk/Contents/Home && mvn spring-boot:run

3. Start Frontend (React) - In a new terminal

cd frontend && npm start

Access Points

- Frontend (User Interface): http://localhost:3000
- Backend API: http://localhost:8080 (for testing only)
- Database: SQL Server running in Docker


## Quick Start Guide

### Terminal 1: Start database and backend
docker-compose up -d && export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-20.jdk/Contents/Home && mvn spring-boot:run

### Terminal 2: Start frontend
cd frontend && npm start

### Stop Everything

Stop frontend: Ctrl+C in frontend terminal
Stop backend: Ctrl+C in backend terminal
Stop database: docker-compose down
