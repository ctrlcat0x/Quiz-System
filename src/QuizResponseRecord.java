import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class QuizResponseRecord {

	private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final long responseId;
	private final String respondentLabel;
	private final LocalDateTime submittedAt;
	private final int[] answers;

	public QuizResponseRecord(long responseId, String respondentLabel, LocalDateTime submittedAt, int questionCount) {
		this.responseId = responseId;
		this.respondentLabel = respondentLabel == null || respondentLabel.trim().isEmpty() ? "Anonymous" : respondentLabel.trim();
		this.submittedAt = submittedAt;
		this.answers = new int[Math.max(questionCount, 0)];
	}

	public long getResponseId() {
		return responseId;
	}

	public String getRespondentLabel() {
		return respondentLabel;
	}

	public String getSubmittedAtDisplay() {
		return submittedAt == null ? "Unknown time" : submittedAt.format(DISPLAY_FORMAT);
	}

	public void setAnswer(int questionIndex, int selectedOption) {
		if (questionIndex < 0 || questionIndex >= answers.length) {
			return;
		}

		answers[questionIndex] = selectedOption;
	}

	public int getAnswer(int questionIndex) {
		if (questionIndex < 0 || questionIndex >= answers.length) {
			return 0;
		}

		return answers[questionIndex];
	}

	public int getQuestionCount() {
		return answers.length;
	}

	public int[] getAnswers() {
		return Arrays.copyOf(answers, answers.length);
	}
}