package org.interview.service;

import org.interview.client.YandexClient;
import org.interview.repository.InterviewRepository;
import org.interview.repository.TopicRepository;
import org.interview.telegram.Bot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
public class SkipCommand extends Command {

    public SkipCommand(TopicRepository topicRepository,
                       YandexClient openAiClient,
                       InterviewRepository interviewRepository) {
        super(topicRepository, openAiClient, interviewRepository);
    }

    @Override
    public boolean isApplicable(Update update) {
        return update.hasMessage() && "/skip".equalsIgnoreCase(update.getMessage().getText());
    }

    @Override
    public String process(Update update, Bot bot) {
        String chatId = update.getMessage().getChatId().toString();

        bot.saveChatIdIfNeeded(update, chatId);

        if (!bot.getSubscriptionService().hasActiveSubscriptionByChatId(chatId)) {
            return "⛔ Подписка истекла. Нажми /start, чтобы активировать доступ.";
        }

        String nextQuestion = topicRepository.getNextQuestion(chatId);

        if (nextQuestion == null) {
            return "Кажется, больше вопросов нет. 🎉 Подожди чуть-чуть — сейчас будет обратная связь!";
        }

        int questionNumber = interviewRepository.getNextQuestionNumber(chatId);
        interviewRepository.addQuestion(chatId, nextQuestion);

        return String.format("Вопрос №%d (предыдущий пропущен):\n%s", questionNumber, nextQuestion);
    }
}





//package org.interview.service;
//
//import org.interview.client.OpenAiClient;
//import org.interview.repository.InterviewRepository;
//import org.interview.repository.TopicRepository;
//import org.interview.telegram.Bot;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.meta.api.objects.Update;
//
//@Component
//public class SkipCommand extends Command {
//
//    public SkipCommand(TopicRepository topicRepository,
//                       OpenAiClient openAiClient,
//                       InterviewRepository interviewRepository) {
//        super(topicRepository, openAiClient, interviewRepository);
//    }
//
//    @Override
//    public boolean isApplicable(Update update) {
//        return update.hasMessage() && "/skip".equalsIgnoreCase(update.getMessage().getText());
//    }
//
//    @Override
//    public String process(Update update, Bot bot) {
//        String userName = update.getMessage().getFrom().getUserName();
//        if (!bot.getSubscriptionService().hasActiveSubscription(userName)) {
//            return "⛔ Подписка истекла. Пожалуйста, продли её, чтобы продолжить.";
//        }
//        String nextTopic = topicRepository.getNextQuestion(userName);
//
//        if (nextTopic == null) {
//            return "Кажется, больше вопросов нет. 🎉 Подожди чуть-чуть — сейчас будет обратная связь!";
//        }
//
//        int questionNumber = interviewRepository.getNextQuestionNumber(userName);
//        String prompt = String.format(VoiceCommand.QUESTION_PROMPT, nextTopic);
//        String question = openAiClient.promptModel(prompt);
//        interviewRepository.addQuestion(userName, question);
//        return String.format("Вопрос №%d (предыдущий пропущен):\n%s", questionNumber, question);
//    }
//}
