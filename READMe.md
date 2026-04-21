# Quiz Management System

Simple Java Swing quiz app with a MySQL backend. You can create quizzes, share 5-character quiz codes, collect guest responses, and view per-question analytics.

## Requirements

- Java JDK 8+
- MySQL Server 5.7+
- MySQL Connector/J 8.x JAR (required only when running)

## Quick Setup (Recommended)

### Windows (PowerShell)

From the project root:

```powershell
./scripts/setup.ps1
```

If your MySQL credentials are different, pass them directly:

```powershell
./scripts/setup.ps1 -DbUser root -DbPass your_password
```

### Linux / macOS (bash)

```bash
chmod +x scripts/setup.sh
./scripts/setup.sh
```

Both scripts:

- apply database schema and seed data (if `mysql` CLI is installed)
- compile Java sources to `out/`
- print the run command template

## Manual Setup

1. Run SQL file in MySQL:

```sql
SOURCE database/schema_seed.sql;
```

2. Set database environment variables (optional, defaults are built in):

- `QUIZ_DB_URL`
- `QUIZ_DB_USER`
- `QUIZ_DB_PASS`

Default URL:

`jdbc:mysql://localhost:3306/survey?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`

3. Compile:

```powershell
javac -d out src\*.java
```

4. Run (replace JAR path):

```powershell
java -cp ".;out;C:\path\to\mysql-connector-j-8.x.x.jar" runner
```

Linux/macOS run template:

```bash
java -cp ".:out:/path/to/mysql-connector-j-8.x.x.jar" runner
```

## Included Sample Data

The seed file `database/schema_seed.sql` inserts a demo account and preset computer science quizzes.

### Demo account

- Username: `demo`
- Password: `demo123`

### Preset quiz codes

- `CS101` (computer science fundamentals)
- `ALGO1` (algorithms)
- `DBMS1` (database systems)

Guests can use these codes from the login screen immediately after seeding.

## Project Logic Documentation

Detailed architecture, flow, data model, and transaction logic is documented in:

- `APP_LOGIC.md`

## Notes

- Existing plain-text passwords from older app versions still authenticate.
- New and changed passwords are stored as SHA-256 (`sha256$...`).
- App remains in the default Java package for simplicity.
