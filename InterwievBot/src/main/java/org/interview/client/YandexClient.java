package org.interview.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class YandexClient {

    @Value("${yandex.api.gpt-key}")
    private String gptApiKey;

    @Value("${yandex.api.speechkit-key}")
    private String speechkitApiKey;

    @Value("${yandex.gpt.model-uri}")
    private String gptUri;

    @Value("${yandex.speechkit.stt-uri}")
    private String sttUri;

    @Value("${yandex.speechkit.folder-id}")
    private String folderId;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public String generateAnswer(String prompt) {
        return promptModel(prompt);
    }

    public String promptModel(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + gptApiKey); // 🛠 здесь исправили на gptApiKey

        Map<String, Object> requestBody = Map.of(
                "modelUri", "gpt://" + folderId + "/yandexgpt-lite",
                "completionOptions", Map.of("stream", false, "temperature", 0.6),
                "messages", List.of(
                        Map.of("role", "system", "text", "Ты Java интервьюер..."),
                        Map.of("role", "user", "text", prompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(gptUri, request, Map.class);

        List<Map<String, Object>> result = (List<Map<String, Object>>) ((Map) response.getBody().get("result")).get("alternatives");
        Map<String, Object> message = (Map<String, Object>) result.get(0).get("message");
        return message.get("text").toString();
    }

    public String transcribe(File file) {
        try {
            System.out.println("📤 Отправка файла на распознавание через SpeechKit: " + file.getName());
            byte[] audioBytes = Files.readAllBytes(file.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Authorization", "Api-Key " + speechkitApiKey);
            System.out.println("✅ SpeechKit Api-Key установлен");

            String fullUrl = sttUri + "?topic=general&lang=ru-RU&folderId=" + folderId;
            System.out.println("🌍 STT URL: " + fullUrl);

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(audioBytes, headers);
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.POST, requestEntity, String.class);

            System.out.println("✅ Ответ от STT: " + response.getBody());

            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("result").asText();
        } catch (HttpClientErrorException.BadRequest e) {
            // Если ошибка 400 и сообщение связано с длинной аудио
            String responseBody = e.getResponseBodyAsString();
            if (responseBody.contains("audio duration should be less than 30s")) {
                System.err.println("⚠️ Аудио дольше 30 секунд!");
                return "⚠️ Голосовое сообщение слишком длинное (больше 30 секунд)! Пожалуйста, отправь покороче.";
            }
            throw new RuntimeException("Ошибка при распознавании речи: " + e.getMessage(), e);
        } catch (IOException e) {
            System.err.println("❌ Ошибка чтения файла: " + e.getMessage());
            throw new RuntimeException("Ошибка чтения файла", e);
        } catch (Exception e) {
            System.err.println("❌ Ошибка при распознавании речи: " + e.getMessage());
            throw new RuntimeException("Ошибка при распознавании речи", e);
        }
    }

    public File convertToOggOpus(File inputFile) throws IOException, InterruptedException {
        File outputFile = new File(inputFile.getAbsolutePath() + "_converted.ogg");

        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", inputFile.getAbsolutePath(),
                "-c:a", "libopus",
                "-b:a", "64k",
                "-f", "ogg",
                "-application", "voip",
                outputFile.getAbsolutePath()
        );

        Process process = builder.inheritIO().start();
        int exitCode = process.waitFor();
        if (exitCode != 0 || !outputFile.exists()) {
            throw new IOException("FFmpeg conversion failed");
        }

        return outputFile;
    }
}
