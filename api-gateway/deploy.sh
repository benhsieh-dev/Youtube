#!/bin/bash

# Deploy API Gateway and Lambda functions using SAM
# Usage: ./deploy.sh [environment] [db-password]

set -e

ENVIRONMENT=${1:-prod}
DB_PASSWORD=${2:-postgres}
STACK_NAME="video-platform-api-${ENVIRONMENT}"
S3_BUCKET="video-platform-sam-artifacts-${ENVIRONMENT}"

echo "Deploying API Gateway and Lambda functions for environment: $ENVIRONMENT"

# Check if AWS CLI is configured
if ! aws sts get-caller-identity > /dev/null 2>&1; then
    echo "Error: AWS CLI not configured. Run 'aws configure' first."
    exit 1
fi

# Check if SAM CLI is installed
if ! command -v sam &> /dev/null; then
    echo "Error: SAM CLI not found. Install it first:"
    echo "pip install aws-sam-cli"
    exit 1
fi

# Build Lambda functions
echo "Building Lambda functions..."

# Build Auth Service
echo "Building auth service..."
cd ../lambda/auth-service
mvn clean package -q
cd ../../api-gateway

# Build User Service
echo "Building user service..."
cd ../lambda/user-service
mvn clean package -q
cd ../../api-gateway

# Create S3 bucket for SAM artifacts if it doesn't exist
if ! aws s3 ls "s3://${S3_BUCKET}" 2>&1 | grep -q 'NoSuchBucket'; then
    echo "S3 bucket ${S3_BUCKET} already exists"
else
    echo "Creating S3 bucket ${S3_BUCKET}..."
    aws s3 mb "s3://${S3_BUCKET}"
fi

# Deploy using SAM
echo "Deploying stack ${STACK_NAME}..."

sam deploy \
    --template-file template.yaml \
    --stack-name "$STACK_NAME" \
    --s3-bucket "$S3_BUCKET" \
    --capabilities CAPABILITY_IAM \
    --parameter-overrides \
        Environment="$ENVIRONMENT" \
        DatabasePassword="$DB_PASSWORD" \
    --no-confirm-changeset \
    --no-fail-on-empty-changeset

# Get the API Gateway URL
API_URL=$(aws cloudformation describe-stacks \
    --stack-name "$STACK_NAME" \
    --query 'Stacks[0].Outputs[?OutputKey==`ApiGatewayUrl`].OutputValue' \
    --output text)

echo "Deployment completed successfully!"
echo "API Gateway URL: $API_URL"
echo ""
echo "Available endpoints:"
echo "POST $API_URL/auth/register"
echo "POST $API_URL/auth/login"
echo "GET  $API_URL/auth/check?username=testuser"
echo "GET  $API_URL/users/profile?userId=1"
echo "PUT  $API_URL/users/profile?userId=1"
echo "GET  $API_URL/users/username"