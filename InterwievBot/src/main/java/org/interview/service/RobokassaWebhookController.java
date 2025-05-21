package org.interview.service;

import org.interview.config.RobokassaConfig;
import org.interview.telegram.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.Map;

@RestController
@RequestMapping("/robokassa")
public class RobokassaWebhookController {

    private static final Logger log = LoggerFactory.getLogger(RobokassaWebhookController.class);

    private final SubscriptionService subscriptionService;
    private final Bot bot;
    private final RobokassaConfig config;

    public RobokassaWebhookController(SubscriptionService subscriptionService, Bot bot, RobokassaConfig config) {
        this.subscriptionService = subscriptionService;
        this.bot = bot;
        this.config = config;
    }

    @PostMapping("/result")
    public String handlePayment(@RequestParam Map<String, String> params) {
        log.info("📥 Получено уведомление от Robokassa: {}", params);

        String outSum = params.get("OutSum");
        String invId = params.get("InvId");
        String receivedSignature = params.get("SignatureValue");
        String userId = params.get("Shp_user");

        String expectedSignature = generateSignature(outSum, invId, config.getPassword2(), userId);
        if (!expectedSignature.equalsIgnoreCase(receivedSignature)) {
            log.warn("❌ Неверная подпись платежа Robokassa для InvId={}", invId);
            return "Invalid signature";
        }

        String chatId = subscriptionService.getChatIdByUserName(userId);
        if (chatId == null) {
            log.warn("❗ chatId не найден для userName={}, отмена обработки платежа.", userId);
            return "OK" + invId;
        }

        // ✅ Отвечаем сразу
        new Thread(() -> {
            try {
                log.info("📦 Продлеваем подписку для chatId={}...", chatId);
                subscriptionService.saveOrUpdateSubscription(userId, chatId);
                bot.sendMessage(chatId, "✅ Платёж успешно получен! Нажми /start чтобы начать.");
                bot.simulateStart(chatId, true);
            } catch (Exception e) {
                log.error("❗ Ошибка при асинхронной обработке подписки: {}", e.getMessage(), e);
            }
        }).start();

        return "OK" + invId;
    }

    private String generateSignature(String outSum, String invId, String password2, String userId) {
        String data = outSum + ":" + invId + ":" + password2 + ":Shp_user=" + userId;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
            Formatter formatter = new Formatter();
            for (byte b : digest) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } catch (Exception e) {
            log.error("❗ Ошибка генерации подписи MD5: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка генерации подписи MD5", e);
        }
    }
}


