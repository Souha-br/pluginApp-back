package com.example.test1.controllers;

import com.example.test1.dto.SignupRequest;
import com.example.test1.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/signup")
public class SignupController {
    private final AuthService authService;

    @Autowired
    public SignupController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> signupUser(@RequestBody SignupRequest signupRequest) {
        boolean isUserCreated = authService.createUser(signupRequest);

        Map<String, Object> response = new HashMap<>();

        if (isUserCreated) {
            response.put("success", true);
            response.put("message", "User created successfully");
            response.put("name", signupRequest.getUsername()); // assuming SignupRequest has a name field
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("success", false);
            response.put("message", "Failed to create user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
