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

To compile and launch the app later without rebuilding the classpath by hand:

```powershell
./scripts/run.ps1 -ConnectorJar "C:\path\to\mysql-connector-j-8.x.x.jar" -DbUser root -DbPass your_password
```

Or prompt for the database password without putting it in shell history:

```powershell
./scripts/run.ps1 -ConnectorJar "C:\path\to\mysql-connector-j-8.x.x.jar" -DbUser root -PromptForDbPass
```

If you omit `-ConnectorJar`, the script tries to auto-detect a `mysql-connector-j-*.jar` near the repo and its parent folder.

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
./scripts/run.ps1 -ConnectorJar "C:\path\to\mysql-connector-j-8.x.x.jar" -DbUser root -DbPass your_password
```

If MySQL shows `Access denied for user 'root'@'localhost' (using password: NO)`, it means the app launched without a database password. Pass `-DbPass` to `scripts/run.ps1`, use `-PromptForDbPass`, or set `QUIZ_DB_PASS` before starting Java.

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

# From Quiz-System root — compile (once):

.\scripts\setup.ps1

# Run:

cd out
java -cp ".;json-20240303.jar" fallback_runner

# View schema:

.\scripts\view_schema.ps1
