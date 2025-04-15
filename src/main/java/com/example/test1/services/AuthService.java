package com.example.test1.services;

import com.example.test1.dto.SignupRequest;

public interface AuthService {
    boolean createUser(SignupRequest signupRequest);
}
