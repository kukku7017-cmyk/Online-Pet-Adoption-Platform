package com.petadoption.dao;

import com.petadoption.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    User create(User user);
    void updateProfile(User user);
    void updateStatus(Long userId, User.UserStatus status);
    List<User> findAll();
    List<User> findByRole(User.Role role);
    long countActiveAdmins();
    long countByRole(User.Role role);
    long countTotal();
}
