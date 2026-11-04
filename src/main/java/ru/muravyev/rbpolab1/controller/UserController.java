package ru.muravyev.rbpolab1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.muravyev.rbpolab1.dto.UserRequest;
import ru.muravyev.rbpolab1.dto.UserResponse;
import ru.muravyev.rbpolab1.entity.User;
import ru.muravyev.rbpolab1.service.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return toResponse(userService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest r) {
        User created = userService.create(r.username(), r.password(), r.name(), r.email(), r.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @RequestBody UserRequest r, Principal principal) {
        return toResponse(userService.update(id, r.name(), r.email(), r.role(), r.status(),
                principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        userService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getName(), u.getEmail(),
                u.getRole(), u.getStatus(), u.getCreatedAt());
    }
}