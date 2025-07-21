package com.example.rate_limit.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SampleController {

    @GetMapping("/api/test")
    public ResponseEntity<Map<String, Object>> testEndpoint(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Request successful!");
        response.put("timestamp", Instant.now().toString());
        response.put("userId", userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/status")
    public ResponseEntity<Map<String, Object>> statusEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }
}

