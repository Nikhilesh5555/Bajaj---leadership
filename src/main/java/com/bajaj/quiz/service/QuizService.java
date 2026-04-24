package com.bajaj.quiz.service;

import com.bajaj.quiz.model.LeaderboardEntry;
import com.bajaj.quiz.model.QuizEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@EnableAsync
public class QuizService {
    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final String REG_NO = "2024CS101";
    private final ObjectMapper objectMapper = new ObjectMapper();

    // State
    private int currentPoll = -1;
    private String status = "Idle";
    private int grandTotalScore = 0;
    private int eventsProcessed = 0;
    private int duplicatesIgnored = 0;
    private Map<String, Object> submissionResult = new HashMap<>();
    private List<LeaderboardEntry> leaderboard = new ArrayList<>();
    private final Set<String> processedEvents = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<String, Integer> participantScores = new ConcurrentHashMap<>();

    public int getCurrentPoll()                      { return currentPoll; }
    public String getStatus()                        { return status; }
    public int getGrandTotalScore()                  { return grandTotalScore; }
    public int getEventsProcessed()                  { return eventsProcessed; }
    public int getDuplicatesIgnored()                { return duplicatesIgnored; }
    public Map<String, Object> getSubmissionResult() { return submissionResult; }
    public List<LeaderboardEntry> getLeaderboard()   { return leaderboard; }

    @Async
    public void startPolling() {
        if ("Processing".equals(status)) return;

        status = "Processing";
        currentPoll = 0;
        grandTotalScore = 0;
        eventsProcessed = 0;
        duplicatesIgnored = 0;
        submissionResult = new HashMap<>();
        processedEvents.clear();
        participantScores.clear();
        leaderboard.clear();

        try {
            for (int i = 0; i <= 9; i++) {
                currentPoll = i;
                List<QuizEvent> events = fetchPollData(i);

                for (QuizEvent event : events) {
                    String uniqueKey = event.getUniqueKey();
                    if (processedEvents.add(uniqueKey)) {
                        participantScores.merge(event.getParticipant(), event.getScore(), Integer::sum);
                        eventsProcessed++;
                    } else {
                        duplicatesIgnored++;
                    }
                }

                updateLeaderboard();

                if (i < 9) Thread.sleep(5000);
            }

            status = "Submitting";
            submitLeaderboard(leaderboard);
            status = "Completed";

        } catch (Exception e) {
            status = "Error: " + e.getMessage();
            e.printStackTrace();
        }
    }

    private void updateLeaderboard() {
        this.leaderboard = participantScores.entrySet().stream()
                .map(entry -> new LeaderboardEntry(entry.getKey(), entry.getValue()))
                .sorted((e1, e2) -> Integer.compare(e2.getTotalScore(), e1.getTotalScore()))
                .collect(Collectors.toList());
        this.grandTotalScore = leaderboard.stream().mapToInt(LeaderboardEntry::getTotalScore).sum();
    }

    public List<QuizEvent> fetchPollData(int pollId) throws Exception {
        String urlString = BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + pollId;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            JsonNode root = objectMapper.readTree(conn.getInputStream());
            JsonNode eventsNode = root.has("events") ? root.get("events") : root;
            if (eventsNode.isArray()) {
                return objectMapper.convertValue(eventsNode, new TypeReference<List<QuizEvent>>() {});
            }
        }
        return new ArrayList<>();
    }

    public void submitLeaderboard(List<LeaderboardEntry> lb) throws Exception {
        String urlString = BASE_URL + "/quiz/submit";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("regNo", REG_NO);
        payload.put("leaderboard", lb);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(objectMapper.writeValueAsBytes(payload));
        }

        int code = conn.getResponseCode();
        System.out.println("Submission Response Code: " + code);

        // Capture full submission response
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            String body = sb.toString();
            System.out.println("Submission Response: " + body);
            if (!body.isEmpty()) {
                submissionResult = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            }
            submissionResult.put("httpStatus", code);
        }
    }
}
