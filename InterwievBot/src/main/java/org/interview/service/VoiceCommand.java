package org.interview.service;

import org.interview.client.YandexClient;
import org.interview.dto.Question;
import org.interview.repository.InterviewRepository;
import org.interview.repository.TopicRepository;
import org.interview.telegram.Bot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;

@Component
public class VoiceCommand extends Command {

    @Value("${interview.max-questions}")
    private int maxQuestions;

    public VoiceCommand(YandexClient yandexClient,
                        InterviewRepository interviewRepository,
                        TopicRepository topicRepository) {
        super(topicRepository, yandexClient, interviewRepository);
    }

    @Override
    public boolean isApplicable(Update update) {
        return update.getMessage().hasVoice();
    }

    @Override
    public String process(Update update, Bot bot) {
        String chatId = update.getMessage().getChatId().toString();
        String answer = transcribeVoiceAnswer(update, bot);

        // ✨ Проверка: если ошибка про слишком длинное сообщение, сразу вернуть текст пользователю
        if (answer.startsWith("⚠️")) {
            return answer;
        }

        // ✨ Проверка: есть ли активная сессия
        if (!interviewRepository.hasActiveSession(chatId)) {
            return "❗ У вас нет активного интервью. Нажмите /start, чтобы начать.";
        }

        interviewRepository.addAnswer(chatId, answer);

        if (interviewRepository.getUserQuestions(chatId) == maxQuestions) {
            return provideFeedback(chatId);
        } else {
            return askNextQuestion(chatId);
        }
    }

    private String transcribeVoiceAnswer(Update update, Bot bot) {
        Voice voice = update.getMessage().getVoice();
        String fileId = voice.getFileId();
        java.io.File downloadedFile = null;
        java.io.File oggOpusFile = null;

        try {
            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(fileId);
            File file = bot.execute(getFileRequest);

            String filePath = file.getFilePath();
            System.out.println("📂 Telegram путь к файлу: " + filePath);
            System.out.println("📦 MIME type: " + voice.getMimeType());

            downloadedFile = bot.downloadFile(filePath);
            System.out.println("🎧 Скачан файл: " + downloadedFile.getAbsolutePath() +
                    ", размер: " + downloadedFile.length() + " байт");

            if (downloadedFile.length() > 1024 * 1024) {
                return "⚠️ Голосовое сообщение превышает лимит в 30 сек или 1 МБ. Пожалуйста, сократи его.";
            }

            // конвертация в oggopus
            oggOpusFile = yandexClient.convertToOggOpus(downloadedFile);

            // 🧹 Теперь чистим файлы после распознавания
            String result = yandexClient.transcribe(oggOpusFile);
            return result;

        } catch (Exception e) {
            throw new IllegalStateException("Ошибка при обработке голосового сообщения", e);
        } finally {
            if (downloadedFile != null && downloadedFile.exists()) {
                boolean deleted = downloadedFile.delete();
                if (!deleted) {
                    downloadedFile.deleteOnExit();
                    System.out.println("⚠️ Не удалось удалить оригинальный файл сразу, запланировано удаление при завершении приложения");
                } else {
                    System.out.println("🧹 Оригинальный файл удалён");
                }
            }
            if (oggOpusFile != null && oggOpusFile.exists()) {
                boolean deleted = oggOpusFile.delete();
                if (!deleted) {
                    oggOpusFile.deleteOnExit();
                    System.out.println("⚠️ Не удалось удалить конвертированный файл сразу, запланировано удаление при завершении приложения");
                } else {
                    System.out.println("🧹 Конвертированный файл удалён");
                }
            }
        }
    }

    private String askNextQuestion(String userName) {
        String nextQuestion = topicRepository.getNextQuestion(userName);

        if (nextQuestion == null) {
            return "Кажется, больше вопросов нет. 🎉 Подожди чуть-чуть — сейчас будет обратная связь!";
        }

        int questionNumber = interviewRepository.getNextQuestionNumber(userName);
        interviewRepository.addQuestion(userName, nextQuestion);

        return String.format("Вопрос №%d:\n%s", questionNumber, nextQuestion);
    }

    private String provideFeedback(String userName) {
        StringBuilder feedbackPrompt = new StringBuilder(Prompts.FEEDBACK_PROMPT);
        Deque<Question> questions = interviewRepository.finishInterview(userName);
        questions.forEach(question -> feedbackPrompt.append("Вопрос: ")
                .append(question.getQuestion()).append("\nОтвет: ")
                .append(question.getAnswer()).append("\n\n"));
        return yandexClient.promptModel(feedbackPrompt.toString());
    }
}