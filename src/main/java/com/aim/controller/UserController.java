package com.aim.controller;

import com.aim.model.User;
import com.aim.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            log.info("Registering new user with email: {}", user.getEmail());
            
            User createdUser = userService.createUser(user);
            
            log.info("User registered successfully with ID: {}", createdUser.getId());
            
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.error("Validation error during user registration: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error during user registration", e);
            return new ResponseEntity<>("An error occurred during registration", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        try {
            log.info("Login attempt for user: {}", loginRequest.getEmail());
            
            User user = userService.findByEmail(loginRequest.getEmail());
            if (user == null) {
                log.warn("Login failed: User not found with email: {}", loginRequest.getEmail());
                return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
            }
            
            // For MVP, simple password check (in production, use proper password hashing)
            if (!user.getPassword().equals(loginRequest.getPassword())) {
                log.warn("Login failed: Invalid password for user: {}", loginRequest.getEmail());
                return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
            }
            
            log.info("User logged in successfully: {}", user.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error during user login", e);
            return new ResponseEntity<>("An error occurred during login", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        
        User user = userService.findByEmail(email);
        if (user == null) {
            log.warn("User not found with email: {}", email);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user);
    }
}
