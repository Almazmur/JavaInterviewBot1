package org.interview.service;
import org.interview.client.YandexClient;
import org.interview.repository.InterviewRepository;
import org.interview.repository.TopicRepository;
import org.interview.telegram.Bot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ResetCommand extends Command {

    public ResetCommand(TopicRepository topicRepository,
                        YandexClient openAiClient,
                        InterviewRepository interviewRepository) {
        super(topicRepository, openAiClient, interviewRepository);
    }

    @Override
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();
        return message.hasText() && "/reset".equalsIgnoreCase(message.getText());
    }

    @Override
    public String process(Update update, Bot bot) {
        String chatId = update.getMessage().getChatId().toString();

        bot.saveChatIdIfNeeded(update, chatId);

        if (!bot.getSubscriptionService().hasActiveSubscriptionByChatId(chatId)) {
            return "⛔ Подписка истекла. Нажми /start, чтобы активировать доступ.";
        }

        topicRepository.clearUserSession(chatId);
        interviewRepository.finishInterview(chatId);
        return "Готово! Всё очищено. Можешь ввести /start, чтобы начать собеседование заново 🎉";
    }
}




