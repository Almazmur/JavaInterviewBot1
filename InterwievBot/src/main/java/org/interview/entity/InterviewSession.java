package org.interview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "interview_sessions")
public class InterviewSession {

    @Id
    private String userId;

    @ElementCollection
    @CollectionTable(name = "interview_session_questions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "question")
    private List<String> questions;

    private int currentIndex;

    private LocalDateTime expiresAt;

}
