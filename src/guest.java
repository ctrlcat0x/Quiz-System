import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class guest {

	private JFrame frame;
	private JLabel progressLabel;
	private JTextArea questionArea;
	private JRadioButton[] optionButtons;
	private JPanel[] optionPanels;
	private ButtonGroup buttonGroup;
	private JButton previousButton;
	private JButton nextButton;
	private List<QuizQuestion> questions;
	private int[] answers;
	private int currentQuestionIndex;
	private String surveyCode;

	public void guestView(String surveyCode) {
		this.surveyCode = surveyCode == null ? "" : surveyCode.trim().toUpperCase();

		try (SQLoperations manage = new SQLoperations()) {
			questions = manage.getQuizQuestions(this.surveyCode);
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null,
				"Unable to load that quiz right now.\n\n" + ex.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (questions.isEmpty()) {
			JOptionPane.showMessageDialog(null, "That quiz does not contain any questions.", "Empty Quiz", JOptionPane.WARNING_MESSAGE);
			return;
		}

		answers = new int[questions.size()];
		currentQuestionIndex = 0;

		frame = AppTheme.createFrame("Complete Quiz", 760, 560, JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);
		frame.setLayout(new BorderLayout(20, 20));
		frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

		JPanel header = new JPanel(new BorderLayout(0, 8));
		header.setOpaque(false);
		JLabel title = AppTheme.createTitleLabel("Guest Quiz");
		header.add(title, BorderLayout.NORTH);
		progressLabel = AppTheme.createMutedLabel("");
		header.add(progressLabel, BorderLayout.SOUTH);

		JPanel card = AppTheme.createCard(new BorderLayout(0, 22), 24);
		questionArea = AppTheme.createTextArea(4, 20, false);
		questionArea.setFont(AppTheme.SECTION_FONT);
		questionArea.setBackground(AppTheme.SURFACE);
		questionArea.setPreferredSize(new Dimension(620, 100));
		card.add(new JScrollPane(questionArea), BorderLayout.NORTH);

		JPanel optionsPanel = new JPanel();
		optionsPanel.setOpaque(false);
		optionsPanel.setLayout(new javax.swing.BoxLayout(optionsPanel, javax.swing.BoxLayout.Y_AXIS));

		buttonGroup = new ButtonGroup();
		optionButtons = new JRadioButton[4];
		optionPanels = new JPanel[4];
		for (int index = 0; index < optionButtons.length; index++) {
			optionButtons[index] = new JRadioButton();
			optionButtons[index].setFont(AppTheme.BODY_FONT);
			optionButtons[index].setOpaque(false);
			optionButtons[index].setFocusPainted(false);
			optionButtons[index].setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

			JPanel optionRow = new JPanel(new java.awt.BorderLayout());
			optionRow.setBackground(AppTheme.SURFACE);
			optionRow.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(AppTheme.BORDER, 1),
				BorderFactory.createEmptyBorder(10, 14, 10, 14)
			));
			optionRow.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 48));
			optionRow.add(optionButtons[index], java.awt.BorderLayout.CENTER);
			optionPanels[index] = optionRow;

			final int capturedIndex = index;
			optionButtons[index].addActionListener(e -> highlightSelectedOption(capturedIndex));

			buttonGroup.add(optionButtons[index]);
			optionsPanel.add(optionRow);
			if (index < 3) {
				optionsPanel.add(javax.swing.Box.createVerticalStrut(6));
			}
		}
		card.add(optionsPanel, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		actions.setOpaque(false);
		previousButton = AppTheme.createSecondaryButton("Previous");
		JButton cancelButton = AppTheme.createSecondaryButton("Cancel");
		nextButton = AppTheme.createPrimaryButton("Next Question");
		actions.add(previousButton);
		actions.add(cancelButton);
		actions.add(nextButton);
		card.add(actions, BorderLayout.SOUTH);

		previousButton.addActionListener(e -> goToPreviousQuestion());
		cancelButton.addActionListener(e -> frame.dispose());
		nextButton.addActionListener(e -> goToNextQuestion());

		frame.add(header, BorderLayout.NORTH);
		frame.add(card, BorderLayout.CENTER);

		showQuestion();
		frame.setVisible(true);
	}

	private void goToPreviousQuestion() {
		persistSelection(false);
		if (currentQuestionIndex > 0) {
			currentQuestionIndex--;
			showQuestion();
		}
	}

	private void goToNextQuestion() {
		if (!persistSelection(true)) {
			return;
		}

		if (currentQuestionIndex == questions.size() - 1) {
			submitQuiz();
			return;
		}

		currentQuestionIndex++;
		showQuestion();
	}

	private boolean persistSelection(boolean requireSelection) {
		int choice = getSelectedOption();
		if (choice == 0 && requireSelection) {
			JOptionPane.showMessageDialog(frame, "Choose one answer before continuing.", "Select an Answer", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (choice != 0) {
			answers[currentQuestionIndex] = choice;
		}
		return true;
	}

	private int getSelectedOption() {
		for (int index = 0; index < optionButtons.length; index++) {
			if (optionButtons[index].isSelected()) {
				return index + 1;
			}
		}
		return 0;
	}

	private void showQuestion() {
		QuizQuestion question = questions.get(currentQuestionIndex);
		progressLabel.setText("Quiz code " + surveyCode + "  •  Question " + (currentQuestionIndex + 1) + " of " + questions.size());
		questionArea.setText(question.getPrompt());
		questionArea.setCaretPosition(0);

		buttonGroup.clearSelection();
		for (int index = 0; index < optionButtons.length; index++) {
			optionButtons[index].setText(question.getOption(index));
			optionPanels[index].setBackground(AppTheme.SURFACE);
			optionPanels[index].setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(AppTheme.BORDER, 1),
				BorderFactory.createEmptyBorder(10, 14, 10, 14)
			));
			if (answers[currentQuestionIndex] == index + 1) {
				optionButtons[index].setSelected(true);
				highlightSelectedOption(index);
			}
		}

		previousButton.setEnabled(currentQuestionIndex > 0);
		nextButton.setText(currentQuestionIndex == questions.size() - 1 ? "Submit Quiz" : "Next Question");
	}

	private void highlightSelectedOption(int selectedIndex) {
		for (int index = 0; index < optionPanels.length; index++) {
			boolean active = index == selectedIndex;
			optionPanels[index].setBackground(active ? AppTheme.PRIMARY_SOFT : AppTheme.SURFACE);
			optionPanels[index].setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(active ? AppTheme.PRIMARY : AppTheme.BORDER, 1),
				BorderFactory.createEmptyBorder(10, 14, 10, 14)
			));
		}
	}

	private void submitQuiz() {
		List<Integer> submittedAnswers = new ArrayList<Integer>();
		for (int answer : answers) {
			submittedAnswers.add(Integer.valueOf(answer));
		}

		try (SQLoperations manage = new SQLoperations()) {
			manage.submitQuizResponses(surveyCode, submittedAnswers);
			JOptionPane.showMessageDialog(frame, "Quiz completed. Thanks for responding.", "Submitted", JOptionPane.INFORMATION_MESSAGE);
			frame.dispose();
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(frame,
				"Unable to submit your answers right now.\n\n" + ex.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}
}