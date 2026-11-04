package ru.muravyev.rbpolab1.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.muravyev.rbpolab1.entity.Survey;

import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    // SELECT ... FOR UPDATE: строка опроса блокируется до конца транзакции
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Survey s where s.id = :id")
    Optional<Survey> findByIdForUpdate(@Param("id") Long id);
}