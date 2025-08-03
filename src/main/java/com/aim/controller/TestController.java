package com.aim.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test")
@Slf4j
public class TestController {

    @GetMapping("/cors")
    public ResponseEntity<String> testCors() {
        log.info("CORS test endpoint called");
        return ResponseEntity.ok("CORS is working! Backend is accessible from frontend.");
    }

    @PostMapping("/cors")
    public ResponseEntity<String> testCorsPost(@RequestBody String data) {
        log.info("CORS POST test endpoint called with data: {}", data);
        return ResponseEntity.ok("CORS POST is working! Received: " + data);
    }
} 