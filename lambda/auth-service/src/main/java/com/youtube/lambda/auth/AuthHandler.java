package com.youtube.lambda.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.lambda.auth.dto.LoginRequest;
import com.youtube.lambda.auth.dto.RegisterRequest;
import com.youtube.lambda.auth.model.User;
import com.youtube.lambda.auth.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

public class AuthHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository = new UserRepository();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String path = input.getPath();
            String method = input.getHttpMethod();
            
            context.getLogger().log("Processing request: " + method + " " + path);
            
            if ("POST".equals(method)) {
                if (path.endsWith("/register")) {
                    return handleRegister(input);
                } else if (path.endsWith("/login")) {
                    return handleLogin(input);
                }
            } else if ("GET".equals(method) && path.endsWith("/check")) {
                return handleUsernameCheck(input);
            }
            
            return createResponse(404, createErrorResponse("Endpoint not found"));
            
        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            return createResponse(500, createErrorResponse("Internal server error"));
        }
    }
    
    private APIGatewayProxyResponseEvent handleRegister(APIGatewayProxyRequestEvent input) {
        try {
            RegisterRequest request = objectMapper.readValue(input.getBody(), RegisterRequest.class);
            
            // Validate request
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return createResponse(400, createErrorResponse("Username is required"));
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return createResponse(400, createErrorResponse("Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return createResponse(400, createErrorResponse("Password must be at least 6 characters"));
            }
            
            // Check if user already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                return createResponse(400, createErrorResponse("Username already exists"));
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                return createResponse(400, createErrorResponse("Email already exists"));
            }
            
            // Create new user
            User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
            );
            
            user = userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            
            return createResponse(200, response);
            
        } catch (Exception e) {
            return createResponse(400, createErrorResponse("Invalid request: " + e.getMessage()));
        }
    }
    
    private APIGatewayProxyResponseEvent handleLogin(APIGatewayProxyRequestEvent input) {
        try {
            LoginRequest request = objectMapper.readValue(input.getBody(), LoginRequest.class);
            
            // Validate request
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return createResponse(400, createErrorResponse("Username is required"));
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return createResponse(400, createErrorResponse("Password is required"));
            }
            
            // Find user
            User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
            
            if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return createResponse(400, createErrorResponse("Invalid username or password"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            
            return createResponse(200, response);
            
        } catch (Exception e) {
            return createResponse(400, createErrorResponse("Invalid request: " + e.getMessage()));
        }
    }
    
    private APIGatewayProxyResponseEvent handleUsernameCheck(APIGatewayProxyRequestEvent input) {
        try {
            String username = input.getQueryStringParameters().get("username");
            
            if (username == null || username.trim().isEmpty()) {
                return createResponse(400, createErrorResponse("Username parameter is required"));
            }
            
            boolean exists = userRepository.existsByUsername(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("available", !exists);
            
            return createResponse(200, response);
            
        } catch (Exception e) {
            return createResponse(400, createErrorResponse("Invalid request: " + e.getMessage()));
        }
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