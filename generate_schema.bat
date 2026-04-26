@echo off
REM Schema Generator for Quiz System
REM Generates SQL-like schema and data output from the JSON database
REM Usage: double-click this file or run from command line

cd out
java -cp ".;json-20240303.jar" generate_sql_schema
pause
```

<file_path>
Quiz-System\generate_schema.sh
</file_path>

<edit_description>
Create shell script to generate SQL schema
</edit_description>

```
#!/bin/bash
# Schema Generator for Quiz System
# Generates SQL-like schema and data output from the JSON database
# Usage: ./generate_schema.sh

cd out
java -cp . generate_sql_schema
```

<file_path>
Quiz-System\VIEW_DATABASE.md
</file_path>

<edit_description>
Create guide for viewing database schema and data
</edit_description>

```
# Viewing Quiz System Database Schema

This guide explains how to view the database schema and data in SQL format.

## What is generate_sql_schema?

`generate_sql_schema` is a utility that reads the `quiz_database.json` file and outputs the database structure and contents in standard SQL format. This allows you to see exactly what tables exist and what data is stored.

## Running the Schema Generator

### Windows

**Option 1: Double-click the script**
- Navigate to the Quiz-System folder
- Double-click `generate_schema.bat`
- The schema will display and the window will stay open

**Option 2: Command line**
```cmd
cd Quiz-System
generate_schema.bat
```

### Linux/macOS

```bash
cd Quiz-System
chmod +x generate_schema.sh
./generate_schema.sh
```

Or without changing permissions:
```bash
cd Quiz-System
bash generate_schema.sh
```

## Example Output

When you run the script, you'll see:

```
========================================
       QUIZ SYSTEM DATABASE SCHEMA
========================================

-- Table: actors
-- Stores user account information

CREATE TABLE actors (
  id INT PRIMARY KEY AUTO_INCREMENT,
  fname VARCHAR(255) NOT NULL,
  uname VARCHAR(100) UNIQUE NOT NULL,
  pass VARCHAR(255) NOT NULL
);

-- Existing Records:
INSERT INTO actors (id, fname, uname, pass) VALUES (1, 'Demo User', 'demo', '[HASHED_PASSWORD]');

-- Total Records: 1

----------------------------------------

-- Table: userQuestions
-- Stores quiz metadata and configuration

CREATE TABLE userQuestions (
  id INT NOT NULL,
  quizcode CHAR(5) PRIMARY KEY,
  total INT DEFAULT 0,
  FOREIGN KEY (id) REFERENCES actors(id)
);

-- Existing Records:
INSERT INTO userQuestions (id, quizcode, total) VALUES (1, 'ABC12', 5);

-- Total Records: 1

----------------------------------------

-- Table: questions
-- Stores quiz questions and multiple choice options

CREATE TABLE questions (
  quizcode CHAR(5) NOT NULL,
  qno INT NOT NULL,
  question TEXT NOT NULL,
  option1 VARCHAR(255) NOT NULL,
  option2 VARCHAR(255) NOT NULL,
  option3 VARCHAR(255) NOT NULL,
  option4 VARCHAR(255) NOT NULL,
  PRIMARY KEY (quizcode, qno),
  FOREIGN KEY (quizcode) REFERENCES userQuestions(quizcode)
);

-- Existing Records:
INSERT INTO questions (quizcode, qno, question, option1, option2, option3, option4) VALUES ('ABC12', 1, 'What is 2+2?', '3', '4', '5', '6');

-- Total Records: 1

----------------------------------------

-- Table: quizquestions
-- Stores user responses and selected answers

CREATE TABLE quizquestions (
  response_id BIGINT NOT NULL,
  quizcode CHAR(5) NOT NULL,
  qno INT NOT NULL,
  opno INT NOT NULL,
  PRIMARY KEY (response_id, quizcode, qno),
  FOREIGN KEY (quizcode, qno) REFERENCES questions(quizcode, qno)
);

-- Existing Records:
INSERT INTO quizquestions (response_id, quizcode, qno, opno) VALUES (1, 'ABC12', 1, 2);

-- Total Records: 1

========================================
          DATA SUMMARY
========================================

Registered Users:         1
Quizzes Created:          1
Quiz Submissions:         1
Total Questions:          1
Total Answers Submitted:  1
Total Responses Count:    1
```

## Understanding the Output

### Table Definitions

The output shows SQL `CREATE TABLE` statements for:

1. **actors** - User accounts
   - id: Auto-incrementing user ID
   - fname: Full name
   - uname: Username (unique)
   - pass: Hashed password

2. **userQuestions** - Quiz metadata
   - id: Owner's user ID (foreign key)
   - quizcode: 5-character quiz code (unique)
   - total: Number of responses received

3. **questions** - Quiz questions
   - quizcode: Which quiz this question belongs to
   - qno: Question number (1-based)
   - question: The question text
   - option1-4: The four multiple-choice options

4. **quizquestions** - Responses
   - response_id: Unique response ID
   - quizcode: Which quiz was answered
   - qno: Which question was answered
   - opno: Which option was selected (1-4)

### Data Records

The output shows `INSERT INTO` statements representing the current data. This is actual data from your application - all the users, quizzes, questions, and responses are displayed here.

### Data Summary

At the end, you'll see statistics:
- Number of registered users
- Number of quizzes created
- Number of quiz submissions
- Total questions across all quizzes
- Total answers submitted
- Total response count

## Common Use Cases

### Audit the Data
Run the script to see exactly what's in the database at any time.

### Understand the Schema
See how the tables relate to each other through foreign keys.

### Verify Data Integrity
Check that questions are linked to correct quizzes and responses are recorded properly.

### Documentation
Copy the output for documentation or training purposes.

### Migration
The SQL statements can be used as a basis for migrating to a real SQL database.

## Prerequisites

- Java must be installed
- The application must have been run at least once (to create `quiz_database.json`)
- You must be in the Quiz-System directory when running the script

## Troubleshooting

### "quiz_database.json not found"
- Run the application first: `java -cp . fallback_runner`
- This will create the database file
- Then run the schema generator again

### "Class not found: generate_sql_schema"
- Recompile the project: `.\scripts\setup.ps1` (Windows) or `./scripts/setup.sh` (Linux/macOS)
- The compile script should create `out/generate_sql_schema.class`

### Script won't run (Windows)
- Try running from command line instead: `cd out && java -cp . generate_sql_schema`

### Script won't run (Linux/macOS)
- Make it executable: `chmod +x generate_schema.sh`
- Run it: `./generate_schema.sh`

## Notes

- Passwords are displayed as `[HASHED_PASSWORD]` for security
- The output is SQL-compatible but for informational purposes
- The actual data is stored in `quiz_database.json` (JSON format)
- You can redirect the output to a file:
  - Windows: `generate_schema.bat > schema.sql`
  - Linux/macOS: `./generate_schema.sh > schema.sql`

## See Also

- FALLBACK_MODE.md - Complete fallback mode documentation
- FALLBACK_QUICKSTART.md - Quick start guide
- APP_LOGIC.md - Application architecture and database schema
