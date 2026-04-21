public class QuizSummary {

	private final String code;
	private final int totalResponses;

	public QuizSummary(String code, int totalResponses) {
		this.code = code;
		this.totalResponses = totalResponses;
	}

	public String getCode() {
		return code;
	}

	public int getTotalResponses() {
		return totalResponses;
	}
}