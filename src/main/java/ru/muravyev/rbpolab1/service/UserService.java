package ru.muravyev.rbpolab1.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.muravyev.rbpolab1.entity.User;
import ru.muravyev.rbpolab1.entity.UserRole;
import ru.muravyev.rbpolab1.entity.UserStatus;
import ru.muravyev.rbpolab1.exception.NotFoundException;
import ru.muravyev.rbpolab1.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {

    private static final String USERNAME_PATTERN = "^[A-Za-z0-9_.-]{3,50}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User register(String username, String password, String name, String email) {
        return createAccount(username, password, name, email, UserRole.USER);
    }

    public User create(String username, String password, String name, String email, UserRole role) {
        return createAccount(username, password, name, email, role != null ? role : UserRole.USER);
    }

    public User update(Long id, String name, String email, UserRole role, UserStatus status,
                       String currentUsername) {
        User user = getById(id);
        boolean self = user.getUsername().equals(currentUsername);
        boolean changesRole = role != null && role != user.getRole();
        boolean changesStatus = status != null && status != user.getStatus();
        if (self && (changesRole || changesStatus)) {
            throw new IllegalStateException("Administrators cannot change their own role or status");
        }
        if (name != null) {
            checkName(name);
            user.setName(name);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (role != null) {
            user.setRole(role);
        }
        if (status != null) {
            user.setStatus(status);
        }
        return userRepository.save(user);
    }

    public void delete(Long id, String currentUsername) {
        User user = getById(id);
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalStateException("Administrators cannot delete themselves");
        }
        userRepository.delete(user);
    }

    private User createAccount(String username, String password, String name, String email, UserRole role) {
        checkUsername(username);
        checkPassword(password, username);
        checkName(name);
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username is already taken");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email is already in use");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password)); // в БД попадает только хэш
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setName(name);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private void checkUsername(String username) {
        if (username == null || !username.matches(USERNAME_PATTERN)) {
            throw new IllegalArgumentException(
                    "Username must be 3-50 characters: latin letters, digits, '_', '.', '-'");
        }
    }

    private void checkPassword(String password, String username) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must not be longer than 72 bytes");
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new IllegalArgumentException(
                    "Password must contain an uppercase letter, a lowercase letter and a digit");
        }
        if (password.equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("Password must not be equal to the username");
        }
    }

    private void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
    }
}