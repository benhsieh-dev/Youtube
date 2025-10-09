# YouTube-like Application

## Technologies Used
- Java 20
- Spring Boot
- React
- Docker
- PostgreSQL
- Docker Compose
- Maven

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
### Start Backend (Spring Boot)
### video-platform
- mvn spring-boot:run

### Start Frontend (Angular)
### /frontend
-npm start

## Development Setup

### Demo User Credentials
- **demo**: 
- **demo123**: 

### Stopping Services
```bash
docker-compose down
```

Starting the Application

1. Start SQL Server Database

docker-compose up -d

2. Start Backend API (Spring Boot)

mvn spring-boot:run

3. Start Frontend (React) - In a new terminal

cd frontend && npm start

Access Points

- Frontend (User Interface): http://localhost:4200
- Backend API: http://localhost:8080 (for testing only)
- Database: Postgresql running in Docker


## Quick Start Guide

### Terminal 1: Start database and backend
docker-compose up -d && mvn spring-boot:run

### Terminal 2: Start frontend
cd frontend && npm start

### Close Application 

Stop frontend: Ctrl+C in frontend terminal
Stop backend: Ctrl+C in backend terminal
Stop database: docker-compose down

## Docker Commands
docker exec -it youtube-postgres psql -U postgres -d youtube_db
-- List all tables
\dt

-- Check if users table exists and see its structure
\d users

-- See all users in the database
SELECT * FROM users;

docker system prune -a

## Brew Commands
brew services start postgresql
brew services list | grep postgres
brew services stop postgresql@17


## Status check
lsof -i:8080

## API Gateway Deployment Process   
1. Deploy Lambda services: cd api-gateway && ./deploy.sh prod
2. Update frontend API URL with actual API Gateway endpoint
3. Deploy EC2 application: ./deploy.sh prod
