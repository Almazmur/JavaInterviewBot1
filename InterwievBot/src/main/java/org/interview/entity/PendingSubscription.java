package org.interview.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "pending_subscriptions")
public class PendingSubscription {
    @Id
    private String userName;
    private String chatId;
    private LocalDateTime createdAt = LocalDateTime.now();
}
