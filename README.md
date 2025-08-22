# Java Interview Bot (Telegram)

Этот проект представляет собой Telegram-бота, который помогает готовиться к собеседованиям по Java. Бот задает вопросы по выбранной теме, принимает голосовые ответы, распознает их с помощью Yandex SpeechKit и предоставляет персонализированную обратную связь с помощью Yandex GPT.

## 🔧 Основные функции

*   **Выбор темы:** Пользователь может выбрать тему для интервью (например, ООП, Коллекции, Spring) через удобное inline-меню с эмодзи.
*   **Голосовые ответы:** Бот принимает только голосовые сообщения в качестве ответов.
*   **Распознавание речи:** Интеграция с Yandex SpeechKit для преобразования голоса в текст.
*   **Интерактивное интервью:** Бот задает серию вопросов (например, 5) по выбранной теме.
*   **Обратная связь от ИИ:** По завершении интервью Yandex GPT анализирует ответы и генерирует подробную, структурированную обратную связь.
*   **Управление сессией:** Бот отслеживает прогресс пользователя в рамках одной сессии.
*   **Команды пользователя:**
    *   `/start` - Начать новое интервью (выбор темы).
    *   `/reset` - Сбросить текущую сессию.
    *   `/skip` - Пропустить текущий вопрос.
    *   `/feedback_now` - Получить обратную связь по текущему интервью досрочно.
*   **Администрирование:**
    *   `/broadcast <текст>` - (Для администратора) Отправить текстовое сообщение всем подписчикам.
    *   `/broadcast` с подписью к фото/документу - (Для администратора) Отправить медиа с подписью всем подписчикам.

## 🛠 Технологии

*   **Язык:** Java 17+
*   **Фреймворк:** Spring Boot
*   **Работа с БД:** Spring Data JPA, PostgreSQL
*   **Telegram API:** `telegrambots-spring-boot-starter`
*   **Внешние API:** Yandex Cloud (SpeechKit для STT, Yandex GPT для генерации текста)
*   **Другое:** Maven, Docker, FFmpeg (для обработки аудио)

## 🚀 Запуск

### Требования

*   Java 21
*   Docker & Docker Compose (рекомендуется)
*   PostgreSQL (если не используешь Docker Compose)
*   Аккаунт в Yandex Cloud с настроенным доступом к SpeechKit и Yandex GPT
*   Telegram бот (токен от @BotFather)
*   (Опционально) Telegram канал, обязательный для подписки

### Конфигурация

Перед запуском создайте файл `application.yaml` (или `application.properties`) и заполните необходимые параметры:

```yaml
# application.yaml
bot:
  token: YOUR_BOT_TOKEN_HERE
  username: YOUR_BOT_USERNAME_HERE # e.g., JavaInterviewBot_bot
  required-channel-id: YOUR_CHANNEL_ID_HERE # (Опционально) ID канала, на который нужно подписаться

yandex:
  api:
    gpt-key: YOUR_YANDEX_GPT_API_KEY_HERE
    speechkit-key: YOUR_YANDEX_SPEECHKIT_API_KEY_HERE
  gpt:
    model-uri: gpt://YOUR_FOLDER_ID/yandexgpt-lite # Или другой используемый URI модели
  speechkit:
    stt-uri: https://stt.api.cloud.yandex.net/speech/v1/stt:recognize
    folder-id: YOUR_YANDEX_FOLDER_ID_HERE

interview:
  max-questions: 5 # Количество вопросов в одном интервью
  session-ttl-minutes: 30 # Время жизни сессии в минутах

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/YOUR_DB_NAME # При использовании Docker Compose поменяй host на db
    username: YOUR_DB_USER
    password: YOUR_DB_PASSWORD
  jpa:
    hibernate:
      ddl-auto: update # Или validate, в зависимости от настроек
    show-sql: false # Установи true для отладки
  main:
    allow-circular-references: true # Необходимо для корректной работы DI

# Логирование (опционально)
logging:
  level:
    org.interview: INFO
