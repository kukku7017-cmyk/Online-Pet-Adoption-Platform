package com.petadoption.service;

import com.petadoption.model.User;

import java.util.List;

public interface UserService {
    User register(String email, String password, String fullName, String phone, String address, String roleStr);
    User authenticate(String email, String password);
    User updateProfile(Long userId, String fullName, String phone, String address);
    void updateUserStatus(Long adminUserId, Long targetUserId, String statusStr);
    User getUserById(Long id);
    List<User> getAllUsers();
    List<User> getUsersByRole(String roleStr);
}
