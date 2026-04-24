# Quiz Leaderboard System

A complete Java backend application that polls quiz data, deduplicates results, and submits a final sorted leaderboard.

## Prerequisites
- Java 17 or higher
- Maven 3.6+

## Project Structure
- `src/main/java/com/bajaj/quiz/Main.java`: The main entry point and orchestration logic.
- `src/main/java/com/bajaj/quiz/service/QuizService.java`: Service for handling API calls and JSON processing.
- `src/main/java/com/bajaj/quiz/model/`: Data models for Quiz Events and Leaderboard entries.
- `pom.xml`: Maven configuration with Jackson dependencies.

## How to Run

1. **Clone or Navigate** to the project directory:
   ```bash
   cd c:\Users\NIKHIL\Desktop\Bajaj
   ```

2. **Clean and Compile**:
   ```bash
   mvn clean compile
   ```

3. **Execute the Application**:
   ```bash
   mvn exec:java
   ```

The application will run for approximately 1 minute due to the mandatory 5-second delays between the 10 polls.

## Features
- **Strict Deduplication**: Uses `roundId` + `participant` to ensure each score for a round is only counted once.
- **Aggregation**: Totals scores for each unique participant across all rounds.
- **Sorting**: Generates a leaderboard sorted by total score in descending order.
- **Submission**: Sends the final results to the submission endpoint automatically.
- **Graceful Error Handling**: Logs API failures and continues processing.
