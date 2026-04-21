# Quiz System - Working and Logic Guide

This document explains how the app works end-to-end: architecture, screen flow, database flow, and core logic.

## 1. High-Level Architecture

- UI layer: Java Swing screens in src/
- Domain layer: QuizQuestion and QuizSummary model classes
- Data layer: SQLoperations (all database reads/writes)
- Theme layer: AppTheme (shared tokens and UI component factories)

The project intentionally uses the default Java package to stay simple.

## 2. Application Entry and Initialization

- Entry point: src/runner.java
- runner calls AppTheme.applyLookAndFeel()
- runner opens login.loginView()

At runtime, DB settings are read from:

- QUIZ_DB_URL
- QUIZ_DB_USER
- QUIZ_DB_PASS

If these are not set, SQLoperations uses localhost defaults.

## 3. Screen Flow

### 3.1 Login Screen (src/login.java)

- User can:
  - Sign in
  - Open sign-up screen
  - Continue as guest with a 5-character quiz code
- Sign in calls SQLoperations.authUser(username, password)
- On success, app opens mainpage.mainPageView(userId)

### 3.2 Sign-Up Screen (src/signup.java)

- Validates:
  - all fields filled
  - username has no spaces
  - password length >= 6
  - password and confirm match
- Uses SQLoperations.checkUsername() to prevent duplicates
- Creates account using SQLoperations.newUser()

### 3.3 Main Dashboard (src/mainpage.java)

Main dashboard has two views under a CardLayout:

- Builder view:
  - Add question + 4 options into draft list
  - Remove/clear draft
  - Publish quiz
- Library view:
  - Search quiz codes
  - View response counts
  - Browse question analytics
  - Delete quiz

Publish flow:

- Collects draft List<QuizQuestion>
- Calls SQLoperations.createQuiz(userId, draftQuestions)
- Generated 5-character quiz code is copied to clipboard
- Automatically switches to library view

### 3.4 Guest Quiz Screen (src/guest.java)

- Loads quiz questions using SQLoperations.getQuizQuestions(code)
- User answers one question at a time
- Supports previous/next navigation
- On submit, sends all answers to SQLoperations.submitQuizResponses(code, answers)

## 4. Database and Data Contracts

### 4.1 Tables

- actors: user accounts
- userQuestions: quiz owner + quiz code + total response count
- questions: ordered questions by (quizcode, qno)
- quizquestions: guest responses per question (opno = selected option)

### 4.2 Question Ordering

questions.qno stores the display order. UI and analytics always read questions ordered by qno.

### 4.3 Quiz Code Generation

- SQLoperations.stringGenerator() builds 5-character code from:
  - ABCDEFGHJKLMNPQRSTUVWXYZ23456789
- SQLoperations.generateUniqueCode() loops until code is not present in userQuestions

## 5. Security and Validation

### 5.1 Password Storage

- New passwords are stored as SHA-256 with prefix:
  - sha256$<hex>
- passwordMatches() supports legacy plain-text rows for backward compatibility

### 5.2 Input Validation

Validation happens in both UI and data operations:

- UI prevents empty forms and invalid combinations
- SQLoperations.requireNotBlank() protects key write operations
- Prepared statements are used everywhere (prevents SQL injection)

## 6. Transactional Behavior

The following methods use transactions (commit/rollback):

- createQuiz()
- submitQuizResponses()
- removeSurvey()

This guarantees consistent writes for multi-step operations.

## 7. Analytics Logic

For selected quiz in library view:

- getQuizQuestions(code) loads prompts/options
- For each question index i:
  - getVoteCounts(code, i + 1)
- UI renders per-option vote badges and total response count

## 8. Seed Data Behavior

database/schema_seed.sql creates schema and inserts:

- Demo account:
  - username: demo
  - password: demo123
- Preset quizzes:
  - CS101
  - ALGO1
  - DBMS1

Each seed run is idempotent:

- Uses CREATE TABLE IF NOT EXISTS
- Uses ON DUPLICATE KEY UPDATE for sample records
- Clears old sample quiz responses and resets totals

## 9. Setup Automation

- scripts/setup.ps1 for Windows PowerShell
- scripts/setup.sh for Linux/macOS bash

Both scripts:

- optionally apply schema + seed data via mysql CLI
- compile sources into out/
- print run command template with connector JAR placeholder

## 10. Key Files Map

- src/runner.java: app entry
- src/AppTheme.java: shared visual system
- src/login.java: authentication and guest code entry
- src/signup.java: account creation
- src/mainpage.java: quiz builder + analytics dashboard
- src/guest.java: guest quiz-taking flow
- src/SQLoperations.java: all DB operations
- src/QuizQuestion.java: question model
- src/QuizSummary.java: quiz list model
- database/schema_seed.sql: schema + sample data
- scripts/setup.ps1 and scripts/setup.sh: quick setup automation
