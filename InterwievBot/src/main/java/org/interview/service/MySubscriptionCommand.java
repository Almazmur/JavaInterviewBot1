package org.interview.service;

import org.interview.repository.SubscriptionRepository;
import org.interview.telegram.Bot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.format.DateTimeFormatter;

@Component
public class MySubscriptionCommand extends Command {

    private final SubscriptionRepository repository;

    public MySubscriptionCommand(SubscriptionRepository repository) {
        super(null, null, null);
        this.repository = repository;
    }

    @Override
    public boolean isApplicable(Update update) {
        return update.hasMessage() && "/my_subscription".equalsIgnoreCase(update.getMessage().getText());
    }

    @Override
    public String process(Update update, Bot bot) {
        String userId = update.getMessage().getFrom().getUserName();

        return repository.findByChatId(userId)
                .map(sub -> "🔔 Твоя подписка активна до: " +
                        sub.getExpiresAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .orElse("⛔ У тебя нет активной подписки.");
    }
}
