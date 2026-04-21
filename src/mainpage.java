import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class mainpage {

	private int id;
	private String username;
	private JFrame frame;
	private CardLayout contentLayout;
	private JPanel contentPanel;
	private JButton addQuizNavButton;
	private JButton viewQuizNavButton;
	private JTextArea questionInput;
	private JTextField[] optionInputs;
	private JLabel questionCountLabel;
	private JLabel draftHintLabel;
	private JList<String> draftList;
	private DefaultListModel<String> draftListModel;
	private JButton removeDraftButton;
	private JButton clearDraftButton;
	private JButton submitQuizButton;
	private JTable quizTable;
	private DefaultTableModel quizTableModel;
	private JTextField searchField;
	private JLabel selectedQuizCodeLabel;
	private JLabel selectedResponsesLabel;
	private JTextArea analyticsQuestionArea;
	private JLabel[] analyticsOptionLabels;
	private JLabel[] analyticsVoteLabels;
	private JButton previousQuestionButton;
	private JButton nextQuestionButton;
	private JButton deleteQuizButton;
	private final List<QuizQuestion> draftQuestions = new ArrayList<QuizQuestion>();
	private List<QuizQuestion> selectedQuizQuestions = new ArrayList<QuizQuestion>();
	private String selectedQuizCode;
	private int selectedQuizResponses;
	private int selectedQuestionIndex;

	public void mainPageView(int id) {
		this.id = id;

		try (SQLoperations manage = new SQLoperations()) {
			username = manage.getUsername(id);
		} catch (SQLException ex) {
			showDatabaseError("load your dashboard", ex);
			return;
		}

		if (username == null) {
			JOptionPane.showMessageDialog(null, "That account could not be loaded.", "Sign In Error", JOptionPane.ERROR_MESSAGE);
			new login().loginView();
			return;
		}

		frame = AppTheme.createFrame("Quiz Dashboard", 1180, 760, JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLayout(new BorderLayout());

		frame.add(createSidebar(), BorderLayout.WEST);

		contentLayout = new CardLayout();
		contentPanel = new JPanel(contentLayout);
		contentPanel.setBackground(AppTheme.BACKGROUND);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
		contentPanel.add(createBuilderView(), "builder");
		contentPanel.add(createLibraryView(), "library");
		frame.add(contentPanel, BorderLayout.CENTER);

		switchView("builder");
		refreshQuizTable(null);
		frame.setVisible(true);
	}

	private JPanel createSidebar() {
		JPanel sidebar = new JPanel();
		sidebar.setPreferredSize(new Dimension(240, 760));
		sidebar.setBackground(AppTheme.SURFACE);
		sidebar.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.BORDER),
			BorderFactory.createEmptyBorder(28, 20, 28, 20)
		));
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

		JLabel brand = new JLabel("Quiz Studio");
		brand.setFont(AppTheme.SECTION_FONT);
		brand.setForeground(AppTheme.TEXT);
		brand.setAlignmentX(Component.LEFT_ALIGNMENT);
		sidebar.add(brand);
		sidebar.add(Box.createVerticalStrut(4));

		JLabel welcome = new JLabel(AppTheme.html("Signed in as <b>" + username + "</b>", 180));
		welcome.setFont(AppTheme.SMALL_FONT);
		welcome.setForeground(AppTheme.TEXT_SECONDARY);
		welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
		sidebar.add(welcome);
		sidebar.add(Box.createVerticalStrut(24));

		addQuizNavButton = AppTheme.createSidebarButton("Add Quiz", true);
		addQuizNavButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		addQuizNavButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		addQuizNavButton.addActionListener(e -> switchView("builder"));
		sidebar.add(addQuizNavButton);
		sidebar.add(Box.createVerticalStrut(4));

		viewQuizNavButton = AppTheme.createSidebarButton("View Quizzes", false);
		viewQuizNavButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		viewQuizNavButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		viewQuizNavButton.addActionListener(e -> {
			refreshQuizTable(selectedQuizCode);
			switchView("library");
		});
		sidebar.add(viewQuizNavButton);
		sidebar.add(Box.createVerticalStrut(4));

		JButton changePasswordButton = AppTheme.createSidebarButton("Change Password", false);
		changePasswordButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		changePasswordButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		changePasswordButton.addActionListener(e -> showChangePasswordDialog());
		sidebar.add(changePasswordButton);
		sidebar.add(Box.createVerticalGlue());

		JButton logoutButton = AppTheme.createDangerButton("Logout");
		logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		logoutButton.addActionListener(e -> {
			new login().loginView();
			frame.dispose();
		});
		sidebar.add(logoutButton);

		return sidebar;
	}

	private JPanel createBuilderView() {
		JPanel page = new JPanel(new BorderLayout(0, 20));
		page.setOpaque(false);

		page.add(createPageHeader("Create a Quiz", "Build questions on the left, review the draft on the right, and publish when it is ready."), BorderLayout.NORTH);

		JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
		body.setOpaque(false);
		body.add(createBuilderFormCard());
		body.add(createDraftPreviewCard());
		page.add(body, BorderLayout.CENTER);

		return page;
	}

	private JPanel createBuilderFormCard() {
		JPanel card = AppTheme.createCard(new BorderLayout(0, 18), 22);

		JPanel header = new JPanel();
		header.setOpaque(false);
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		JLabel title = AppTheme.createSectionLabel("Question Builder");
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(title);
		JLabel subtitle = AppTheme.createMutedLabel("Each quiz can contain up to 50 multiple-choice questions.");
		subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(Box.createVerticalStrut(6));
		header.add(subtitle);
		card.add(header, BorderLayout.NORTH);

		JPanel form = new JPanel();
		form.setOpaque(false);
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

		form.add(createFormLabel("Question"));
		form.add(Box.createVerticalStrut(6));
		questionInput = AppTheme.createTextArea(5, 20, true);
		JScrollPane questionScroll = new JScrollPane(questionInput);
		questionScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
		questionScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
		form.add(questionScroll);
		form.add(Box.createVerticalStrut(16));

		JPanel optionsGrid = new JPanel(new GridLayout(2, 2, 12, 12));
		optionsGrid.setOpaque(false);
		optionInputs = new JTextField[4];
		for (int index = 0; index < optionInputs.length; index++) {
			optionInputs[index] = AppTheme.createTextField(18);
			optionsGrid.add(createOptionFieldPanel("Option " + (index + 1), optionInputs[index]));
		}
		form.add(optionsGrid);

		card.add(form, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		actions.setOpaque(false);
		JButton addQuestionButton = AppTheme.createPrimaryButton("Add Question");
		addQuestionButton.addActionListener(e -> addDraftQuestion());
		actions.add(addQuestionButton);

		JButton clearFormButton = AppTheme.createSecondaryButton("Clear Form");
		clearFormButton.addActionListener(e -> clearQuestionForm());
		actions.add(clearFormButton);

		card.add(actions, BorderLayout.SOUTH);
		return card;
	}

	private JPanel createDraftPreviewCard() {
		JPanel card = AppTheme.createCard(new BorderLayout(0, 18), 22);

		JPanel header = new JPanel(new BorderLayout(0, 8));
		header.setOpaque(false);
		JPanel titleGroup = new JPanel();
		titleGroup.setOpaque(false);
		titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
		questionCountLabel = AppTheme.createSectionLabel("0 questions ready");
		titleGroup.add(questionCountLabel);
		draftHintLabel = AppTheme.createMutedLabel("Add your first question to start the draft.");
		titleGroup.add(Box.createVerticalStrut(6));
		titleGroup.add(draftHintLabel);
		header.add(titleGroup, BorderLayout.CENTER);
		card.add(header, BorderLayout.NORTH);

		draftListModel = new DefaultListModel<String>();
		draftList = new JList<String>(draftListModel);
		draftList.setFont(AppTheme.BODY_FONT);
		draftList.setBackground(AppTheme.SURFACE);
		draftList.setForeground(AppTheme.TEXT);
		draftList.setSelectionBackground(AppTheme.PRIMARY_SOFT);
		draftList.setSelectionForeground(AppTheme.TEXT);
		draftList.setFixedCellHeight(34);
		draftList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		draftList.addListSelectionListener(e -> updateDraftState());
		JScrollPane draftListScroll = new JScrollPane(draftList);
		draftListScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
		card.add(draftListScroll, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		actions.setOpaque(false);
		removeDraftButton = AppTheme.createSecondaryButton("Remove Selected");
		removeDraftButton.addActionListener(e -> removeSelectedDraftQuestion());
		actions.add(removeDraftButton);

		clearDraftButton = AppTheme.createSecondaryButton("Clear Draft");
		clearDraftButton.addActionListener(e -> clearDraft());
		actions.add(clearDraftButton);

		submitQuizButton = AppTheme.createPrimaryButton("Create Quiz");
		submitQuizButton.addActionListener(e -> submitQuiz());
		actions.add(submitQuizButton);

		card.add(actions, BorderLayout.SOUTH);
		updateDraftState();
		return card;
	}

	private JPanel createLibraryView() {
		JPanel page = new JPanel(new BorderLayout(0, 20));
		page.setOpaque(false);
		page.add(createPageHeader("Quiz Library", "Search your quizzes, inspect response counts, and browse question-by-question analytics."), BorderLayout.NORTH);

		JPanel body = new JPanel(new GridLayout(2, 1, 0, 20));
		body.setOpaque(false);
		body.add(createQuizTableCard());
		body.add(createAnalyticsCard());
		page.add(body, BorderLayout.CENTER);

		return page;
	}

	private JPanel createQuizTableCard() {
		JPanel card = AppTheme.createCard(new BorderLayout(0, 16), 22);

		JPanel topBar = new JPanel(new BorderLayout(12, 0));
		topBar.setOpaque(false);
		searchField = AppTheme.createTextField(18);
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				refreshQuizTable(selectedQuizCode);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				refreshQuizTable(selectedQuizCode);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				refreshQuizTable(selectedQuizCode);
			}
		});

		JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
		searchPanel.setOpaque(false);
		searchPanel.add(createFormLabel("Search by quiz code"), BorderLayout.WEST);
		searchPanel.add(searchField, BorderLayout.CENTER);
		topBar.add(searchPanel, BorderLayout.CENTER);

		JButton refreshButton = AppTheme.createSecondaryButton("Refresh");
		refreshButton.addActionListener(e -> refreshQuizTable(selectedQuizCode));
		topBar.add(refreshButton, BorderLayout.EAST);
		card.add(topBar, BorderLayout.NORTH);

		quizTableModel = new DefaultTableModel(new Object[] { "Quiz Code", "Responses" }, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		quizTable = new JTable(quizTableModel);
		quizTable.setRowHeight(34);
		quizTable.setFont(AppTheme.BODY_FONT);
		quizTable.setForeground(AppTheme.TEXT);
		quizTable.setBackground(AppTheme.SURFACE);
		quizTable.setSelectionBackground(AppTheme.PRIMARY_SOFT);
		quizTable.setSelectionForeground(AppTheme.TEXT);
		quizTable.setGridColor(AppTheme.BORDER);
		quizTable.setShowGrid(true);
		quizTable.getTableHeader().setFont(AppTheme.LABEL_FONT);
		quizTable.getTableHeader().setForeground(AppTheme.TEXT_SECONDARY);
		quizTable.getTableHeader().setBackground(AppTheme.SURFACE_ALT);
		quizTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		quizTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				loadSelectedQuizFromTable();
			}
		});

		JScrollPane tableScroll = new JScrollPane(quizTable);
		tableScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
		card.add(tableScroll, BorderLayout.CENTER);
		return card;
	}

	private JPanel createAnalyticsCard() {
		JPanel card = AppTheme.createCard(new BorderLayout(0, 18), 22);

		JPanel header = new JPanel(new BorderLayout(0, 8));
		header.setOpaque(false);
		selectedQuizCodeLabel = AppTheme.createSectionLabel("Select a quiz");
		header.add(selectedQuizCodeLabel, BorderLayout.NORTH);
		selectedResponsesLabel = AppTheme.createMutedLabel("Choose a quiz above to inspect its questions and votes.");
		header.add(selectedResponsesLabel, BorderLayout.SOUTH);
		card.add(header, BorderLayout.NORTH);

		JPanel details = new JPanel();
		details.setOpaque(false);
		details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

		details.add(createFormLabel("Question"));
		details.add(Box.createVerticalStrut(6));
		analyticsQuestionArea = AppTheme.createTextArea(4, 20, false);
		analyticsQuestionArea.setBackground(AppTheme.SURFACE_ALT);
		analyticsQuestionArea.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(AppTheme.BORDER, 1),
			BorderFactory.createEmptyBorder(10, 12, 10, 12)
		));
		details.add(analyticsQuestionArea);
		details.add(Box.createVerticalStrut(16));

		JPanel answersPanel = new JPanel(new GridLayout(4, 1, 0, 10));
		answersPanel.setOpaque(false);
		analyticsOptionLabels = new JLabel[4];
		analyticsVoteLabels = new JLabel[4];
		for (int index = 0; index < 4; index++) {
			JPanel row = new JPanel(new BorderLayout(12, 0));
			row.setOpaque(false);
			row.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(AppTheme.BORDER, 1),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)
			));

			analyticsOptionLabels[index] = AppTheme.createBodyLabel("");
			row.add(analyticsOptionLabels[index], BorderLayout.CENTER);

			analyticsVoteLabels[index] = AppTheme.createBadgeLabel("0 votes", AppTheme.PRIMARY_SOFT, AppTheme.PRIMARY_DARK);
			row.add(analyticsVoteLabels[index], BorderLayout.EAST);
			answersPanel.add(row);
		}
		details.add(answersPanel);

		card.add(details, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		actions.setOpaque(false);
		previousQuestionButton = AppTheme.createSecondaryButton("Previous Question");
		previousQuestionButton.addActionListener(e -> {
			if (selectedQuestionIndex > 0) {
				selectedQuestionIndex--;
				renderSelectedQuestion();
			}
		});
		actions.add(previousQuestionButton);

		nextQuestionButton = AppTheme.createSecondaryButton("Next Question");
		nextQuestionButton.addActionListener(e -> {
			if (selectedQuestionIndex < selectedQuizQuestions.size() - 1) {
				selectedQuestionIndex++;
				renderSelectedQuestion();
			}
		});
		actions.add(nextQuestionButton);

		deleteQuizButton = AppTheme.createDangerButton("Delete Quiz");
		deleteQuizButton.addActionListener(e -> deleteSelectedQuiz());
		actions.add(deleteQuizButton);

		card.add(actions, BorderLayout.SOUTH);
		clearSelectedQuiz();
		return card;
	}

	private JPanel createPageHeader(String titleText, String subtitleText) {
		JPanel header = new JPanel();
		header.setOpaque(false);
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

		JLabel title = AppTheme.createTitleLabel(titleText);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(title);
		header.add(Box.createVerticalStrut(6));

		JLabel subtitle = AppTheme.createMutedLabel(subtitleText);
		subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(subtitle);

		return header;
	}

	private JPanel createOptionFieldPanel(String labelText, JTextField field) {
		JPanel panel = new JPanel(new BorderLayout(0, 6));
		panel.setOpaque(false);
		panel.add(createFormLabel(labelText), BorderLayout.NORTH);
		panel.add(field, BorderLayout.CENTER);
		return panel;
	}

	private JLabel createFormLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(AppTheme.LABEL_FONT);
		label.setForeground(AppTheme.TEXT_SECONDARY);
		return label;
	}

	private void addDraftQuestion() {
		if (draftQuestions.size() >= 50) {
			JOptionPane.showMessageDialog(frame, "A quiz can contain at most 50 questions.", "Draft Full", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String prompt = questionInput.getText().trim();
		String[] options = new String[4];
		for (int index = 0; index < optionInputs.length; index++) {
			options[index] = optionInputs[index].getText().trim();
		}

		if (prompt.isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Enter a question before adding it to the draft.", "Missing Question", JOptionPane.WARNING_MESSAGE);
			return;
		}

		for (String option : options) {
			if (option.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "Each question needs all four answer options.", "Missing Options", JOptionPane.WARNING_MESSAGE);
				return;
			}
		}

		draftQuestions.add(new QuizQuestion(prompt, options[0], options[1], options[2], options[3]));
		refreshDraftListModel();
		clearQuestionForm();
		updateDraftState();
		questionInput.requestFocusInWindow();
	}

	private void removeSelectedDraftQuestion() {
		int index = draftList.getSelectedIndex();
		if (index < 0 || index >= draftQuestions.size()) {
			JOptionPane.showMessageDialog(frame, "Select a question from the draft to remove it.", "Nothing Selected", JOptionPane.WARNING_MESSAGE);
			return;
		}

		draftQuestions.remove(index);
		refreshDraftListModel();
		if (!draftQuestions.isEmpty()) {
			draftList.setSelectedIndex(Math.min(index, draftQuestions.size() - 1));
		}
		updateDraftState();
	}

	private void clearDraft() {
		draftQuestions.clear();
		draftListModel.clear();
		clearQuestionForm();
		updateDraftState();
	}

	private void clearQuestionForm() {
		questionInput.setText("");
		for (JTextField optionInput : optionInputs) {
			optionInput.setText("");
		}
	}

	private void refreshDraftListModel() {
		draftListModel.clear();
		for (int index = 0; index < draftQuestions.size(); index++) {
			draftListModel.addElement((index + 1) + ". " + draftQuestions.get(index).getPreviewText());
		}
	}

	private void updateDraftState() {
		int count = draftQuestions.size();
		questionCountLabel.setText(count + (count == 1 ? " question ready" : " questions ready"));
		if (count == 0) {
			draftHintLabel.setText(hasPendingFormInput()
				? "The form has content that still needs to be added."
				: "Add your first question to start the draft.");
		} else {
			draftHintLabel.setText("Questions are kept in order and a share code is generated automatically when you publish.");
		}

		removeDraftButton.setEnabled(draftList.getSelectedIndex() >= 0);
		clearDraftButton.setEnabled(count > 0 || hasPendingFormInput());
		submitQuizButton.setEnabled(count > 0);
	}

	private boolean hasPendingFormInput() {
		if (!questionInput.getText().trim().isEmpty()) {
			return true;
		}

		for (JTextField optionInput : optionInputs) {
			if (!optionInput.getText().trim().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private void submitQuiz() {
		if (hasPendingFormInput()) {
			JOptionPane.showMessageDialog(frame,
				"The form still contains text. Add that question first or clear the form before publishing.",
				"Draft Not Finished",
				JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (draftQuestions.isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Add at least one question before creating a quiz.", "Empty Draft", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try (SQLoperations manage = new SQLoperations()) {
			String code = manage.createQuiz(id, draftQuestions);
			AppTheme.copyToClipboard(code);
			clearDraft();
			searchField.setText(code);
			refreshQuizTable(code);
			switchView("library");
			JOptionPane.showMessageDialog(frame,
				"Quiz created successfully.\n\nQuiz code: " + code + "\nThe code has been copied to your clipboard.",
				"Quiz Ready",
				JOptionPane.INFORMATION_MESSAGE);
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(frame, ex.getMessage(), "Invalid Quiz", JOptionPane.WARNING_MESSAGE);
		} catch (SQLException ex) {
			showDatabaseError("create the quiz", ex);
		}
	}

	private void refreshQuizTable(String preferredCode) {
		String targetCode = preferredCode != null ? preferredCode : selectedQuizCode;

		try (SQLoperations manage = new SQLoperations()) {
			List<QuizSummary> quizzes = manage.getQuizSummaries(id, searchField == null ? "" : searchField.getText());
			quizTableModel.setRowCount(0);
			int targetRow = -1;

			for (int index = 0; index < quizzes.size(); index++) {
				QuizSummary summary = quizzes.get(index);
				quizTableModel.addRow(new Object[] { summary.getCode(), Integer.valueOf(summary.getTotalResponses()) });
				if (summary.getCode().equals(targetCode)) {
					targetRow = index;
				}
			}

			if (quizTableModel.getRowCount() == 0) {
				clearSelectedQuiz();
				return;
			}

			final int rowToSelect = targetRow >= 0 ? targetRow : 0;
			SwingUtilities.invokeLater(() -> quizTable.setRowSelectionInterval(rowToSelect, rowToSelect));
		} catch (SQLException ex) {
			showDatabaseError("load your quizzes", ex);
		}
	}

	private void loadSelectedQuizFromTable() {
		int row = quizTable.getSelectedRow();
		if (row < 0) {
			clearSelectedQuiz();
			return;
		}

		selectedQuizCode = String.valueOf(quizTableModel.getValueAt(row, 0));
		selectedQuizResponses = ((Integer) quizTableModel.getValueAt(row, 1)).intValue();

		try (SQLoperations manage = new SQLoperations()) {
			List<QuizQuestion> questions = manage.getQuizQuestions(selectedQuizCode);
			for (int index = 0; index < questions.size(); index++) {
				questions.get(index).setVoteCounts(manage.getVoteCounts(selectedQuizCode, index + 1));
			}
			selectedQuizQuestions = questions;
			selectedQuestionIndex = 0;
			renderSelectedQuestion();
		} catch (SQLException ex) {
			showDatabaseError("load quiz details", ex);
		}
	}

	private void renderSelectedQuestion() {
		if (selectedQuizCode == null || selectedQuizQuestions.isEmpty()) {
			clearSelectedQuiz();
			return;
		}

		QuizQuestion question = selectedQuizQuestions.get(selectedQuestionIndex);
		selectedQuizCodeLabel.setText("Quiz " + selectedQuizCode + "  •  Question " + (selectedQuestionIndex + 1) + " of " + selectedQuizQuestions.size());
		selectedResponsesLabel.setText(selectedQuizResponses + (selectedQuizResponses == 1 ? " response recorded" : " responses recorded"));
		analyticsQuestionArea.setText(question.getPrompt());
		analyticsQuestionArea.setCaretPosition(0);

		for (int index = 0; index < 4; index++) {
			analyticsOptionLabels[index].setText(AppTheme.html(question.getOption(index), 430));
			analyticsVoteLabels[index].setText(question.getVoteCount(index) + (question.getVoteCount(index) == 1 ? " vote" : " votes"));
		}

		previousQuestionButton.setEnabled(selectedQuestionIndex > 0);
		nextQuestionButton.setEnabled(selectedQuestionIndex < selectedQuizQuestions.size() - 1);
		deleteQuizButton.setEnabled(true);
	}

	private void clearSelectedQuiz() {
		selectedQuizCode = null;
		selectedQuizResponses = 0;
		selectedQuizQuestions = new ArrayList<QuizQuestion>();
		selectedQuestionIndex = 0;

		if (selectedQuizCodeLabel != null) {
			selectedQuizCodeLabel.setText("Select a quiz");
		}
		if (selectedResponsesLabel != null) {
			selectedResponsesLabel.setText("Choose a quiz above to inspect its questions and votes.");
		}
		if (analyticsQuestionArea != null) {
			analyticsQuestionArea.setText("");
		}
		if (analyticsOptionLabels != null) {
			for (JLabel optionLabel : analyticsOptionLabels) {
				optionLabel.setText("");
			}
		}
		if (analyticsVoteLabels != null) {
			for (JLabel voteLabel : analyticsVoteLabels) {
				voteLabel.setText("0 votes");
			}
		}
		if (previousQuestionButton != null) {
			previousQuestionButton.setEnabled(false);
		}
		if (nextQuestionButton != null) {
			nextQuestionButton.setEnabled(false);
		}
		if (deleteQuizButton != null) {
			deleteQuizButton.setEnabled(false);
		}
	}

	private void deleteSelectedQuiz() {
		if (selectedQuizCode == null) {
			return;
		}

		int decision = JOptionPane.showConfirmDialog(
			frame,
			"Delete quiz " + selectedQuizCode + " and all of its responses?",
			"Delete Quiz",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE);

		if (decision != JOptionPane.YES_OPTION) {
			return;
		}

		try (SQLoperations manage = new SQLoperations()) {
			manage.removeSurvey(selectedQuizCode);
			refreshQuizTable(null);
		} catch (SQLException ex) {
			showDatabaseError("delete the quiz", ex);
		}
	}

	private void showChangePasswordDialog() {
		JPasswordField currentPasswordField = AppTheme.createPasswordField(12);
		JPasswordField newPasswordField = AppTheme.createPasswordField(12);
		JPasswordField confirmPasswordField = AppTheme.createPasswordField(12);

		JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
		panel.setBackground(AppTheme.BACKGROUND);
		panel.add(createFormLabel("Current password"));
		panel.add(currentPasswordField);
		panel.add(createFormLabel("New password"));
		panel.add(newPasswordField);
		panel.add(createFormLabel("Confirm new password"));
		panel.add(confirmPasswordField);

		int result = JOptionPane.showConfirmDialog(frame, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		String currentPassword = new String(currentPasswordField.getPassword());
		String newPassword = new String(newPasswordField.getPassword());
		String confirmPassword = new String(confirmPasswordField.getPassword());

		if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Fill in all three password fields.", "Missing Information", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (newPassword.length() < 6) {
			JOptionPane.showMessageDialog(frame, "The new password must be at least 6 characters long.", "Weak Password", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (!newPassword.equals(confirmPassword)) {
			JOptionPane.showMessageDialog(frame, "The new passwords do not match.", "Password Mismatch", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try (SQLoperations manage = new SQLoperations()) {
			if (!manage.validateUserPassword(id, currentPassword)) {
				JOptionPane.showMessageDialog(frame, "The current password is incorrect.", "Password Incorrect", JOptionPane.WARNING_MESSAGE);
				return;
			}

			manage.changePassword(id, newPassword);
			JOptionPane.showMessageDialog(frame, "Password updated successfully.", "Password Changed", JOptionPane.INFORMATION_MESSAGE);
		} catch (SQLException ex) {
			showDatabaseError("change the password", ex);
		}
	}

	private void switchView(String viewName) {
		contentLayout.show(contentPanel, viewName);
		boolean builderActive = "builder".equals(viewName);
		AppTheme.setSidebarButtonState(addQuizNavButton, builderActive);
		AppTheme.setSidebarButtonState(viewQuizNavButton, !builderActive);
	}

	private void showDatabaseError(String action, SQLException ex) {
		JOptionPane.showMessageDialog(frame,
			"Unable to " + action + ". Check your database setup and try again.\n\n" + ex.getMessage(),
			"Database Error",
			JOptionPane.ERROR_MESSAGE);
	}
}