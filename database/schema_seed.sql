CREATE DATABASE IF NOT EXISTS survey;
USE survey;

CREATE TABLE IF NOT EXISTS actors (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fname VARCHAR(100) NOT NULL,
    uname VARCHAR(50) NOT NULL UNIQUE,
    pass VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS userQuestions (
    id INT NOT NULL,
    quizcode CHAR(5) NOT NULL PRIMARY KEY,
    total INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS questions (
    quizcode CHAR(5) NOT NULL,
    qno INT NOT NULL,
    question VARCHAR(255) NOT NULL,
    option1 VARCHAR(255) NOT NULL,
    option2 VARCHAR(255) NOT NULL,
    option3 VARCHAR(255) NOT NULL,
    option4 VARCHAR(255) NOT NULL,
    PRIMARY KEY (quizcode, qno)
);

CREATE TABLE IF NOT EXISTS quizquestions (
    quizcode CHAR(5) NOT NULL,
    qno INT NOT NULL,
    opno INT NOT NULL
);

INSERT INTO actors (id, fname, uname, pass)
VALUES (9001, 'Demo Instructor', 'demo', 'sha256$d3ad9315b7be5dd53b31a273b3b3aba5defe700808305aa16a3062b76658a791')
ON DUPLICATE KEY UPDATE
    fname = VALUES(fname),
    pass = VALUES(pass);

INSERT INTO userQuestions (id, quizcode, total)
VALUES
    (9001, 'CS101', 0),
    (9001, 'ALGO1', 0),
    (9001, 'DBMS1', 0)
ON DUPLICATE KEY UPDATE
    id = VALUES(id);

INSERT INTO questions (quizcode, qno, question, option1, option2, option3, option4)
VALUES
    ('CS101', 1, 'What is the time complexity of binary search on a sorted array?', 'O(log n)', 'O(n)', 'O(n log n)', 'O(1)'),
    ('CS101', 2, 'Which data structure follows FIFO order?', 'Queue', 'Stack', 'Tree', 'Graph'),
    ('CS101', 3, 'Which protocol secures web traffic with encryption?', 'HTTPS', 'HTTP', 'FTP', 'SMTP'),
    ('CS101', 4, 'Which unit performs arithmetic and logic operations in a CPU?', 'ALU', 'RAM', 'Cache', 'Control Bus'),

    ('ALGO1', 1, 'Dijkstra''s algorithm assumes edge weights are:', 'Non-negative', 'All equal', 'Negative only', 'Prime numbers'),
    ('ALGO1', 2, 'What is the average and worst-case time complexity of merge sort?', 'O(n log n)', 'O(n)', 'O(log n)', 'O(n^2)'),
    ('ALGO1', 3, 'Depth-first search is typically implemented using:', 'A stack (or recursion)', 'A queue', 'A heap', 'A hash table'),
    ('ALGO1', 4, 'Dynamic programming is most useful when subproblems are:', 'Overlapping', 'Independent', 'Random', 'Unordered'),

    ('DBMS1', 1, 'In ACID properties, the A stands for:', 'Atomicity', 'Availability', 'Accessibility', 'Adaptability'),
    ('DBMS1', 2, 'Which normal form removes transitive dependencies?', 'Third Normal Form (3NF)', 'First Normal Form (1NF)', 'Boyce-Codd Normal Form only', 'Zero Normal Form'),
    ('DBMS1', 3, 'Which SQL clause is used to aggregate rows into groups?', 'GROUP BY', 'ORDER BY', 'WHERE', 'LIMIT'),
    ('DBMS1', 4, 'Which index type is commonly used for fast equality and range lookups?', 'B-tree', 'Bitmap only', 'Trie only', 'Linked List')
ON DUPLICATE KEY UPDATE
    question = VALUES(question),
    option1 = VALUES(option1),
    option2 = VALUES(option2),
    option3 = VALUES(option3),
    option4 = VALUES(option4);

DELETE FROM quizquestions WHERE quizcode IN ('CS101', 'ALGO1', 'DBMS1');
UPDATE userQuestions SET total = 0 WHERE quizcode IN ('CS101', 'ALGO1', 'DBMS1');
