package org.interview.service;

import org.interview.entity.PendingSubscription;
import org.interview.repository.PendingSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.interview.entity.Subscription;
import org.interview.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionService {

    @Value("${subscription.ttl-days}")
    private int subscriptionTtlDays;

    private final SubscriptionRepository repository;

    @Autowired
    private PendingSubscriptionRepository pendingRepository;


    public SubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    /**
     * Обновляет или создаёт подписку: сохраняет chatId (если передан) и продлевает подписку.
     */
    public void saveOrUpdateSubscription(String userName, String chatId) {
        Subscription sub = repository.findByChatId(chatId).orElseGet(() -> {
            Subscription s = new Subscription();
            s.setChatId(chatId);
            return s;
        });

        if (userName != null && (sub.getUserName() == null || !sub.getUserName().equals(userName))) {
            sub.setUserName(userName);
        }

        sub.setExpiresAt(LocalDateTime.now().plusDays(subscriptionTtlDays));
        repository.save(sub);
    }

    /**
     * Проверяет, активна ли подписка пользователя.
     */
    public boolean hasActiveSubscription(String userId) {
        return repository.existsByChatIdAndExpiresAtAfter(userId, LocalDateTime.now());
    }

    /**
     * Получает chatId по userId, если он сохранён.
     */
    public String getChatIdByUserName(String userName) {
        return pendingRepository.findById(userName)
                .map(PendingSubscription::getChatId)
                .orElse(null);
    }

    /**
     * Удаляет все просроченные подписки.
     */
    public void deleteExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expired = repository.findAllByExpiresAtBefore(now);
        if (!expired.isEmpty()) {
            expired.forEach(sub -> {
                System.out.println("❌ Удаляем подписку userId=" + sub.getUserName());
                sub.setExpiresAt(null);
                repository.save(sub);
            });
        }
    }



    public boolean extendIfExists(String userId, String chatId) {
        return repository.findByChatId(userId).map(sub -> {
            // 💡 Обновляем chatId, если он отсутствует или не совпадает
            if (chatId != null && (sub.getChatId() == null || !sub.getChatId().equals(chatId))) {
                sub.setChatId(chatId);
            }

            sub.setExpiresAt(LocalDateTime.now().plusDays(subscriptionTtlDays));
            repository.save(sub);
            return true;
        }).orElse(false);
    }

    public Subscription getSubscriptionByChatId(String chatId) {
        return repository.findByChatId(chatId).orElse(null);
    }

    public boolean hasActiveSubscriptionByChatId(String chatId) {
        return repository.existsByChatIdAndExpiresAtAfter(chatId, LocalDateTime.now());
    }

    public Subscription getSubscription(String userId) {
        return repository.findByChatId(userId).orElse(null);
    }


}
