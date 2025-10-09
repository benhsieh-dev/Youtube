# Microservices Architecture Deployment Guide

## Overview
This application now uses a hybrid microservices architecture:
- **Authentication & User Management**: API Gateway + Lambda (Serverless)
- **Video Platform Core**: EC2 + Docker (Traditional)
- **Database**: PostgreSQL (Shared by both)

## Architecture Diagram
```
Frontend (Angular) 
    ↓
API Gateway
    ├── /auth/* → Lambda Functions (Auth Service)
    ├── /users/* → Lambda Functions (User Service)  
    └── /videos/* → EC2 Spring Boot (Video Service)
         ↓
    PostgreSQL Database (RDS)
```

## Deployment Steps

### 1. Deploy API Gateway + Lambda Services
```bash
cd api-gateway
./deploy.sh prod your-database-password
```

This will:
- Build Java Lambda functions
- Deploy to AWS using SAM
- Create API Gateway endpoints
- Output the API Gateway URL

### 2. Update Frontend Configuration
After step 1, update the API Gateway URL in:
```typescript
// frontend/src/app/services/api.ts
private readonly API_GATEWAY_URL = 'https://YOUR-ACTUAL-API-GATEWAY-ID.execute-api.us-east-1.amazonaws.com/prod';
```

### 3. Deploy EC2 Application
```bash
# SSH to your EC2 instance
./deploy.sh prod
```

## Service Breakdown

### Lambda Services (Serverless)
**Auth Service** (`lambda/auth-service/`):
- POST `/auth/register` - User registration
- POST `/auth/login` - User login  
- GET `/auth/check` - Username availability

**User Service** (`lambda/user-service/`):
- GET `/users/profile` - Get current user profile
- PUT `/users/profile` - Update user profile
- GET `/users/{username}` - Get public user profile

### EC2 Services (Traditional)
**Video Service** (Spring Boot):
- Video upload/streaming
- Video metadata management
- Video processing workflows
- Any heavy computational tasks

## Benefits of This Architecture

### Cost Optimization
- **Lambda**: Pay only when auth/user functions execute
- **EC2**: Always-on for video streaming (better for sustained load)

### Scalability
- **Auth/Users**: Auto-scales with traffic spikes
- **Videos**: Manual scaling but better for predictable load

### Development
- **Independent deployments**: Update auth without touching video service
- **Technology choice**: Could migrate video service to different tech later
- **Team separation**: Different teams can own different services

## Monitoring & Debugging

### Lambda Functions
- **CloudWatch Logs**: Automatic logging for each function
- **X-Ray Tracing**: Request tracing across services
- **Metrics**: Function duration, error rates, invocation count

### EC2 Application
- **Docker logs**: `docker-compose logs -f`
- **Application logs**: Standard Spring Boot logging
- **Health checks**: Built into docker-compose

## Development Workflow

### Local Development
1. **Database**: `docker-compose up postgres`
2. **Backend**: `mvn spring-boot:run` (traditional Spring Boot)
3. **Frontend**: `npm start` (points to local backend for now)

### Testing Lambda Functions Locally
```bash
# Install SAM CLI
pip install aws-sam-cli

# Test auth function locally
cd lambda/auth-service
sam local start-api --template ../../api-gateway/template.yaml
```

## Migration Notes

### What Was Moved to Lambda
- Authentication logic (login, register, username check)
- User profile management (get, update user profiles)
- Stateless operations that benefit from auto-scaling

### What Stayed on EC2
- Video upload/processing (file handling, complex workflows)
- Database-heavy operations
- Long-running processes
- Existing Spring Boot ecosystem integrations

### Database Considerations
- **Shared database**: Both Lambda and EC2 connect to same PostgreSQL
- **Connection pooling**: Lambda uses direct connections, EC2 uses Spring Boot pooling
- **Migrations**: Run database migrations from EC2 application startup

## Future Enhancements
- **JWT Authentication**: Add proper JWT token handling between services
- **API Gateway Authorizers**: Validate tokens at gateway level
- **Service Mesh**: Consider AWS App Mesh for more complex service communication
- **Event-Driven**: Use SQS/SNS for async communication between services