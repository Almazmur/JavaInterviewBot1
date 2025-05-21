package org.interview.repository;

import org.interview.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {


    List<Subscription> findAllByExpiresAtBetween(LocalDateTime start, LocalDateTime end);


    List<Subscription> findAllByExpiresAtBefore(LocalDateTime time);


    boolean existsByChatIdAndExpiresAtAfter(String chatId, LocalDateTime now);

    Optional<Subscription> findByChatId(String chatId);

    Optional<Subscription> findByUserName(String userName);
}
