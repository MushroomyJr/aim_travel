package com.aim.service;

import com.aim.model.User;

public interface UserService {
    User createUser(User user);
    User findById(Long id);
    User findByEmail(String email);
} 