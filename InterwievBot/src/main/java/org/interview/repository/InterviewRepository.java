package org.interview.repository;

import org.interview.dto.Question;
import org.springframework.stereotype.Repository;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Repository
public class InterviewRepository {

    private final Map<String, Deque<Question>> userQuestions = new HashMap<>();

    public void startInterview(String chatId) {
        userQuestions.put(chatId, new LinkedList<>());
    }

    public void addQuestion(String chatId, String question) {
        Question dto = new Question();
        dto.setQuestion(question);
        userQuestions.computeIfAbsent(chatId, k -> new LinkedList<>()).add(dto);
    }

    public void addAnswer(String chatId, String answer) {
        Deque<Question> questions = userQuestions.get(chatId);
        if (questions == null) {
            throw new IllegalStateException("There is no interview session starter for user " + chatId);
        }

        Question question = questions.peekLast();
        if (question == null) {
            throw new IllegalStateException("There is no question being answered for user " + chatId);
        }

        question.setAnswer(answer);
    }

    public Deque<Question> finishInterview(String chatId) {
        return userQuestions.remove(chatId);
    }

    public int getUserQuestions(String chatId) {
        return userQuestions.getOrDefault(chatId, new LinkedList<>()).size();
    }

    public int getNextQuestionNumber(String chatId) {
        return getUserQuestions(chatId) + 1;
    }

    public boolean hasActiveSession(String chatId) {
        return userQuestions.containsKey(chatId);
    }
}
