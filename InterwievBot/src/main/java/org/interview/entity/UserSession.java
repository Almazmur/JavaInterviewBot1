
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
@Table(name = "user_session")
public class UserSession {

    @Id
    private String userId;

    private Integer currentIndex = 0;
    private LocalDateTime expiresAt;
}