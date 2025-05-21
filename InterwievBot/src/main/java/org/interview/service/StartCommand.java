package org.interview.service;

import org.interview.repository.InterviewRepository;
import org.interview.repository.TopicRepository;
import org.interview.telegram.Bot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class StartCommand extends Command {

    @Value("${interview.max-questions}")
    private int maxQuestions;

    public StartCommand(TopicRepository topicRepository,
                        InterviewRepository interviewRepository) {
        super(topicRepository, null, interviewRepository);
    }

    @Override
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();
        return message.hasText() && "/start".equalsIgnoreCase(message.getText());
    }

    @Override
    public String process(Update update, Bot bot) {
        String chatId = update.getMessage().getChatId().toString();

        if (!bot.getSubscriptionService().hasActiveSubscriptionByChatId(chatId)) {
            return "⛔ Нет подписки. Пожалуйста, продли её, чтобы продолжить.";
        }

        interviewRepository.startInterview(chatId);

        String firstQuestion = topicRepository.startUserSession(chatId, maxQuestions);

        if (firstQuestion == null) {
            return "😅 Не удалось найти вопросы для интервью. Обратись к администратору.";
        }

        interviewRepository.addQuestion(chatId, firstQuestion);

        return "🚀 Начинаем интервью!\nОтвечай только голосом 🎤\n\nВопрос №1:\n" + firstQuestion;
    }

    private String askNextQuestion(String userName) {
        String nextTopic = topicRepository.getNextQuestion(userName);

        if (nextTopic == null) {
            return "Кажется, больше вопросов нет. 🎉 Подожди чуть-чуть — сейчас будет обратная связь!";
        }

        int questionNumber = interviewRepository.getNextQuestionNumber(userName);

        String prompt = String.format(Prompts.QUESTION_PROMPT, nextTopic);
        String question = yandexClient.promptModel(prompt);

        interviewRepository.addQuestion(userName, question);

        return String.format("Вопрос №%d:\n%s", questionNumber, question);
    }
}



//package org.interview.service;
//
//import org.interview.client.OpenAiClient;
//import org.interview.repository.InterviewRepository;
//import org.interview.repository.TopicRepository;
//import org.interview.telegram.Bot;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.meta.api.objects.Message;
//import org.telegram.telegrambots.meta.api.objects.Update;
//
//
//@Component
//public class StartCommand extends Command {
//
//    @Value("${interview.max-questions}")
//    private int maxQuestions;
//
//    private static final String INTERVIEW_PROMPT = """
//            Весело и тепло поприветствуй кандидата на собеседовании, а также расскажи ему о правилах интервью
//                    "1. Будет %d вопросов по Java на уровень Java Junior+
//                    "2. Отвечать на вопросы кандидат должен только голосовыми сообщениями и так подробно, как считает нужным
//                    "3. По итогам его %d ответов будет предоставлена обратная связь от собеседующего с обозначением
//                    "сильных сторон, а также мест, где ответы были не очень точны с указанием, на какие аспекты подготовки
//                    "кандидату нужно обратить внимание
//                     Вот исходный вопрос для собеседования на Java Junior в Java: %s
//
//
//            Сам следуй правилам выше при формировании вопросов. Учитывай, что кандидат пока может не знать
//            про веб-приложения и сложную разработку. Но он знает про популярные приложения, которые есть
//            в мире, такие как YouTube, Vkontakte, Yandex, Sber, Netflix, T-bank, Telegram и другие. Но не используй Meta, Facebook или
//            Instagram в качестве своих примеров. Прочие же приложения могут участвовать в формировании
//            контекста для твоих вопросов, чтобы сделать собеседование более интересным для кандидата и еще добавь немножко
//            смайлики, при формировании вопроса, чтоб не выглядело слишком сухо.
//
//            Вот исходный вопрос для собеседования на Java Junior+ в Java: %s
//            Задай его, придумав интересную ситуацию c каким-нибудь реальным очень известным
//            приложением, в контексте которого и будет задаваться вопрос. Чтобы это не выглядело сухо, как
//            просто вопрос по Java. Подумай, как можно сделать его более интересным и понятным для кандидата
//            с помощью дополнительного контекста реального приложения.
//
//            Пример: Представим, что мы работаем в Google над приложением YouTube. И нам поручают задачу:
//            создать класс видео, в котором будет 3 поля: название, количество лайков и количество просмотров.
//            Добавляя эти поля, нам важно соблюсти принцип инкапсуляции в ООП. Что это такое, и как он
//            реализуется в Java?
//
//            Используй и другие примеры известных приложений, не только YouTube. Но иногда можно и YouTube.
//
//            Стиль общения:
//            Общайся с кандидатом на "ты". Это должна быть беседа двух хороших друзей, тепло и непринужденно,
//            без лишних формальностей.
//            Избегай слишком многословных формулировок. Старайся формулировать предложения четко и по делу,
//            но в то же время сохраняй ламповость и живость беседы. Нужно найти баланс. Тем не менее,
//            человек не должен получить от тебя огромный текст, который ему будет лень читать. Разговор
//            должен быть интересным, но лаконичным, чтобы человек не потерял желание его продолжать из-за
//            огромного количества текста на экране.
//            """;
//
//    public StartCommand(TopicRepository topicRepository,
//                        OpenAiClient openAiClient,
//                        InterviewRepository interviewRepository) {
//        super(topicRepository, openAiClient, interviewRepository);
//    }
//
//    public boolean isApplicable(Update update) {
//        Message message = update.getMessage();
//        return message.hasText() && "/start".equalsIgnoreCase(message.getText());
//    }
//
//    @Override
//    public String process(Update update, Bot bot) {
//        String userId = update.getMessage().getFrom().getUserName();
//        if (userId == null || userId.isBlank()) {
//            return "Ошибка: у тебя нет username в Telegram. Укажи его в настройках профиля!";
//        }
//
//        if (!bot.getSubscriptionService().hasActiveSubscription(userId)) {
//            return "🚫 У тебя нет активной подписки. Пожалуйста, оплати по ссылке, чтобы начать 👉 /pay";
//        }
//
//        interviewRepository.startInterview(userId);
//        String firstTopic = topicRepository.startUserSession(userId, maxQuestions);
//        String prompt = String.format(INTERVIEW_PROMPT, maxQuestions, maxQuestions, firstTopic);
//        String firstQuestion = openAiClient.promptModel(prompt);
//        interviewRepository.addQuestion(userId, firstQuestion);
//        return firstQuestion;
//    }
//}
