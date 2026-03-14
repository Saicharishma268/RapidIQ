RapidIQ - Java Quiz Platform
A desktop-based quiz application built with Java Swing and MySQL.
Features

User Login and Signup with role-based access (User and Admin)
3-Level progression system (Beginner, Intermediate, Advanced)
Levels unlock based on score (Beginner needs 70%, Intermediate needs 80%)
Timed quizzes with auto-advance when timer runs out
Instant answer feedback with correct/wrong highlighting
Admin dashboard to manage topics and questions
Maximum 5 questions per topic
Leaderboard showing top 5 users with points
Overall progress tracking across all topics
Quiz attempt history stored in database

Tech Stack

Language: Java
UI Framework: Java Swing
Database: MySQL
Connectivity: JDBC
IDE: VS Code

Database Tables
users, levels, topics, questions, quiz_attempts, user_scores, level_progress
How to Run









Install Java JDK and MySQL
Create database named rapidiq
Add mysql-connector-j-9.6.0.jar to classpath
Compile: javac -cp ".;mysql-connector-j-9.6.0.jar" src/*.java
Run: java -cp ".;mysql-connector-j-9.6.0.jar" LoginPage

Developer

Name: Saicharishma
GitHub: https://github.com/Saicharishma268
