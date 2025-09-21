package com.youtube.app.controller;

import com.youtube.app.model.User;
import com.youtube.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("displayName", user.getDisplayName());
            response.put("profileImageUrl", user.getProfileImageUrl());
            response.put("createdAt", user.getCreatedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unable to get user profile");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        try {
            User user = userService.findByUsername(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("displayName", user.getDisplayName());
            response.put("profileImageUrl", user.getProfileImageUrl());
            response.put("createdAt", user.getCreatedAt());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
        Authentication authentication,
        @RequestBody Map<String, String> updates
    ) {
        try {
            User user = (User) authentication.getPrincipal();
            
            if (updates.containsKey("displayName")) {
                user.setDisplayName(updates.get("displayName"));
            }
            
            if (updates.containsKey("profileImageUrl")) {
                user.setProfileImageUrl(updates.get("profileImageUrl"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unable to update profile");
            return ResponseEntity.badRequest().body(error);
        }
    }
}