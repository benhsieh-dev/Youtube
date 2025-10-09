package com.youtube.lambda.user;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.youtube.lambda.user.model.User;
import com.youtube.lambda.user.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class UserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository = new UserRepository();
    
    public UserHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String path = input.getPath();
            String method = input.getHttpMethod();
            
            context.getLogger().log("Processing request: " + method + " " + path);
            
            if ("GET".equals(method)) {
                if (path.endsWith("/profile")) {
                    return handleGetCurrentUserProfile(input);
                } else if (path.matches(".*/users/[^/]+$")) {
                    return handleGetUserProfile(input);
                }
            } else if ("PUT".equals(method) && path.endsWith("/profile")) {
                return handleUpdateProfile(input);
            }
            
            return createResponse(404, createErrorResponse("Endpoint not found"));
            
        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            return createResponse(500, createErrorResponse("Internal server error"));
        }
    }
    
    private APIGatewayProxyResponseEvent handleGetCurrentUserProfile(APIGatewayProxyRequestEvent input) {
        try {
            // Extract user ID from JWT token (simplified - in real implementation you'd validate JWT)
            String authHeader = input.getHeaders().get("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return createResponse(401, createErrorResponse("Authorization header required"));
            }
            
            // For demo purposes, extract userId from query parameter
            // In real implementation, decode JWT token
            String userIdStr = input.getQueryStringParameters() != null ? 
                input.getQueryStringParameters().get("userId") : null;
            
            if (userIdStr == null) {
                return createResponse(400, createErrorResponse("User ID required"));
            }
            
            Long userId = Long.parseLong(userIdStr);
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("displayName", user.getDisplayName());
            response.put("profileImageUrl", user.getProfileImageUrl());
            response.put("createdAt", user.getCreatedAt());
            
            return createResponse(200, response);
            
        } catch (Exception e) {
            return createResponse(400, createErrorResponse("Invalid request: " + e.getMessage()));
        }
    }
    
    private APIGatewayProxyResponseEvent handleGetUserProfile(APIGatewayProxyRequestEvent input) {
        try {
            String path = input.getPath();
            String username = path.substring(path.lastIndexOf('/') + 1);
            
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("displayName", user.getDisplayName());
            response.put("profileImageUrl", user.getProfileImageUrl());
            response.put("createdAt", user.getCreatedAt());
            
            return createResponse(200, response);
            
        } catch (Exception e) {
            return createResponse(404, createErrorResponse("User not found"));
        }
    }
    
    private APIGatewayProxyResponseEvent handleUpdateProfile(APIGatewayProxyRequestEvent input) {
        try {
            // Extract user ID from JWT token (simplified)
            String userIdStr = input.getQueryStringParameters() != null ? 
                input.getQueryStringParameters().get("userId") : null;
            
            if (userIdStr == null) {
                return createResponse(400, createErrorResponse("User ID required"));
            }
            
            Long userId = Long.parseLong(userIdStr);
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Parse update request
            @SuppressWarnings("unchecked")
            Map<String, String> updates = objectMapper.readValue(input.getBody(), Map.class);
            
            boolean updated = false;
            
            if (updates.containsKey("displayName")) {
                user.setDisplayName(updates.get("displayName"));
                updated = true;
            }
            
            if (updates.containsKey("profileImageUrl")) {
                user.setProfileImageUrl(updates.get("profileImageUrl"));
                updated = true;
            }
            
            if (updated) {
                user = userRepository.save(user);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("user", createUserResponse(user));
            
            return createResponse(200, response);
            
        } catch (Exception e) {
            return createResponse(400, createErrorResponse("Unable to update profile: " + e.getMessage()));
        }
    }
    
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("displayName", user.getDisplayName());
        userResponse.put("profileImageUrl", user.getProfileImageUrl());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("updatedAt", user.getUpdatedAt());
        return userResponse;
    }
    
    private APIGatewayProxyResponseEvent createResponse(int statusCode, Object body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,Authorization");
        headers.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        response.setHeaders(headers);
        
        try {
            response.setBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            response.setBody("{\"error\":\"Failed to serialize response\"}");
        }
        
        return response;
    }
    
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}