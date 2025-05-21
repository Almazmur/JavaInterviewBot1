package org.interview.service;

import org.interview.client.YandexClient;
import org.interview.dto.Question;
import org.interview.repository.InterviewRepository;
import org.interview.repository.TopicRepository;
import org.interview.telegram.Bot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Deque;

@Component
public class FeedbackNowCommand extends Command {

    public FeedbackNowCommand(TopicRepository topicRepository,
                              YandexClient yandexClient,
                              InterviewRepository interviewRepository) {
        super(topicRepository, yandexClient, interviewRepository);
    }

    @Override
    public boolean isApplicable(Update update) {
        return update.hasMessage() && "/feedback_now".equalsIgnoreCase(update.getMessage().getText());
    }

    @Override
    public String process(Update update, Bot bot) {
        String chatId = update.getMessage().getChatId().toString();

        bot.saveChatIdIfNeeded(update, chatId);

        if (!bot.getSubscriptionService().hasActiveSubscriptionByChatId(chatId)) {
            return "⛔ Подписка истекла. Нажми /start, чтобы активировать доступ.";
        }

        if (interviewRepository.getUserQuestions(chatId) == 0) {
            return "Ты ещё не ответил ни на один вопрос 😅 Начни с команды /start";
        }

        return provideFeedback(chatId);
    }

    private String provideFeedback(String chatId) {
        StringBuilder feedbackPrompt = new StringBuilder(Prompts.FEEDBACK_PROMPT);

        Deque<Question> questions = interviewRepository.finishInterview(chatId);
        questions.forEach(question -> feedbackPrompt.append("Исходный вопрос: ")
                .append(question.getQuestion()).append("\n")
                .append("Ответ кандидата: ")
                .append(question.getAnswer()).append("\n"));

        return yandexClient.promptModel(feedbackPrompt.toString());
    }
}








//    private String provideFeedback(String userId) {
//        StringBuilder feedbackPrompt = new StringBuilder();
//        feedbackPrompt.append(VoiceCommand.FEEDBACK_PROMPT);
//        Deque<Question> questions = interviewRepository.finishInterview(userId);
//        questions.forEach(question -> feedbackPrompt.append("Исходный вопрос: ")
//                .append(question.getQuestion()).append("\n")
//                .append("Ответ кандидата: ")
//                .append(question.getAnswer()).append("\n"));
//        return openAiClient.promptModel(feedbackPrompt.toString());
//    }

