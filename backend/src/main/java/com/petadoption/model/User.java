package com.petadoption.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

public class User {

    public enum Role {
        ADMIN, SHELTER, ADOPTER;

        public static Role fromString(String roleStr) {
            if (roleStr == null) return null;
            try {
                return Role.valueOf(roleStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum UserStatus {
        ACTIVE, INACTIVE;

        public static UserStatus fromString(String statusStr) {
            if (statusStr == null) return null;
            try {
                return UserStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private Long id;
    private String email;
    @JsonIgnore
    private String passwordHash;
    private String fullName;
    private String phone;
    private String address;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
        this.status = UserStatus.ACTIVE;
    }

    public User(Long id, String email, String passwordHash, String fullName, String phone, String address, Role role, UserStatus status) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.status = status != null ? status : UserStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
