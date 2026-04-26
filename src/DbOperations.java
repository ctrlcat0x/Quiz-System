import java.util.List;

public interface DbOperations extends AutoCloseable {
	void newUser(String name, String uname, String pass) throws Exception;
	int authUser(String uname, String pass) throws Exception;
	boolean validateUserPassword(int id, String password) throws Exception;
	boolean checkUsername(String uname) throws Exception;
	void changePassword(int id, String newPass) throws Exception;
	String getUsername(int id) throws Exception;
	boolean check(String quizCode) throws Exception;
	String createQuiz(int userId, List<QuizQuestion> questions) throws Exception;
	List<QuizSummary> getQuizSummaries(int userId, String searchText) throws Exception;
	List<QuizQuestion> getQuizQuestions(String quizCode) throws Exception;
	int[] getVoteCounts(String quizCode, int questionNumber) throws Exception;
	void submitQuizResponses(String quizCode, List<Integer> answers) throws Exception;
	List<QuizResponseRecord> getQuizResponseRecords(String quizCode, int questionCount) throws Exception;
	boolean hasLegacyQuizResponses(String quizCode) throws Exception;
	void removeSurvey(String quizCode) throws Exception;
	void close() throws Exception;
}
