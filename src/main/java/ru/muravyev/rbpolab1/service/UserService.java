package ru.muravyev.rbpolab1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.muravyev.rbpolab1.entity.User;
import ru.muravyev.rbpolab1.exception.NotFoundException;
import ru.muravyev.rbpolab1.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public User create(String name, String email) {
        checkName(name);
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User update(Long id, String name, String email) {
        User user = getById(id);
        if (name != null) {
            checkName(name);
            user.setName(name);
        }
        if (email != null) {
            user.setEmail(email);
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        // опросы и участия пользователя удалит БД (ON DELETE CASCADE)
        userRepository.delete(getById(id));
    }

    private void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
    }
}