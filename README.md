# 🏆 Quiz Leaderboard System

A complete Java backend application that polls quiz data from an external API, deduplicates results, aggregates scores, and submits a final sorted leaderboard.

---

## 📌 Overview

This project simulates a real-world backend integration scenario where quiz results are fetched multiple times from an external validator system. Since duplicate responses may occur, the application ensures data integrity by filtering duplicates and producing an accurate leaderboard.

---

## ⚙️ Tech Stack

* **Language:** Java 17
* **Build Tool:** Maven
* **Libraries:** Jackson (JSON processing)

---

## 🚀 Features

* **Strict Deduplication**
  Uses `roundId + participant` to ensure each score for a round is counted only once.

* **Aggregation**
  Totals scores for each unique participant across all rounds.

* **Sorting**
  Generates a leaderboard sorted by total score in descending order.

* **Submission**
  Sends the final results to the submission endpoint automatically.

* **Graceful Error Handling**
  Logs API failures and continues processing without interruption.

---

## 🗂️ Project Structure

```id="a1b2c3"
src/main/java/com/bajaj/quiz/
│
├── Main.java                 # Entry point and orchestration logic
├── service/
│   └── QuizService.java     # Handles API calls and JSON processing
└── model/                   # Data models for Quiz Events & Leaderboard
```

```id="d4e5f6"
pom.xml                      # Maven configuration with dependencies
```

---

## 🛠️ Prerequisites

Ensure the following are installed:

* Java 17 or higher
* Maven 3.6+

---

## ▶️ How to Run

### 1. Clone the Repository

```bash id="g7h8i9"
git clone <your-repo-url>
cd <your-project-folder>
```

> Alternatively, navigate manually:

```bash id="j1k2l3"
cd c:\Users\NIKHIL\Desktop\Bajaj
```

---

### 2. Clean and Compile

```bash id="m4n5o6"
mvn clean compile
```

---

### 3. Execute the Application

```bash id="p7q8r9"
mvn exec:java
```

---

## ⏱️ Execution Details

* The application performs **10 API polls**
* Each poll runs with a **5-second delay**
* Total execution time is approximately **1 minute**

---

## 🧠 Core Workflow

1. Fetch quiz data from the API
2. Remove duplicate entries
3. Aggregate scores for each participant
4. Sort participants by total score (descending)
5. Submit the final leaderboard

---

## 📊 Sample Output

```id="s1t2u3"
1. Alice   - 95
2. Bob     - 88
3. Charlie - 80
```

---

## ⚠️ Error Handling

* Logs API failures without stopping execution
* Skips duplicate or invalid entries
* Ensures consistent and reliable processing

---

## 📈 Future Improvements

* Add REST API using Spring Boot
* Integrate database (MySQL/PostgreSQL)
* Add unit and integration testing
* Dockerize for deployment

---

## 👤 Author

**Nikhil**

---

## 📄 License

This project is intended for educational and internship assessment purposes.
