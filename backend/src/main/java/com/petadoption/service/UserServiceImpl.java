package com.petadoption.service;

import com.petadoption.dao.AuditLogDAO;
import com.petadoption.dao.AuditLogDAOImpl;
import com.petadoption.dao.UserDAO;
import com.petadoption.dao.UserDAOImpl;
import com.petadoption.exception.AuthenticationException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.AuditLog;
import com.petadoption.model.User;
import com.petadoption.util.PasswordUtil;
import com.petadoption.util.ValidationUtil;

import java.util.List;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;
    private final NotificationService notificationService;

    public UserServiceImpl() {
        this(new UserDAOImpl(), new AuditLogDAOImpl(), NotificationService.getInstance());
    }

    public UserServiceImpl(UserDAO userDAO, AuditLogDAO auditLogDAO, NotificationService notificationService) {
        this.userDAO = userDAO;
        this.auditLogDAO = auditLogDAO;
        this.notificationService = notificationService;
    }

    @Override
    public User register(String email, String password, String fullName, String phone, String address, String roleStr) {
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePasswordStrength(password);
        ValidationUtil.requireNonBlank(fullName, "Full Name");
        ValidationUtil.validatePhone(phone);

        User.Role role = User.Role.fromString(roleStr);
        if (role == null || role == User.Role.ADMIN) {
            throw new ValidationException("Public registration allows only ADOPTER or SHELTER roles.");
        }

        if (userDAO.findByEmail(email).isPresent()) {
            throw new ValidationException("An account with email " + email + " already exists.");
        }

        String passwordHash = PasswordUtil.hashPassword(password);
        User user = new User();
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordHash);
        user.setFullName(ValidationUtil.sanitize(fullName));
        user.setPhone(phone != null ? phone.trim() : null);
        user.setAddress(ValidationUtil.sanitize(address));
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);

        User created = userDAO.create(user);

        // Asynchronous non-blocking audit logging
        notificationService.submitAsyncTask(() -> {
            auditLogDAO.log(new AuditLog(
                created.getId(),
                "USER_REGISTERED",
                "USER",
                created.getId(),
                "Registered new " + role.name() + " account: " + created.getEmail(),
                "127.0.0.1"
            ));
        });

        return created;
    }

    @Override
    public User authenticate(String email, String password) {
        ValidationUtil.validateEmail(email);
        ValidationUtil.requireNonBlank(password, "Password");

        User user = userDAO.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("Invalid email or password."));

        if (!user.isActive()) {
            throw new AuthenticationException("Your account has been deactivated. Please contact support.");
        }

        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password.");
        }

        notificationService.submitAsyncTask(() -> {
            auditLogDAO.log(new AuditLog(
                user.getId(),
                "USER_LOGIN",
                "USER",
                user.getId(),
                "Successful login for " + user.getEmail(),
                "127.0.0.1"
            ));
        });

        return user;
    }

    @Override
    public User updateProfile(Long userId, String fullName, String phone, String address) {
        ValidationUtil.requireNonBlank(fullName, "Full Name");
        ValidationUtil.validatePhone(phone);

        User user = getUserById(userId);
        user.setFullName(ValidationUtil.sanitize(fullName));
        user.setPhone(phone != null ? phone.trim() : null);
        user.setAddress(ValidationUtil.sanitize(address));

        userDAO.updateProfile(user);
        return user;
    }

    @Override
    public void updateUserStatus(Long adminUserId, Long targetUserId, String statusStr) {
        User.UserStatus status = User.UserStatus.fromString(statusStr);
        if (status == null) {
            throw new ValidationException("Invalid user status: " + statusStr);
        }

        User targetUser = getUserById(targetUserId);

        // Security check: Guard against disabling the last active admin
        if (targetUser.getRole() == User.Role.ADMIN && status == User.UserStatus.INACTIVE) {
            long activeAdmins = userDAO.countActiveAdmins();
            if (activeAdmins <= 1) {
                throw new ValidationException("Cannot deactivate the only remaining active administrator account.");
            }
        }

        userDAO.updateStatus(targetUserId, status);

        notificationService.submitAsyncTask(() -> {
            auditLogDAO.log(new AuditLog(
                adminUserId,
                "USER_STATUS_UPDATED",
                "USER",
                targetUserId,
                "Admin changed status of user " + targetUser.getEmail() + " to " + status.name(),
                "127.0.0.1"
            ));
        });
    }

    @Override
    public User getUserById(Long id) {
        return userDAO.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public List<User> getUsersByRole(String roleStr) {
        User.Role role = User.Role.fromString(roleStr);
        if (role == null) {
            return getAllUsers();
        }
        return userDAO.findByRole(role);
    }
}
