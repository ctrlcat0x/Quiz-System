import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility to generate SQL-like schema and data output from JSON database file.
 * Useful for viewing the structure and contents of the database.
 *
 * Usage: java -cp . generate_sql_schema
 */
public class generate_sql_schema {

	public static void main(String[] args) {
		try {
			File dbFile = new File("quiz_database.json");
			if (!dbFile.exists()) {
				System.err.println("Error: quiz_database.json not found");
				System.err.println("Please run the application first to create the database.");
				System.exit(1);
			}

			// Load JSON database
			String content = readFile(dbFile);
			JSONObject database = new JSONObject(content);

			System.out.println("========================================");
			System.out.println("       QUIZ SYSTEM DATABASE SCHEMA      ");
			System.out.println("========================================\n");

			// Generate schema for actors table
			generateActorsSchema(database);

			System.out.println("\n----------------------------------------\n");

			// Generate schema for userQuestions table (quizzes)
			generateQuizzesSchema(database);

			System.out.println("\n----------------------------------------\n");

			// Generate schema for questions table
			generateQuestionsSchema(database);

			System.out.println("\n----------------------------------------\n");

			// Generate schema for quizquestions table (responses)
			generateResponsesSchema(database);

			System.out.println("\n========================================");
			System.out.println("          DATA SUMMARY                  ");
			System.out.println("========================================\n");

			// Print data summary
			printDataSummary(database);

		} catch (Exception ex) {
			System.err.println("Error reading database: " + ex.getMessage());
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private static void generateActorsSchema(JSONObject database) {
		System.out.println("-- Table: actors");
		System.out.println("-- Stores user account information\n");
		System.out.println("CREATE TABLE actors (");
		System.out.println("  id INT PRIMARY KEY AUTO_INCREMENT,");
		System.out.println("  fname VARCHAR(255) NOT NULL,");
		System.out.println("  uname VARCHAR(100) UNIQUE NOT NULL,");
		System.out.println("  pass VARCHAR(255) NOT NULL");
		System.out.println(");\n");

		JSONArray actors = database.optJSONArray("actors");
		if (actors != null && actors.length() > 0) {
			System.out.println("-- Sample Data:");
			for (int i = 0; i < actors.length(); i++) {
				JSONObject actor = actors.getJSONObject(i);
				System.out.printf("INSERT INTO actors (id, fname, uname, pass) VALUES (%d, '%s', '%s', '%s');%n",
					actor.getInt("id"),
					escapeSql(actor.getString("fname")),
					escapeSql(actor.getString("uname")),
					escapeSql(actor.getString("pass")));
			}
			System.out.printf("\n-- Total Records: %d\n", actors.length());
		}
	}

	private static void generateQuizzesSchema(JSONObject database) {
		System.out.println("-- Table: userQuestions");
		System.out.println("-- Stores quiz metadata\n");
		System.out.println("CREATE TABLE userQuestions (");
		System.out.println("  id INT NOT NULL,");
		System.out.println("  quizcode CHAR(5) PRIMARY KEY,");
		System.out.println("  total INT DEFAULT 0,");
		System.out.println("  FOREIGN KEY (id) REFERENCES actors(id)");
		System.out.println(");\n");

		JSONArray quizzes = database.optJSONArray("quizzes");
		if (quizzes != null && quizzes.length() > 0) {
			System.out.println("-- Sample Data:");
			for (int i = 0; i < quizzes.length(); i++) {
				JSONObject quiz = quizzes.getJSONObject(i);
				System.out.printf("INSERT INTO userQuestions (id, quizcode, total) VALUES (%d, '%s', %d);%n",
					quiz.getInt("id"),
					quiz.getString("quizcode"),
					quiz.getInt("total"));
			}
			System.out.printf("\n-- Total Records: %d\n", quizzes.length());
		}
	}

	private static void generateQuestionsSchema(JSONObject database) {
		System.out.println("-- Table: questions");
		System.out.println("-- Stores quiz questions and options\n");
		System.out.println("CREATE TABLE questions (");
		System.out.println("  quizcode CHAR(5) NOT NULL,");
		System.out.println("  qno INT NOT NULL,");
		System.out.println("  question TEXT NOT NULL,");
		System.out.println("  option1 VARCHAR(255) NOT NULL,");
		System.out.println("  option2 VARCHAR(255) NOT NULL,");
		System.out.println("  option3 VARCHAR(255) NOT NULL,");
		System.out.println("  option4 VARCHAR(255) NOT NULL,");
		System.out.println("  PRIMARY KEY (quizcode, qno),");
		System.out.println("  FOREIGN KEY (quizcode) REFERENCES userQuestions(quizcode)");
		System.out.println(");\n");

		JSONArray quizzes = database.optJSONArray("quizzes");
		int totalQuestions = 0;
		if (quizzes != null) {
			System.out.println("-- Sample Data:");
			for (int i = 0; i < quizzes.length(); i++) {
				JSONObject quiz = quizzes.getJSONObject(i);
				String quizcode = quiz.getString("quizcode");
				JSONArray questions = quiz.optJSONArray("questions");
				if (questions != null) {
					for (int j = 0; j < questions.length(); j++) {
						JSONObject q = questions.getJSONObject(j);
						System.out.printf(
							"INSERT INTO questions (quizcode, qno, question, option1, option2, option3, option4) " +
							"VALUES ('%s', %d, '%s', '%s', '%s', '%s', '%s');%n",
							quizcode,
							q.getInt("qno"),
							escapeSql(q.getString("question")),
							escapeSql(q.getString("option1")),
							escapeSql(q.getString("option2")),
							escapeSql(q.getString("option3")),
							escapeSql(q.getString("option4")));
						totalQuestions++;
					}
				}
			}
			if (totalQuestions > 0) {
				System.out.printf("\n-- Total Records: %d\n", totalQuestions);
			}
		}
	}

	private static void generateResponsesSchema(JSONObject database) {
		System.out.println("-- Table: quizquestions (responses)");
		System.out.println("-- Stores quiz responses and selected answers\n");
		System.out.println("CREATE TABLE quizquestions (");
		System.out.println("  response_id BIGINT NOT NULL,");
		System.out.println("  quizcode CHAR(5) NOT NULL,");
		System.out.println("  qno INT NOT NULL,");
		System.out.println("  opno INT NOT NULL,");
		System.out.println("  PRIMARY KEY (response_id, quizcode, qno),");
		System.out.println("  FOREIGN KEY (quizcode, qno) REFERENCES questions(quizcode, qno)");
		System.out.println(");\n");

		JSONArray responses = database.optJSONArray("responses");
		int totalAnswers = 0;
		if (responses != null && responses.length() > 0) {
			System.out.println("-- Sample Data:");
			for (int i = 0; i < responses.length(); i++) {
				JSONObject response = responses.getJSONObject(i);
				long responseId = response.getLong("response_id");
				String quizcode = response.getString("quizcode");
				JSONArray answers = response.optJSONArray("answers");
				if (answers != null) {
					for (int j = 0; j < answers.length(); j++) {
						int opno = answers.getInt(j);
						System.out.printf(
							"INSERT INTO quizquestions (response_id, quizcode, qno, opno) VALUES (%d, '%s', %d, %d);%n",
							responseId,
							quizcode,
							j + 1,
							opno);
						totalAnswers++;
					}
				}
			}
			System.out.printf("\n-- Total Records: %d\n", totalAnswers);
		}
	}

	private static void printDataSummary(JSONObject database) {
		JSONArray actors = database.optJSONArray("actors");
		JSONArray quizzes = database.optJSONArray("quizzes");
		JSONArray responses = database.optJSONArray("responses");

		System.out.printf("User Accounts:        %d%n", actors != null ? actors.length() : 0);
		System.out.printf("Quizzes Created:      %d%n", quizzes != null ? quizzes.length() : 0);
		System.out.printf("Quiz Submissions:     %d%n", responses != null ? responses.length() : 0);

		int totalQuestions = 0;
		int totalAnswers = 0;

		if (quizzes != null) {
			for (int i = 0; i < quizzes.length(); i++) {
				JSONObject quiz = quizzes.getJSONObject(i);
				JSONArray questions = quiz.optJSONArray("questions");
				if (questions != null) {
					totalQuestions += questions.length();
				}
			}
		}

		if (responses != null) {
			for (int i = 0; i < responses.length(); i++) {
				JSONObject response = responses.getJSONObject(i);
				JSONArray answers = response.optJSONArray("answers");
				if (answers != null) {
					totalAnswers += answers.length();
				}
			}
		}

		System.out.printf("Total Questions:     %d%n", totalQuestions);
		System.out.printf("Total Answers:       %d%n", totalAnswers);

		if (quizzes != null) {
			int totalResponses = 0;
			for (int i = 0; i < quizzes.length(); i++) {
				totalResponses += quizzes.getJSONObject(i).getInt("total");
			}
			System.out.printf("Total Responses:     %d%n", totalResponses);
		}

		System.out.println();
	}

	private static String readFile(File file) throws Exception {
		StringBuilder content = new StringBuilder();
		try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
			char[] buffer = new char[1024];
			int bytesRead;
			while ((bytesRead = reader.read(buffer)) != -1) {
				content.append(buffer, 0, bytesRead);
			}
		}
		return content.toString();
	}

	private static String escapeSql(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("'", "''").replace("\\", "\\\\");
	}
}
