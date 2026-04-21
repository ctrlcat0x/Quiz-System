import java.util.Arrays;

public class QuizQuestion {

	private final String prompt;
	private final String[] options;
	private int[] voteCounts;

	public QuizQuestion(String prompt, String option1, String option2, String option3, String option4) {
		this.prompt = normalize(prompt);
		this.options = new String[] {
			normalize(option1),
			normalize(option2),
			normalize(option3),
			normalize(option4)
		};
		this.voteCounts = new int[4];
	}

	public String getPrompt() {
		return prompt;
	}

	public String getOption(int index) {
		return options[index];
	}

	public String[] getOptions() {
		return Arrays.copyOf(options, options.length);
	}

	public void setVoteCounts(int[] voteCounts) {
		if (voteCounts == null || voteCounts.length != 4) {
			throw new IllegalArgumentException("Vote counts must contain exactly four values.");
		}

		this.voteCounts = Arrays.copyOf(voteCounts, voteCounts.length);
	}

	public int getVoteCount(int index) {
		return voteCounts[index];
	}

	public String getPreviewText() {
		if (prompt.length() <= 48) {
			return prompt;
		}

		return prompt.substring(0, 45) + "...";
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim();
	}
}