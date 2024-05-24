package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class NasaTelegramBot extends TelegramLongPollingBot {

    private final String BOT_NAME;
    private final String BOT_TOKEN;
    private final String URL_NASA = "https://api.nasa.gov/planetary/apod?api_key" +
            "=Mhtp52XtdZKj1plEwgCpeiFQVF0j7hVb9oxt6Cx9";

    public NasaTelegramBot(String BOT_NAME, String BOT_TOKEN) throws TelegramApiException {
        this.BOT_NAME = BOT_NAME;
        this.BOT_TOKEN = BOT_TOKEN;

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String answer = update.getMessage().getText();
            String[] separatedAnswer = answer.split(" ");
            String action = separatedAnswer[0];

            switch (action) {
                case "/start":
                case "/начало":
                    sendMessage("Я бот, присылаю картинку дня введите /image или /фото для получения сегоднешнего изображения", chatId);
                    break;
                case "/help":
                case "/помощь":
                    sendMessage("Напишите /image(/фото) для получения сегоднешнего изображения. Напишите \"/date(/дата) YYYY-MM-dd\" для получения фото конкретного дня", chatId);
                    break;
                case "/image":
                case "/фото":
                    String image = Utils.getUrl(URL_NASA);
                    sendMessage(image, chatId);
                    break;
                case "/date":
                case "/дата":
                    String date = separatedAnswer[1];
                    String dateImage = Utils.getUrl(URL_NASA + "&date=" + date);
                    sendMessage(dateImage, chatId);
                    break;
                default:
                    sendMessage("Что-то на эльфийском", chatId);
            }


        }
    }

    void sendMessage(String text, Long chatId) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
