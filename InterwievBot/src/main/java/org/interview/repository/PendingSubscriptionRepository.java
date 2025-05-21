package org.interview.repository;

import org.interview.entity.PendingSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingSubscriptionRepository extends JpaRepository<PendingSubscription, String> {
}
