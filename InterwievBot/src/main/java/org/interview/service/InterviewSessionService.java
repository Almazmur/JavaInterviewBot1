package org.interview.service;

import org.interview.entity.InterviewSession;
import org.interview.repository.InterviewSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewSessionService {

    private final InterviewSessionRepository repository;

    public InterviewSessionService(InterviewSessionRepository repository) {
        this.repository = repository;
    }

    public void startSession(String userId, List<String> questions, int ttlMinutes) {
        InterviewSession session = new InterviewSession();
        session.setUserId(userId);
        session.setQuestions(questions);
        session.setCurrentIndex(1); // сразу ставим на 1
        session.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        repository.save(session);
    }

    public String getNextQuestion(String userId) {
        InterviewSession session = repository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Сессия не найдена"));

        if (session.getCurrentIndex() >= session.getQuestions().size()) return null;

        String question = session.getQuestions().get(session.getCurrentIndex());
        session.setCurrentIndex(session.getCurrentIndex() + 1);
        repository.save(session);
        return question;
    }

    public void cleanExpiredSessions() {
        repository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }
}
