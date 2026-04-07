package com.sentinel.auth;

import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        // Simple mock authentication
        String token = jwtService.generateToken(request.getUserId());
        return Map.of("token", token, "status", "SUCCESS");
    }

    @Data
    public static class LoginRequest {
        private String userId;
        private String password;
    }
}
