package com.petadoption;

import com.petadoption.config.DatabaseConfig;
import com.petadoption.dao.AuditLogDAOImpl;
import com.petadoption.dao.UserDAO;
import com.petadoption.dao.UserDAOImpl;
import com.petadoption.exception.AuthenticationException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.User;
import com.petadoption.service.NotificationService;
import com.petadoption.service.UserService;
import com.petadoption.service.UserServiceImpl;
import com.petadoption.util.PasswordUtil;
import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoAndServiceTest {

    private static UserService userService;
    private static UserDAO userDAO;

    @BeforeAll
    public static void setUp() {
        System.setProperty("app.environment", "h2");
        DatabaseConfig.initialize();
        userDAO = new UserDAOImpl();
        userService = new UserServiceImpl(userDAO, new AuditLogDAOImpl(), NotificationService.getInstance());
    }

    @Test
    @Order(1)
    @DisplayName("AUTH-01: Successful Adopter registration with BCrypt password hashing")
    public void testUserRegistration() {
        String email = "test.adopter." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        User user = userService.register(
            email,
            "Password@123",
            "Test Adopter User",
            "+1-555-9988",
            "123 Testing Lane",
            "ADOPTER"
        );

        assertNotNull(user.getId(), "User ID should be generated");
        assertEquals(email.toLowerCase(), user.getEmail());
        assertEquals(User.Role.ADOPTER, user.getRole());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());

        // Verify password hash in DAO
        Optional<User> fetched = userDAO.findByEmail(email);
        assertTrue(fetched.isPresent());
        assertTrue(PasswordUtil.checkPassword("Password@123", fetched.get().getPasswordHash()));
        assertFalse(PasswordUtil.checkPassword("WrongPassword", fetched.get().getPasswordHash()));
    }

    @Test
    @Order(2)
    @DisplayName("AUTH-02: Prevent self-registration as ADMIN")
    public void testRejectAdminRegistration() {
        String email = "hacker.admin@example.com";
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userService.register(
                email,
                "Password@123",
                "Fake Admin",
                "+1-555-0000",
                "Nowhere",
                "ADMIN"
            );
        });
        assertTrue(ex.getMessage().contains("Public registration allows only ADOPTER or SHELTER"));
    }

    @Test
    @Order(3)
    @DisplayName("AUTH-03: Prevent duplicate email registration")
    public void testRejectDuplicateEmail() {
        String email = "duplicate.test@example.com";
        userService.register(email, "Password@123", "User One", null, null, "ADOPTER");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userService.register(email, "Password@123", "User Two", null, null, "ADOPTER");
        });
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @Order(4)
    @DisplayName("AUTH-04: Successful authentication with valid credentials")
    public void testSuccessfulAuthentication() {
        String email = "auth.success." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        userService.register(email, "Password@123", "Auth Tester", null, null, "SHELTER");

        User authenticated = userService.authenticate(email, "Password@123");
        assertNotNull(authenticated);
        assertEquals(email.toLowerCase(), authenticated.getEmail());
    }

    @Test
    @Order(5)
    @DisplayName("AUTH-05: Reject invalid password authentication")
    public void testFailedAuthentication() {
        String email = "auth.fail." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        userService.register(email, "Password@123", "Fail Tester", null, null, "ADOPTER");

        AuthenticationException ex = assertThrows(AuthenticationException.class, () -> {
            userService.authenticate(email, "IncorrectPassword");
        });
        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }

    @Test
    @Order(6)
    @DisplayName("ADM-01: Prevent deactivating the last active administrator")
    public void testPreventDeactivatingLastAdmin() {
        // Admin ID 1 is seeded as the sole default admin
        User adminUser = userDAO.findById(1L).orElseThrow();
        assertEquals(User.Role.ADMIN, adminUser.getRole());

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userService.updateUserStatus(1L, 1L, "INACTIVE");
        });
        assertTrue(ex.getMessage().contains("Cannot deactivate the only remaining active administrator"));
    }
}
