package org.interview.service;

import org.interview.client.YandexClient;
import org.interview.repository.InterviewRepository;
import org.interview.repository.TopicRepository;
import org.interview.telegram.Bot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.format.DateTimeFormatter;

@Component
public class SubscribeCommand extends Command {

    private final PaymentService paymentService;

    public SubscribeCommand(PaymentService paymentService,
                            TopicRepository topicRepository,
                            YandexClient openAiClient,
                            InterviewRepository interviewRepository) {
        super(topicRepository, openAiClient, interviewRepository);
        this.paymentService = paymentService;
    }

    @Override
    public boolean isApplicable(Update update) {
        return update.hasMessage() && "/subscribe".equalsIgnoreCase(update.getMessage().getText());
    }

    @Override
    public String process(Update update, Bot bot) {
        String chatId = update.getMessage().getChatId().toString();

        bot.saveChatIdIfNeeded(update, chatId);

        var subscription = bot.getSubscriptionService().getSubscriptionByChatId(chatId);

        if (subscription != null && subscription.getExpiresAt() != null && subscription.getExpiresAt().isAfter(java.time.LocalDateTime.now())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String formattedDate = subscription.getExpiresAt().toLocalDate().format(formatter);

            return String.format("""
                ✅ У тебя уже активная подписка!
                
                🔐 Дата окончания: %s
                
                👉 Можешь сразу продолжить интервью — просто напиши /start
                """, formattedDate);
        }

        return "⛔ Подписка не найдена или уже закончилась. Нажми /start, чтобы активировать доступ.";
    }
}



