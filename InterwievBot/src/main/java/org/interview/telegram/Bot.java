package org.interview.telegram;

import org.interview.entity.PendingSubscription;
import org.interview.entity.Subscription;
import org.interview.repository.PendingSubscriptionRepository;
import org.interview.repository.SubscriptionRepository;
import org.interview.service.Command;
import org.interview.service.PaymentService;
import org.interview.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot {

    private final PaymentService paymentService;
    private final List<Command> commands;
    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;
    private final PendingSubscriptionRepository pendingSubscriptionRepository;

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    public Bot(@Value("${bot.token}") String token,
               List<Command> commands,
               SubscriptionService subscriptionService,
               PaymentService paymentService,
               SubscriptionRepository subscriptionRepository, PendingSubscriptionRepository pendingSubscriptionRepository) {
        super(token);
        this.commands = commands;
        this.subscriptionService = subscriptionService;
        this.paymentService = paymentService;
        this.subscriptionRepository = subscriptionRepository;
        this.pendingSubscriptionRepository = pendingSubscriptionRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String chatId = update.getMessage().getChatId().toString();
            saveChatIdIfNeeded(update, chatId);
        }
        onUpdateReceived(update, false);
    }

    public void onUpdateReceived(Update update, boolean fromWebhook) {
        if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        }

        if (!update.hasMessage()) return;

        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        saveChatIdIfNeeded(update, chatId); // обновлено

        if (!subscriptionService.hasActiveSubscriptionByChatId(chatId)) {
            sendStartButtons(chatId);
            return;
        }

        if (message.hasText()) {
            String text = message.getText();

            if (text.startsWith("/start paid_") && !fromWebhook) {
                subscriptionService.saveOrUpdateSubscription(null, chatId);
                sendMessage(chatId, "Спасибо за оплату! 🎉 Теперь тебе доступно интервью!");
                return;
            }

            if ("/start".equalsIgnoreCase(text)) {
                handleInterviewCommand(update, chatId, fromWebhook);
                return;
            }
        }

        handleInterviewCommand(update, chatId, fromWebhook);
    }

    public void saveChatIdIfNeeded(Update update, String chatId) {
        if (chatId == null) return;

        Subscription sub = subscriptionRepository.findByChatId(chatId).orElse(null);

        String userName = null;
        if (update != null && update.getMessage() != null && update.getMessage().getFrom() != null) {
            userName = update.getMessage().getFrom().getUserName();
        }

        if (sub == null) {
            sub = new Subscription();
            sub.setChatId(chatId);
            sub.setUserName(userName);
            subscriptionRepository.save(sub);
            log.info("📦 Создана новая подписка: chatId={}, userName={}", chatId, userName);
        } else if ((sub.getUserName() == null || !sub.getUserName().equals(userName)) && userName != null) {
            sub.setUserName(userName);
            subscriptionRepository.save(sub);
            log.info("📦 userName обновлён: chatId={}, userName={}", chatId, userName);
        }

        // Сохраняем во временную таблицу userName → chatId
        if (userName != null) {
            PendingSubscription pending = new PendingSubscription();
            pending.setUserName(userName);
            pending.setChatId(chatId);
            pending.setCreatedAt(LocalDateTime.now());
            pendingSubscriptionRepository.save(pending);
            log.info("🕓 Сохранено временное сопоставление userName={} → chatId={}", userName, chatId);
        }
    }

    private void handleInterviewCommand(Update update, String chatId, boolean fromWebhook) {
        boolean active = subscriptionService.hasActiveSubscriptionByChatId(chatId);
        log.info("🔍 Подписка пользователя с chatId={} активна: {}", chatId, active);

        if (active) {
            commands.stream()
                    .filter(command -> command.isApplicable(update))
                    .findFirst()
                    .ifPresentOrElse(
                            command -> {
                                String answer = command.process(update, this);
                                sendMessage(chatId, answer);
                            },
                            () -> {
                                if (update.getMessage().hasText() && !"/simulate".equals(update.getMessage().getText())) {
                                    sendMessage(chatId, "🤖 Отвечать нужно только голосовыми сообщениями! Попробуй ещё раз 🎤");
                                }
                            });
        } else {
            sendStartButtons(chatId);
        }
    }

    private void handleCallback(Update update) {
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String data = update.getCallbackQuery().getData();

        if ("check_status".equals(data)) {
            var subscription = subscriptionService.getSubscriptionByChatId(chatId);

            if (subscription == null) {
                sendMessage(chatId, "😔 Подписка не найдена. Убедись, что ты оплатил и подождал несколько секунд.");
                return;
            }

            // ✅ Подписка активна
            if (subscription.getExpiresAt() != null && subscription.getExpiresAt().isAfter(LocalDateTime.now())) {
                sendMessage(chatId, "✅ Подписка активна! 🚀 Можешь начинать с /start");
                simulateStart(chatId, false);
            } else {
                sendMessage(chatId, "⛔ Подписка не активна.");
            }
        }
    }

    public void simulateStart(String chatId, boolean fromWebhook) {
        Update fakeUpdate = new Update();
        Message fakeMessage = new Message();

        Chat chat = new Chat();
        chat.setId(Long.parseLong(chatId));
        fakeMessage.setChat(chat);
        fakeMessage.setText("/simulate");

        fakeMessage.setFrom(new org.telegram.telegrambots.meta.api.objects.User(0L, "", false));
        fakeUpdate.setMessage(fakeMessage);

        handleInterviewCommand(fakeUpdate, chatId, fromWebhook);
    }


    public void sendMessage(String chatId, String text) {
        try {
            execute(new SendMessage(chatId, text));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "JavaInterviewBot_bot";
    }

    private void sendStartButtons(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            sendMessage(chatId, "⚠️ У тебя не установлен username в Telegram. Укажи его в настройках и нажми /start.");
            return;
        }

        // Получаем userName по chatId из подписки
        String userName = subscriptionService.getSubscriptionByChatId(chatId) != null
                ? subscriptionService.getSubscriptionByChatId(chatId).getUserName()
                : null;

        if (userName == null || userName.isBlank()) {
            sendMessage(chatId, "⚠️ У тебя не установлен username в Telegram. Укажи его в настройках и нажми /start.");
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Привет! 👋 Чтобы начать интервью, пожалуйста, оплати подписку:");

        InlineKeyboardButton payButton = new InlineKeyboardButton("💳 Оплатить подписку - 30дней");
        payButton.setUrl(paymentService.createPaymentLink(userName));

        InlineKeyboardButton checkButton = new InlineKeyboardButton("👀 Проверить статус");
        checkButton.setCallbackData("check_status");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(payButton), List.of(checkButton)));

        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
    }

    public void sendMessageToUsername(String username, String text) {
        try {
            execute(new SendMessage("@" + username, text));
        } catch (TelegramApiException e) {
            log.warn("Не удалось отправить сообщение пользователю @{}: {}", username, e.getMessage());
        }
    }
}

