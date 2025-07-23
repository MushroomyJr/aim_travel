package com.aim.repository;

import com.aim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Additional query methods (if needed) can be defined here
    User findByEmail(String email);
} 