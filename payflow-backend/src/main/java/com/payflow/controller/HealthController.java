package com.payflow.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> checkHealth() {
        return Map.of(
            "status", "UP",
            "application", "PayFlow Backend"
        );
    }
}