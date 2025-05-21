package org.interview.repository;

import org.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, String> {
    List<InterviewSession> findAllByExpiresAtBefore(LocalDateTime now);
    void deleteAllByExpiresAtBefore(LocalDateTime now);
}
