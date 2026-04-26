# view_schema.ps1 - Outputs SQL-like schema and data from quiz_database.json
# Run from the Quiz-System root: .\scripts\view_schema.ps1

$dbPath = Join-Path $PSScriptRoot "..\out\quiz_database.json"
if (-not (Test-Path $dbPath)) {
    $dbPath = Join-Path $PSScriptRoot "..\quiz_database.json"
}
if (-not (Test-Path $dbPath)) {
    Write-Error "quiz_database.json not found. Run the application first."
    exit 1
}

$db = Get-Content $dbPath -Raw | ConvertFrom-Json

function Format-SqlString($val) {
    if ($null -eq $val) { return "" }
    return $val.ToString().Replace("'", "''").Replace("\", "\\")
}

Write-Host "========================================"
Write-Host "       QUIZ SYSTEM DATABASE SCHEMA      "
Write-Host "========================================"
Write-Host ""

# â”€â”€ actors â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
Write-Host "-- Table: actors"
Write-Host "-- Stores user account information"
Write-Host ""
Write-Host "CREATE TABLE actors ("
Write-Host "  id    INT          PRIMARY KEY AUTO_INCREMENT,"
Write-Host "  fname VARCHAR(255) NOT NULL,"
Write-Host "  uname VARCHAR(100) UNIQUE NOT NULL,"
Write-Host "  pass  VARCHAR(255) NOT NULL"
Write-Host ");"
Write-Host ""

$actors = $db.actors
if ($actors -and $actors.Count -gt 0) {
    Write-Host "-- Existing Records:"
    foreach ($a in $actors) {
        $id    = $a.id
        $fname = Format-SqlString $a.fname
        $uname = Format-SqlString $a.uname
        $pass  = Format-SqlString $a.pass
        Write-Host "INSERT INTO actors (id, fname, uname, pass) VALUES ($id, '$fname', '$uname', '$pass');"
    }
    Write-Host ""
    Write-Host "-- Total Records: $($actors.Count)"
} else {
    Write-Host "-- No records."
}

Write-Host ""
Write-Host "----------------------------------------"
Write-Host ""

# â”€â”€ userQuestions â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
Write-Host "-- Table: userQuestions"
Write-Host "-- Stores quiz metadata and configuration"
Write-Host ""
Write-Host "CREATE TABLE userQuestions ("
Write-Host "  id       INT      NOT NULL,"
Write-Host "  quizcode CHAR(5)  PRIMARY KEY,"
Write-Host "  total    INT      DEFAULT 0,"
Write-Host "  FOREIGN KEY (id) REFERENCES actors(id)"
Write-Host ");"
Write-Host ""

$quizzes = $db.quizzes
if ($quizzes -and $quizzes.Count -gt 0) {
    Write-Host "-- Existing Records:"
    foreach ($q in $quizzes) {
        $id       = $q.id
        $quizcode = $q.quizcode
        $total    = $q.total
        Write-Host "INSERT INTO userQuestions (id, quizcode, total) VALUES ($id, '$quizcode', $total);"
    }
    Write-Host ""
    Write-Host "-- Total Records: $($quizzes.Count)"
} else {
    Write-Host "-- No records."
}

Write-Host ""
Write-Host "----------------------------------------"
Write-Host ""

# â”€â”€ questions â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
Write-Host "-- Table: questions"
Write-Host "-- Stores quiz questions and multiple choice options"
Write-Host ""
Write-Host "CREATE TABLE questions ("
Write-Host "  quizcode CHAR(5)      NOT NULL,"
Write-Host "  qno      INT          NOT NULL,"
Write-Host "  question TEXT         NOT NULL,"
Write-Host "  option1  VARCHAR(255) NOT NULL,"
Write-Host "  option2  VARCHAR(255) NOT NULL,"
Write-Host "  option3  VARCHAR(255) NOT NULL,"
Write-Host "  option4  VARCHAR(255) NOT NULL,"
Write-Host "  PRIMARY KEY (quizcode, qno),"
Write-Host "  FOREIGN KEY (quizcode) REFERENCES userQuestions(quizcode)"
Write-Host ");"
Write-Host ""

$totalQ = 0
if ($quizzes -and $quizzes.Count -gt 0) {
    Write-Host "-- Existing Records:"
    foreach ($quiz in $quizzes) {
        $qc = $quiz.quizcode
        if ($quiz.questions) {
            foreach ($q in $quiz.questions) {
                $qno = $q.qno
                $question = Format-SqlString $q.question
                $o1 = Format-SqlString $q.option1
                $o2 = Format-SqlString $q.option2
                $o3 = Format-SqlString $q.option3
                $o4 = Format-SqlString $q.option4
                Write-Host "INSERT INTO questions (quizcode, qno, question, option1, option2, option3, option4) VALUES ('$qc', $qno, '$question', '$o1', '$o2', '$o3', '$o4');"
                $totalQ++
            }
        }
    }
    Write-Host ""
    Write-Host "-- Total Records: $totalQ"
} else {
    Write-Host "-- No records."
}

Write-Host ""
Write-Host "----------------------------------------"
Write-Host ""

# â”€â”€ quizquestions (responses) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
Write-Host "-- Table: quizquestions"
Write-Host "-- Stores user responses and selected answers"
Write-Host ""
Write-Host "CREATE TABLE quizquestions ("
Write-Host "  response_id BIGINT  NOT NULL,"
Write-Host "  quizcode    CHAR(5) NOT NULL,"
Write-Host "  qno         INT     NOT NULL,"
Write-Host "  opno        INT     NOT NULL,"
Write-Host "  PRIMARY KEY (response_id, quizcode, qno),"
Write-Host "  FOREIGN KEY (quizcode, qno) REFERENCES questions(quizcode, qno)"
Write-Host ");"
Write-Host ""

$responses = $db.responses
$totalA = 0
if ($responses -and $responses.Count -gt 0) {
    Write-Host "-- Existing Records:"
    foreach ($r in $responses) {
        $rid      = $r.response_id
        $quizcode = $r.quizcode
        $answers  = $r.answers
        if ($answers) {
            for ($i = 0; $i -lt $answers.Count; $i++) {
                $qno  = $i + 1
                $opno = $answers[$i]
                Write-Host "INSERT INTO quizquestions (response_id, quizcode, qno, opno) VALUES ($rid, '$quizcode', $qno, $opno);"
                $totalA++
            }
        }
    }
    Write-Host ""
    Write-Host "-- Total Records: $totalA"
} else {
    Write-Host "-- No records."
}

Write-Host ""
Write-Host "========================================"
Write-Host "            DATA SUMMARY                "
Write-Host "========================================"
Write-Host ""

$userCount   = if ($actors)   { $actors.Count   } else { 0 }
$quizCount   = if ($quizzes)  { $quizzes.Count  } else { 0 }
$respCount   = if ($responses){ $responses.Count} else { 0 }

$totalResponses = 0
if ($quizzes) {
    foreach ($q in $quizzes) { $totalResponses += $q.total }
}

Write-Host ("Registered Users:        " + $userCount)
Write-Host ("Quizzes Created:         " + $quizCount)
Write-Host ("Quiz Submissions:        " + $respCount)
Write-Host ("Total Questions:         " + $totalQ)
Write-Host ("Total Answers Submitted: " + $totalA)
Write-Host ("Total Responses Count:   " + $totalResponses)
Write-Host ""

