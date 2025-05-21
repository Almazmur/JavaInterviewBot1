package org.interview.service;

import org.interview.config.RobokassaConfig;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Formatter;

@Service
public class PaymentService {

    private final RobokassaConfig config;

    public PaymentService(RobokassaConfig config) {
        this.config = config;
    }

    public String createPaymentLink(String userName) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("Username отсутствует. Нельзя сгенерировать ссылку оплаты.");
        }

        String amount = "499.00";
        int invId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

        String signature = generateSignature(config.getMerchantLogin(), amount, String.valueOf(invId), config.getPassword1(), userName);

        return "https://auth.robokassa.ru/Merchant/Index.aspx?" +
                "MerchantLogin=" + config.getMerchantLogin() +
                "&OutSum=" + amount +
                "&InvId=" + invId +
                "&Description=Подписка+на+JavaInterviewBot" +
                "&SignatureValue=" + signature +
                "&IsTest=0" +
                "&Shp_user=" + userName;
    }

    private String generateSignature(String login, String sum, String invId, String password1, String userId) {
        String data = login + ":" + sum + ":" + invId + ":" + password1 + ":Shp_user=" + userId;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
            Formatter formatter = new Formatter();
            for (byte b : digest) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации подписи MD5", e);
        }
    }
}
