package com.bajaj.quiz;

import com.bajaj.quiz.model.LeaderboardEntry;
import com.bajaj.quiz.model.QuizEvent;
import com.bajaj.quiz.service.QuizService;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        QuizService quizService = new QuizService();
        Set<String> processedEvents = new HashSet<>();
        Map<String, Integer> participantScores = new HashMap<>();

        System.out.println("Starting Quiz Leaderboard System...");

        try {
            // Perform exactly 10 API calls with poll values from 0 to 9
            for (int i = 0; i <= 9; i++) {
                System.out.println("--- Processing Poll " + i + " ---");
                
                try {
                    List<QuizEvent> events = quizService.fetchPollData(i);
                    System.out.println("Fetched " + events.size() + " events.");

                    for (QuizEvent event : events) {
                        String uniqueKey = event.getUniqueKey();
                        
                        // Deduplicate using (roundId + participant) as unique key
                        if (!processedEvents.contains(uniqueKey)) {
                            processedEvents.add(uniqueKey);
                            
                            // Aggregate total scores per participant
                            participantScores.put(
                                event.getParticipant(), 
                                participantScores.getOrDefault(event.getParticipant(), 0) + event.getScore()
                            );
                        } else {
                            System.out.println("Duplicate event ignored: " + uniqueKey);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing poll " + i + ": " + e.getMessage());
                }

                // Maintain a mandatory 5-second delay between each API call
                if (i < 9) {
                    System.out.println("Waiting 5 seconds before next poll...");
                    Thread.sleep(5000);
                }
            }

            System.out.println("\n--- Final Processing ---");

            // Generate leaderboard sorted in descending order of totalScore
            List<LeaderboardEntry> leaderboard = participantScores.entrySet().stream()
                    .map(entry -> new LeaderboardEntry(entry.getKey(), entry.getValue()))
                    .sorted((e1, e2) -> Integer.compare(e2.getTotalScore(), e1.getTotalScore()))
                    .collect(Collectors.toList());

            // Compute total score across all participants
            int grandTotalScore = leaderboard.stream().mapToInt(LeaderboardEntry::getTotalScore).sum();

            System.out.println("Leaderboard:");
            leaderboard.forEach(entry -> System.out.println(entry.getParticipant() + " -> " + entry.getTotalScore()));
            System.out.println("Grand Total Score: " + grandTotalScore);

            // POST /quiz/submit with final leaderboard
            quizService.submitLeaderboard(leaderboard);

            System.out.println("\nQuiz Leaderboard System completed successfully.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execution interrupted.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
