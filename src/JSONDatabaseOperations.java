import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JSON-based database implementation for Quiz System.
 * Provides complete fallback when SQL is not available.
 * All data is stored in a single JSON file: quiz_database.json
 */
public class JSONDatabaseOperations implements DbOperations {

	private static final String DB_FILE = "quiz_database.json";
	private static final String HASH_PREFIX = "sha256$";
	private static final String QUIZ_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	private JSONObject database;
	private long nextActorId = 1;
	private long nextResponseId = 1;

	public JSONDatabaseOperations() throws IOException {
		loadOrInitializeDatabase();
	}

	private void loadOrInitializeDatabase() throws IOException {
		File dbFile = new File(DB_FILE);
		if (dbFile.exists() && dbFile.length() > 0) {
			try (FileReader reader = new FileReader(dbFile, StandardCharsets.UTF_8)) {
				StringBuilder content = new StringBuilder();
				char[] buffer = new char[1024];
				int bytesRead;
				while ((bytesRead = reader.read(buffer)) != -1) {
					content.append(buffer, 0, bytesRead);
				}
				database = new JSONObject(content.toString());
			}
		} else {
			// Initialize fresh database
			database = new JSONObject();
			database.put("actors", new JSONArray());
			database.put("quizzes", new JSONArray());
			database.put("responses", new JSONArray());
			saveDatabase();
		}

		// Calculate next IDs
		calculateNextIds();
	}

	private void calculateNextIds() {
		nextActorId = 1;
		JSONArray actors = database.getJSONArray("actors");
		for (int i = 0; i < actors.length(); i++) {
			long id = actors.getJSONObject(i).getLong("id");
			if (id >= nextActorId) {
				nextActorId = id + 1;
			}
		}

		nextResponseId = 1;
		JSONArray responses = database.getJSONArray("responses");
		for (int i = 0; i < responses.length(); i++) {
			long id = responses.getJSONObject(i).getLong("response_id");
			if (id >= nextResponseId) {
				nextResponseId = id + 1;
			}
		}
	}

	private void saveDatabase() throws IOException {
		try (FileWriter writer = new FileWriter(DB_FILE, StandardCharsets.UTF_8)) {
			writer.write(database.toString(2));
		}
	}

	public void newUser(String name, String uname, String pass) throws IOException {
		requireNotBlank(name, "Full name");
		requireNotBlank(uname, "Username");
		requireNotBlank(pass, "Password");

		if (checkUsername(uname)) {
			throw new IllegalArgumentException("Username already exists.");
		}

		JSONObject actor = new JSONObject();
		actor.put("id", nextActorId);
		actor.put("fname", clean(name));
		actor.put("uname", clean(uname));
		actor.put("pass", hashPassword(pass));

		database.getJSONArray("actors").put(actor);
		nextActorId++;
		saveDatabase();
	}

	public int authUser(String uname, String pass) throws IOException {
		String cleanedUname = clean(uname);
		JSONArray actors = database.getJSONArray("actors");
		for (int i = 0; i < actors.length(); i++) {
			JSONObject actor = actors.getJSONObject(i);
			if (actor.getString("uname").equals(cleanedUname)) {
				String storedPassword = actor.getString("pass");
				return passwordMatches(pass, storedPassword) ? (int) actor.getLong("id") : 0;
			}
		}
		return -1;
	}

	public boolean validateUserPassword(int id, String password) throws IOException {
		JSONArray actors = database.getJSONArray("actors");
		for (int i = 0; i < actors.length(); i++) {
			JSONObject actor = actors.getJSONObject(i);
			if (actor.getLong("id") == id) {
				String storedPassword = actor.getString("pass");
				return passwordMatches(password, storedPassword);
			}
		}
		return false;
	}

	public boolean checkUsername(String uname) throws IOException {
		String cleanedUname = clean(uname);
		JSONArray actors = database.getJSONArray("actors");
		for (int i = 0; i < actors.length(); i++) {
			if (actors.getJSONObject(i).getString("uname").equals(cleanedUname)) {
				return true;
			}
		}
		return false;
	}

	public void changePassword(int id, String newPass) throws IOException {
		requireNotBlank(newPass, "Password");

		JSONArray actors = database.getJSONArray("actors");
		for (int i = 0; i < actors.length(); i++) {
			JSONObject actor = actors.getJSONObject(i);
			if (actor.getLong("id") == id) {
				actor.put("pass", hashPassword(newPass));
				saveDatabase();
				return;
			}
		}
	}

	public String getUsername(int id) throws IOException {
		JSONArray actors = database.getJSONArray("actors");
		for (int i = 0; i < actors.length(); i++) {
			JSONObject actor = actors.getJSONObject(i);
			if (actor.getLong("id") == id) {
				return actor.getString("uname");
			}
		}
		return null;
	}

	public boolean check(String quizCode) throws IOException {
		String normalized = clean(quizCode).toUpperCase();
		JSONArray quizzes = database.getJSONArray("quizzes");
		for (int i = 0; i < quizzes.length(); i++) {
			if (quizzes.getJSONObject(i).getString("quizcode").equals(normalized)) {
				return true;
			}
		}
		return false;
	}

	public String createQuiz(int userId, List<QuizQuestion> questions) throws IOException {
		if (questions == null || questions.isEmpty()) {
			throw new IllegalArgumentException("At least one question is required.");
		}

		String quizCode = generateUniqueCode();
		JSONObject quiz = new JSONObject();
		quiz.put("id", userId);
		quiz.put("quizcode", quizCode);
		quiz.put("total", 0);

		JSONArray questionsArray = new JSONArray();
		for (int i = 0; i < questions.size(); i++) {
			QuizQuestion q = questions.get(i);
			JSONObject qObj = new JSONObject();
			qObj.put("qno", i + 1);
			qObj.put("question", q.getPrompt());
			qObj.put("option1", q.getOption(0));
			qObj.put("option2", q.getOption(1));
			qObj.put("option3", q.getOption(2));
			qObj.put("option4", q.getOption(3));
			questionsArray.put(qObj);
		}
		quiz.put("questions", questionsArray);

		database.getJSONArray("quizzes").put(quiz);
		saveDatabase();

		return quizCode;
	}

	public List<QuizSummary> getQuizSummaries(int userId, String searchText) throws IOException {
		List<QuizSummary> summaries = new ArrayList<>();
		String searchUpper = clean(searchText).toUpperCase();

		JSONArray quizzes = database.getJSONArray("quizzes");
		for (int i = 0; i < quizzes.length(); i++) {
			JSONObject quiz = quizzes.getJSONObject(i);
			if (quiz.getLong("id") == userId) {
				String code = quiz.getString("quizcode");
				if (code.contains(searchUpper)) {
					summaries.add(new QuizSummary(code, (int) quiz.getLong("total")));
				}
			}
		}

		summaries.sort(Comparator.comparing(QuizSummary::getCode));
		return summaries;
	}

	public List<QuizQuestion> getQuizQuestions(String quizCode) throws IOException {
		List<QuizQuestion> questions = new ArrayList<>();
		String normalized = clean(quizCode).toUpperCase();

		JSONArray quizzes = database.getJSONArray("quizzes");
		for (int i = 0; i < quizzes.length(); i++) {
			JSONObject quiz = quizzes.getJSONObject(i);
			if (quiz.getString("quizcode").equals(normalized)) {
				JSONArray questionsArray = quiz.getJSONArray("questions");
				for (int j = 0; j < questionsArray.length(); j++) {
					JSONObject q = questionsArray.getJSONObject(j);
					questions.add(new QuizQuestion(
						q.getString("question"),
						q.getString("option1"),
						q.getString("option2"),
						q.getString("option3"),
						q.getString("option4")
					));
				}
				break;
			}
		}

		return questions;
	}

	public int[] getVoteCounts(String quizCode, int questionNumber) throws IOException {
		int[] counts = new int[4];
		String normalized = clean(quizCode).toUpperCase();

		JSONArray responses = database.getJSONArray("responses");
		for (int i = 0; i < responses.length(); i++) {
			JSONObject response = responses.getJSONObject(i);
			if (response.getString("quizcode").equals(normalized)) {
				JSONArray answers = response.getJSONArray("answers");
				if (questionNumber - 1 < answers.length()) {
					int selectedOption = answers.getInt(questionNumber - 1);
					if (selectedOption >= 1 && selectedOption <= 4) {
						counts[selectedOption - 1]++;
					}
				}
			}
		}

		return counts;
	}

	public void submitQuizResponses(String quizCode, List<Integer> answers) throws IOException {
		if (answers == null || answers.isEmpty()) {
			throw new IllegalArgumentException("Answers are required.");
		}

		for (Integer answer : answers) {
			int selectedOption = answer == null ? 0 : answer.intValue();
			if (selectedOption < 1 || selectedOption > 4) {
				throw new IllegalArgumentException("Each answer must choose one of the four options.");
			}
		}

		String normalized = clean(quizCode).toUpperCase();

		// Create response record
		JSONObject response = new JSONObject();
		response.put("response_id", nextResponseId);
		response.put("quizcode", normalized);
		response.put("respondent_label", "Anonymous");
		response.put("submitted_at", LocalDateTime.now().format(DATE_FORMATTER));

		JSONArray answersArray = new JSONArray();
		for (Integer answer : answers) {
			answersArray.put(answer.intValue());
		}
		response.put("answers", answersArray);

		database.getJSONArray("responses").put(response);
		nextResponseId++;

		// Update quiz total
		JSONArray quizzes = database.getJSONArray("quizzes");
		for (int i = 0; i < quizzes.length(); i++) {
			JSONObject quiz = quizzes.getJSONObject(i);
			if (quiz.getString("quizcode").equals(normalized)) {
				quiz.put("total", quiz.getLong("total") + 1);
				break;
			}
		}

		saveDatabase();
	}

	public List<QuizResponseRecord> getQuizResponseRecords(String quizCode, int questionCount) throws IOException {
		Map<Long, QuizResponseRecord> recordsMap = new LinkedHashMap<>();
		String normalized = clean(quizCode).toUpperCase();

		JSONArray responses = database.getJSONArray("responses");
		List<JSONObject> relevantResponses = new ArrayList<>();

		for (int i = 0; i < responses.length(); i++) {
			JSONObject response = responses.getJSONObject(i);
			if (response.getString("quizcode").equals(normalized)) {
				relevantResponses.add(response);
			}
		}

		// Sort by submitted_at descending, then response_id descending
		relevantResponses.sort((a, b) -> {
			String timeA = a.getString("submitted_at");
			String timeB = b.getString("submitted_at");
			int timeCompare = timeB.compareTo(timeA);
			if (timeCompare != 0) return timeCompare;
			return Long.compare(b.getLong("response_id"), a.getLong("response_id"));
		});

		// Create records and populate answers
		for (JSONObject response : relevantResponses) {
			long responseId = response.getLong("response_id");
			String label = response.getString("respondent_label");
			LocalDateTime submittedAt = LocalDateTime.parse(
				response.getString("submitted_at"),
				DATE_FORMATTER
			);

			QuizResponseRecord record = new QuizResponseRecord(responseId, label, submittedAt, questionCount);

			JSONArray answersArray = response.getJSONArray("answers");
			for (int j = 0; j < answersArray.length(); j++) {
				record.setAnswer(j, answersArray.getInt(j));
			}

			recordsMap.put(responseId, record);
		}

		return new ArrayList<>(recordsMap.values());
	}

	public boolean hasLegacyQuizResponses(String quizCode) throws IOException {
		// JSON implementation doesn't have legacy responses
		return false;
	}

	public void removeSurvey(String quizCode) throws IOException {
		String normalized = clean(quizCode).toUpperCase();

		// Remove quiz
		JSONArray quizzes = database.getJSONArray("quizzes");
		for (int i = quizzes.length() - 1; i >= 0; i--) {
			if (quizzes.getJSONObject(i).getString("quizcode").equals(normalized)) {
				quizzes.remove(i);
				break;
			}
		}

		// Remove responses
		JSONArray responses = database.getJSONArray("responses");
		for (int i = responses.length() - 1; i >= 0; i--) {
			if (responses.getJSONObject(i).getString("quizcode").equals(normalized)) {
				responses.remove(i);
			}
		}

		saveDatabase();
	}

	public int getTotal(String quizCode) throws IOException {
		String normalized = clean(quizCode).toUpperCase();
		JSONArray quizzes = database.getJSONArray("quizzes");
		for (int i = 0; i < quizzes.length(); i++) {
			JSONObject quiz = quizzes.getJSONObject(i);
			if (quiz.getString("quizcode").equals(normalized)) {
				return (int) quiz.getLong("total");
			}
		}
		return 0;
	}

	public String generateUniqueCode() throws IOException {
		String code;
		do {
			code = stringGenerator();
		} while (check(code));
		return code;
	}

	public String stringGenerator() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			int position = ThreadLocalRandom.current().nextInt(QUIZ_CODE_ALPHABET.length());
			builder.append(QUIZ_CODE_ALPHABET.charAt(position));
		}
		return builder.toString();
	}

	@Override
	public void close() throws IOException {
		saveDatabase();
	}

	private static String clean(String value) {
		return value == null ? "" : value.trim();
	}

	private static void requireNotBlank(String value, String fieldName) {
		if (clean(value).isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
	}

	private static boolean passwordMatches(String plainPassword, String storedPassword) {
		String safeInput = plainPassword == null ? "" : plainPassword;
		String safeStored = storedPassword == null ? "" : storedPassword;
		if (safeStored.startsWith(HASH_PREFIX)) {
			return safeStored.equals(hashPassword(safeInput));
		}
		return safeStored.equals(safeInput);
	}

	private static String hashPassword(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(HASH_PREFIX);
			for (byte hashByte : hash) {
				builder.append(String.format("%02x", hashByte));
			}
			return builder.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 hashing is not available.", ex);
		}
	}
}
