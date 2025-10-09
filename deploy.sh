#!/bin/bash

# AWS EC2 Docker Deployment Script
# Usage: ./deploy.sh [environment]

set -e

ENVIRONMENT=${1:-prod}
DOCKER_COMPOSE_FILE="docker-compose.${ENVIRONMENT}.yml"

echo "Starting deployment for environment: $ENVIRONMENT"

# Check if docker-compose file exists
if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
    echo "Error: $DOCKER_COMPOSE_FILE not found"
    exit 1
fi

# Load environment variables if .env file exists
if [ -f ".env.${ENVIRONMENT}" ]; then
    echo "Loading environment variables from .env.${ENVIRONMENT}"
    export $(cat .env.${ENVIRONMENT} | xargs)
elif [ -f ".env" ]; then
    echo "Loading environment variables from .env"
    export $(cat .env | xargs)
fi

# Build and deploy
echo "Building Docker images..."
docker-compose -f $DOCKER_COMPOSE_FILE build --no-cache

echo "Stopping existing containers..."
docker-compose -f $DOCKER_COMPOSE_FILE down

echo "Removing unused images..."
docker image prune -f

echo "Starting services..."
docker-compose -f $DOCKER_COMPOSE_FILE up -d

echo "Waiting for services to be healthy..."
sleep 30

# Health check
echo "Checking service health..."
if docker-compose -f $DOCKER_COMPOSE_FILE ps | grep -q "Up (healthy)"; then
    echo "Deployment successful!"
    echo "Frontend: http://$(curl -s ifconfig.me)"
    echo "Backend API: http://$(curl -s ifconfig.me):8080"
    echo "View logs: docker-compose -f $DOCKER_COMPOSE_FILE logs -f"
else
    echo "Some services are not healthy. Check logs:"
    docker-compose -f $DOCKER_COMPOSE_FILE logs
    exit 1
fi