package org.interview.service;

import org.interview.entity.Subscription;
import org.interview.repository.PendingSubscriptionRepository;
import org.interview.repository.SubscriptionRepository;
import org.interview.telegram.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SubscriptionNotifier {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionNotifier.class);

    private final SubscriptionRepository subscriptionRepository;
    private final PendingSubscriptionRepository pendingRepository;
    private final Bot bot;
    private final SubscriptionService subscriptionService;

    public SubscriptionNotifier(
            SubscriptionRepository subscriptionRepository,
            PendingSubscriptionRepository pendingRepository,
            Bot bot,
            SubscriptionService subscriptionService) {
        this.subscriptionRepository = subscriptionRepository;
        this.pendingRepository = pendingRepository;
        this.bot = bot;
        this.subscriptionService = subscriptionService;
    }

    // 🔔 Напоминаем за день до окончания подписки
    @Scheduled(cron = "0 0 10 * * *")
    public void notifyExpiringSubscriptions() {
        LocalDateTime targetDate = LocalDateTime.now().plusDays(1);
        List<Subscription> expiring = subscriptionRepository.findAllByExpiresAtBetween(
                targetDate.withHour(0).withMinute(0).withSecond(0),
                targetDate.withHour(23).withMinute(59).withSecond(59)
        );

        for (Subscription sub : expiring) {
            if (sub.getChatId() == null) continue;

            log.info("🔔 Напоминание для пользователя {}: осталось 1 день до окончания", sub.getUserName());

            SendMessage message = new SendMessage();
            message.setChatId(sub.getChatId());
            message.setText("⏳ Привет! Остался 1 день до окончания твоей подписки на интервью-бота. " +
                    "Если хочешь продолжить получать вопросы — продли подписку! 💡");

            try {
                bot.execute(message);
            } catch (TelegramApiException e) {
                log.error("Не удалось отправить уведомление пользователю " + sub.getUserName(), e);
            }
        }
    }

    // 🧹 Очищаем просроченные записи
    @Scheduled(fixedDelay = 86_400_000) // раз в сутки
    public void cleanupExpired() {
        subscriptionService.deleteExpiredSubscriptions();

        LocalDateTime cutoff = LocalDateTime.now().minusYears(5);
        int count = pendingRepository.findAll().stream()
                .filter(p -> p.getCreatedAt().isBefore(cutoff))
                .map(p -> {
                    pendingRepository.delete(p);
                    return 1;
                }).reduce(0, Integer::sum);

        if (count > 0) {
            log.info("🧹 Очищено {} просроченных Pending-подписок", count);
        }
    }
}