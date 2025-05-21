# 1. Минимальный образ с Java 21 на Ubuntu (есть apt-get)
FROM eclipse-temurin:21-jdk-jammy

# 2. Установить ffmpeg
RUN apt-get update && apt-get install -y ffmpeg && apt-get clean && rm -rf /var/lib/apt/lists/*

# 3. Рабочая директория
WORKDIR /app

# 4. Копируем .jar файл
COPY InterviewBot-1.0-SNAPSHOT.jar app.jar

# 5. Копируем конфиг
COPY application.yaml /app/application.yaml

# 6. Запуск
CMD ["java", "-jar", "app.jar", "--spring.config.location=classpath:/,file:./application.yaml"]
