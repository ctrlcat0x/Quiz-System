# Quiz System - Fallback Mode Quick Start

**No MySQL required!** Run the Quiz System with JSON-based local storage.

## ⚡ Quick Setup (2 minutes)

### 1. Compile
```bash
# Windows PowerShell
cd "C:\Users\ctrlcat0x\Desktop\Quiz-System"
.\scripts\setup.ps1

# Linux/macOS
cd ~/Quiz-System
./scripts/setup.sh
```

### 2. Run
```bash
cd out
java -cp . fallback_runner
```

### 3. Login
- Username: `demo`
- Password: `demo123`

**Done!** The app is running. All data saves to `quiz_database.json`.

---

## 🎯 First Demo (5 minutes)

### Create a Quiz
1. Click **"Quiz Builder"** tab
2. Enter question: "What is 2+2?"
3. Enter options: "3", "4", "5", "6"
4. Click **"Add Question"**
5. Click **"Publish Quiz"**
6. Copy the quiz code (e.g., `ABC12`)

### Take the Quiz as Guest
1. Click **"Logout"** (or use incognito window)
2. Click **"Continue as Guest"**
3. Enter the quiz code
4. Select an answer
5. Click **"Submit"**

### View Results
1. Log back in as `demo`
2. Click **"Quiz Library"** tab
3. Click the quiz code
4. See: response count, vote distribution, timestamps

---

## 📁 Data Storage

Everything is saved in: **`quiz_database.json`**

Reset everything:
```bash
rm quiz_database.json        # Linux/macOS
del quiz_database.json       # Windows
```

Then restart the app.

---

## 🔄 Switch Between Modes

**Fallback (JSON):**
```bash
cd out && java -cp . fallback_runner
```

**SQL (MySQL):**
```bash
cd out && java -cp .:mysql-connector-java-*.jar runner
```

---

## ✅ Features Supported

✅ Account creation & login  
✅ Quiz creation with 4-option questions  
✅ Auto-generated quiz codes (5 characters)  
✅ Guest submissions  
✅ Analytics & vote counts  
✅ Response history with timestamps  
✅ Delete quizzes & all responses  

---

## 🆘 Troubleshooting

| Problem | Solution |
|---------|----------|
| "Class not found" | Run `.\scripts\setup.ps1` (Windows) or `./scripts/setup.sh` (Linux) |
| "Cannot read JSON" | Delete `quiz_database.json` and restart |
| Data not saving | Check write permissions in the directory |
| Cannot create user | Username may exist—try a different name |
| Quiz code invalid | Case-insensitive but must match exactly |

---

## 📚 Full Documentation

See **FALLBACK_MODE.md** for complete guide with:
- Detailed workflow examples
- File format reference
- Security notes
- Advanced usage
- Performance considerations

---

## 🚀 Common Commands

```bash
# Compile
.\scripts\setup.ps1              # Windows
./scripts/setup.sh               # Linux/macOS

# Run
cd out
java -cp . fallback_runner       # Fallback mode
java -cp .:mysql-connector-java-*.jar runner  # SQL mode

# Reset
rm quiz_database.json            # Linux/macOS
del quiz_database.json           # Windows
```

---

## 🎓 Next Steps

1. Create a quiz with 5 questions
2. Share code with a friend
3. They take it as guest
4. View analytics
5. Create more quizzes
6. Test the search/filter feature
7. Delete a quiz (removes all responses)

**All data is automatically saved!**

---

## ℹ️ Demo Account

- **Username:** `demo`
- **Password:** `demo123`

Or create your own account on the signup screen.

---

**Ready to demo?** Run `java -cp . fallback_runner` and start creating quizzes! 🎉