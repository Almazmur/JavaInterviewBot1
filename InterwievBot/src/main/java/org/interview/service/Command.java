package org.interview.service;

import lombok.RequiredArgsConstructor;
import org.interview.client.YandexClient;
import org.interview.repository.InterviewRepository;
import org.interview.repository.TopicRepository;
import org.interview.telegram.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public abstract class Command {

    protected final TopicRepository topicRepository;
    protected final YandexClient yandexClient;
    protected final InterviewRepository interviewRepository;

    public abstract boolean isApplicable(Update update);

    public abstract String process(Update update, Bot bot);
}