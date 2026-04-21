import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SQLoperations implements AutoCloseable {

	private static final String DB_URL = "jdbc:mysql://localhost:3306/survey?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
	private static final String DB_USER = "root";
	private static final String DB_PASS = "";
	private static final String HASH_PREFIX = "sha256$";
	private static final String QUIZ_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

	private final Connection con;

	public SQLoperations() throws SQLException {
		con = DriverManager.getConnection(
			resolveSetting("quiz.db.url", "QUIZ_DB_URL", DB_URL),
			resolveSetting("quiz.db.user", "QUIZ_DB_USER", DB_USER),
			resolveSetting("quiz.db.pass", "QUIZ_DB_PASS", DB_PASS)
		);
	}

	public void newUser(String name, String uname, String pass) throws SQLException {
		requireNotBlank(name, "Full name");
		requireNotBlank(uname, "Username");
		requireNotBlank(pass, "Password");

		String sql = "INSERT INTO actors(fname, uname, pass) VALUES (?, ?, ?)";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, clean(name));
			pst.setString(2, clean(uname));
			pst.setString(3, hashPassword(pass));
			pst.executeUpdate();
		}
	}

	public int authUser(String uname, String pass) throws SQLException {
		String sql = "SELECT id, pass FROM actors WHERE uname = ?";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, clean(uname));
			try (ResultSet rst = pst.executeQuery()) {
				if (!rst.next()) {
					return -1;
				}

				String storedPassword = rst.getString("pass");
				return passwordMatches(pass, storedPassword) ? rst.getInt("id") : 0;
			}
		}
	}

	public boolean validateUserPassword(int id, String password) throws SQLException {
		String sql = "SELECT pass FROM actors WHERE id = ?";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setInt(1, id);
			try (ResultSet rst = pst.executeQuery()) {
				return rst.next() && passwordMatches(password, rst.getString("pass"));
			}
		}
	}

	public boolean checkUsername(String uname) throws SQLException {
		String sql = "SELECT id FROM actors WHERE uname = ?";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, clean(uname));
			try (ResultSet rst = pst.executeQuery()) {
				return rst.next();
			}
		}
	}

	public void changePassword(int id, String newPass) throws SQLException {
		requireNotBlank(newPass, "Password");

		String sql = "UPDATE actors SET pass = ? WHERE id = ?";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, hashPassword(newPass));
			pst.setInt(2, id);
			pst.executeUpdate();
		}
	}

	public String getUsername(int id) throws SQLException {
		String sql = "SELECT uname FROM actors WHERE id = ?";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setInt(1, id);
			try (ResultSet rst = pst.executeQuery()) {
				return rst.next() ? rst.getString("uname") : null;
			}
		}
	}

	public boolean check(String quizCode) throws SQLException {
		String sql = "SELECT quizcode FROM userQuestions WHERE quizcode = ?";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, clean(quizCode).toUpperCase());
			try (ResultSet rst = pst.executeQuery()) {
				return rst.next();
			}
		}
	}

	public String createQuiz(int userId, List<QuizQuestion> questions) throws SQLException {
		if (questions == null || questions.isEmpty()) {
			throw new IllegalArgumentException("At least one question is required.");
		}

		boolean originalAutoCommit = con.getAutoCommit();
		con.setAutoCommit(false);
		String quizCode = generateUniqueCode();

		try (PreparedStatement addQuiz = con.prepareStatement("INSERT INTO userQuestions(id, quizcode, total) VALUES (?, ?, 0)");
			 PreparedStatement addQuestion = con.prepareStatement(
				 "INSERT INTO questions(quizcode, qno, question, option1, option2, option3, option4) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

			addQuiz.setInt(1, userId);
			addQuiz.setString(2, quizCode);
			addQuiz.executeUpdate();

			for (int index = 0; index < questions.size(); index++) {
				QuizQuestion question = questions.get(index);
				addQuestion.setString(1, quizCode);
				addQuestion.setInt(2, index + 1);
				addQuestion.setString(3, question.getPrompt());
				addQuestion.setString(4, question.getOption(0));
				addQuestion.setString(5, question.getOption(1));
				addQuestion.setString(6, question.getOption(2));
				addQuestion.setString(7, question.getOption(3));
				addQuestion.addBatch();
			}

			addQuestion.executeBatch();
			con.commit();
			return quizCode;
		} catch (SQLException ex) {
			con.rollback();
			throw ex;
		} finally {
			con.setAutoCommit(originalAutoCommit);
		}
	}

	public List<QuizSummary> getQuizSummaries(int userId, String searchText) throws SQLException {
		List<QuizSummary> quizSummaries = new ArrayList<QuizSummary>();
		String sql = "SELECT quizcode, total FROM userQuestions WHERE id = ? AND quizcode LIKE ? ORDER BY quizcode ASC";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setInt(1, userId);
			pst.setString(2, "%" + clean(searchText).toUpperCase() + "%");
			try (ResultSet rst = pst.executeQuery()) {
				while (rst.next()) {
					quizSummaries.add(new QuizSummary(rst.getString("quizcode"), rst.getInt("total")));
				}
			}
		}
		return quizSummaries;
	}

	public List<QuizQuestion> getQuizQuestions(String quizCode) throws SQLException {
		List<QuizQuestion> questions = new ArrayList<QuizQuestion>();
		String sql = "SELECT question, option1, option2, option3, option4 FROM questions WHERE quizcode = ? ORDER BY qno ASC";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, clean(quizCode).toUpperCase());
			try (ResultSet rst = pst.executeQuery()) {
				while (rst.next()) {
					questions.add(new QuizQuestion(
						rst.getString("question"),
						rst.getString("option1"),
						rst.getString("option2"),
						rst.getString("option3"),
						rst.getString("option4")
					));
				}
			}
		}
		return questions;
	}

	public int[] getVoteCounts(String quizCode, int questionNumber) throws SQLException {
		int[] counts = new int[4];
		String sql = "SELECT opno, COUNT(*) AS cnt FROM quizquestions WHERE quizcode = ? AND qno = ? GROUP BY opno";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, clean(quizCode).toUpperCase());
			pst.setInt(2, questionNumber);
			try (ResultSet rst = pst.executeQuery()) {
				while (rst.next()) {
					int optionNumber = rst.getInt("opno");
					if (optionNumber >= 1 && optionNumber <= 4) {
						counts[optionNumber - 1] = rst.getInt("cnt");
					}
				}
			}
		}
		return counts;
	}

	public void submitQuizResponses(String quizCode, List<Integer> answers) throws SQLException {
		if (answers == null || answers.isEmpty()) {
			throw new IllegalArgumentException("Answers are required.");
		}

		boolean originalAutoCommit = con.getAutoCommit();
		con.setAutoCommit(false);
		String normalizedCode = clean(quizCode).toUpperCase();

		try (PreparedStatement insertAnswer = con.prepareStatement("INSERT INTO quizquestions(quizcode, qno, opno) VALUES (?, ?, ?)");
			 PreparedStatement updateTotal = con.prepareStatement("UPDATE userQuestions SET total = total + 1 WHERE quizcode = ?")) {

			for (int index = 0; index < answers.size(); index++) {
				insertAnswer.setString(1, normalizedCode);
				insertAnswer.setInt(2, index + 1);
				insertAnswer.setInt(3, answers.get(index).intValue());
				insertAnswer.addBatch();
			}

			insertAnswer.executeBatch();
			updateTotal.setString(1, normalizedCode);
			updateTotal.executeUpdate();
			con.commit();
		} catch (SQLException ex) {
			con.rollback();
			throw ex;
		} finally {
			con.setAutoCommit(originalAutoCommit);
		}
	}

	public void removeSurvey(String quizCode) throws SQLException {
		boolean originalAutoCommit = con.getAutoCommit();
		con.setAutoCommit(false);
		String normalizedCode = clean(quizCode).toUpperCase();

		try (PreparedStatement deleteAnswers = con.prepareStatement("DELETE FROM quizquestions WHERE quizcode = ?");
			 PreparedStatement deleteQuestions = con.prepareStatement("DELETE FROM questions WHERE quizcode = ?");
			 PreparedStatement deleteQuiz = con.prepareStatement("DELETE FROM userQuestions WHERE quizcode = ?")) {

			deleteAnswers.setString(1, normalizedCode);
			deleteAnswers.executeUpdate();

			deleteQuestions.setString(1, normalizedCode);
			deleteQuestions.executeUpdate();

			deleteQuiz.setString(1, normalizedCode);
			deleteQuiz.executeUpdate();

			con.commit();
		} catch (SQLException ex) {
			con.rollback();
			throw ex;
		} finally {
			con.setAutoCommit(originalAutoCommit);
		}
	}

	public int getTotal(String quizCode) throws SQLException {
		String sql = "SELECT total FROM userQuestions WHERE quizcode = ?";
		try (PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, clean(quizCode).toUpperCase());
			try (ResultSet rst = pst.executeQuery()) {
				return rst.next() ? rst.getInt("total") : 0;
			}
		}
	}

	public String generateUniqueCode() throws SQLException {
		String code;
		do {
			code = stringGenerator();
		} while (check(code));
		return code;
	}

	public String stringGenerator() {
		StringBuilder builder = new StringBuilder();
		for (int index = 0; index < 5; index++) {
			int position = ThreadLocalRandom.current().nextInt(QUIZ_CODE_ALPHABET.length());
			builder.append(QUIZ_CODE_ALPHABET.charAt(position));
		}
		return builder.toString();
	}

	@Override
	public void close() throws SQLException {
		if (!con.isClosed()) {
			con.close();
		}
	}

	private static String clean(String value) {
		return value == null ? "" : value.trim();
	}

	private static void requireNotBlank(String value, String fieldName) {
		if (clean(value).isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
	}

	private static String resolveSetting(String propertyName, String envName, String fallback) {
		String propertyValue = System.getProperty(propertyName);
		if (!clean(propertyValue).isEmpty()) {
			return propertyValue.trim();
		}

		String envValue = System.getenv(envName);
		if (!clean(envValue).isEmpty()) {
			return envValue.trim();
		}

		return fallback;
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