package org.interview.config;

import org.interview.repository.SubscriptionRepository;
import org.interview.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommandConfig {
    @Bean
    public MySubscriptionCommand mySubscriptionCommand(SubscriptionRepository repository) {
        return new MySubscriptionCommand(repository);
    }

    @Bean
    public List<Command> commandList(StartCommand startCommand,
                                     VoiceCommand voiceCommand,
                                     ResetCommand resetCommand,
                                     SkipCommand skipCommand,
                                     FeedbackNowCommand feedbackNowCommand,
                                     MySubscriptionCommand mySubscriptionCommand) {
        return List.of(
                startCommand,
                voiceCommand,
                resetCommand,
                skipCommand,
                feedbackNowCommand,
                mySubscriptionCommand  // добавил сюда новую команду
        );
    }
}
