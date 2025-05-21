package org.interview.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InterviewSessionCleaner {

    private final InterviewSessionService sessionService;

    public InterviewSessionCleaner(InterviewSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Scheduled(fixedDelay = 86_400_000) // раз в сутки
    public void cleanExpiredSessions() {
        sessionService.cleanExpiredSessions();
    }
}
