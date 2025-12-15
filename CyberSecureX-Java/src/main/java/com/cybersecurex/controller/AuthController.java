package com.cybersecurex.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Simple demo login: replace with real user store if needed
    private static final String DEMO_USERNAME = "admin";
    private static final String DEMO_PASSWORD = "password123";

    private final com.cybersecurex.service.TokenService tokenService;

    public AuthController(com.cybersecurex.service.TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request) {

        Map<String, Object> result = new HashMap<>();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            result.put("status", "error");
            result.put("message", "Username and password are required");
            return ResponseEntity.badRequest().body(result);
        }

        if (DEMO_USERNAME.equals(username.trim()) && DEMO_PASSWORD.equals(password.trim())) {
            String token = tokenService.issueToken(username.trim());
            result.put("status", "success");
            result.put("message", "Login successful");
            result.put("token", token);
            result.put("user", Map.of(
                    "username", DEMO_USERNAME,
                    "role", "demo-admin",
                    "ip", request.getRemoteAddr()
            ));
            return ResponseEntity.ok(result);
        }

        result.put("status", "error");
        result.put("message", "Invalid credentials");
        return ResponseEntity.status(401).body(result);
    }
}

