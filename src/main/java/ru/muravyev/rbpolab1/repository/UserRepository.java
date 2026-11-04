package ru.muravyev.rbpolab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.muravyev.rbpolab1.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}