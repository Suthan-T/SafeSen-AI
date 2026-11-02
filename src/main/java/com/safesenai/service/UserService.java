package com.safesenai.service;

import com.safesenai.dto.RegistrationDto;
import com.safesenai.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerNewUser(RegistrationDto registrationDto);
    Optional<User> findByEmail(String email);
    User findById(Long id);
    List<User> findAllUsers();
    void toggleUserStatus(Long userId);
    long countTotalUsers();
}

