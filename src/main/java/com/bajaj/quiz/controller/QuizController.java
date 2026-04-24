package com.bajaj.quiz.controller;

import com.bajaj.quiz.model.LeaderboardEntry;
import com.bajaj.quiz.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @PostMapping("/start")
    public Map<String, String> startPolling() {
        quizService.startPolling();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Polling started");
        return response;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("currentPoll", quizService.getCurrentPoll());
        response.put("status", quizService.getStatus());
        response.put("grandTotalScore", quizService.getGrandTotalScore());
        response.put("eventsProcessed", quizService.getEventsProcessed());
        response.put("duplicatesIgnored", quizService.getDuplicatesIgnored());
        response.put("submissionResult", quizService.getSubmissionResult());
        return response;
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntry> getLeaderboard() {
        return quizService.getLeaderboard();
    }
}
