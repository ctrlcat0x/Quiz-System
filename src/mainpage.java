import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
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
	private JTextArea responseHistoryArea;
	private JLabel responseHistoryHintLabel;
	private JLabel[] analyticsOptionLabels;
	private JLabel[] analyticsVoteLabels;
	private JButton previousQuestionButton;
	private JButton nextQuestionButton;
	private JButton deleteQuizButton;
	private final List<QuizQuestion> draftQuestions = new ArrayList<QuizQuestion>();
	private List<QuizQuestion> selectedQuizQuestions = new ArrayList<QuizQuestion>();
	private List<QuizResponseRecord> selectedResponseRecords = new ArrayList<QuizResponseRecord>();
	private String selectedQuizCode;
	private int selectedQuizResponses;
	private int selectedQuestionIndex;
	private boolean selectedQuizHasLegacyResponses;

	public void mainPageView(int id) {
		this.id = id;

		try (DbOperations manage = DbFactory.create()) {
			username = manage.getUsername(id);
		} catch (Exception ex) {
			showDatabaseError("load your dashboard", ex);
			return;
		}

		if (username == null) {
			JOptionPane.showMessageDialog(null, "That account could not be loaded.", "Sign In Error", JOptionPane.ERROR_MESSAGE);
			new login().loginView();
			return;
		}

		frame = AppTheme.createFrame("Quiz Dashboard", 1180, 760, JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setMinimumSize(new Dimension(1060, 720));
		frame.setLayout(new BorderLayout());

		frame.add(createSidebar(), BorderLayout.WEST);

		contentLayout = new CardLayout();
		contentPanel = new JPanel(contentLayout);
		contentPanel.setBackground(AppTheme.BACKGROUND);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
		contentPanel.add(createScrollableView(createBuilderView()), "builder");
		contentPanel.add(createScrollableView(createLibraryView()), "library");
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

		JLabel welcome = AppTheme.createMutedLabel("Signed in as " + username);
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

		JSplitPane bodySplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createBuilderFormCard(), createDraftPreviewCard());
		bodySplit.setResizeWeight(0.7);
		bodySplit.setDividerSize(10);
		bodySplit.setBorder(BorderFactory.createEmptyBorder());
		bodySplit.setOpaque(false);
		bodySplit.setContinuousLayout(true);
		page.add(bodySplit, BorderLayout.CENTER);

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
		questionInput.getDocument().addDocumentListener(createDraftStateListener());
		JScrollPane questionScroll = new JScrollPane(questionInput);
		questionScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
		questionScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
		questionScroll.getVerticalScrollBar().setUnitIncrement(16);
		form.add(questionScroll);
		form.add(Box.createVerticalStrut(16));

		JPanel optionsGrid = new JPanel(new GridLayout(2, 2, 12, 12));
		optionsGrid.setOpaque(false);
		optionInputs = new JTextField[4];
		for (int index = 0; index < optionInputs.length; index++) {
			optionInputs[index] = AppTheme.createTextField(18);
			optionInputs[index].getDocument().addDocumentListener(createDraftStateListener());
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
		draftListScroll.getVerticalScrollBar().setUnitIncrement(16);
		card.add(draftListScroll, BorderLayout.CENTER);

		JPanel actions = new JPanel();
		actions.setOpaque(false);
		actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

		JPanel utilityActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		utilityActions.setOpaque(false);
		removeDraftButton = AppTheme.createSecondaryButton("Remove Selected");
		removeDraftButton.addActionListener(e -> removeSelectedDraftQuestion());
		utilityActions.add(removeDraftButton);

		clearDraftButton = AppTheme.createSecondaryButton("Clear Draft");
		clearDraftButton.addActionListener(e -> clearDraft());
		utilityActions.add(clearDraftButton);
		actions.add(utilityActions);
		actions.add(Box.createVerticalStrut(12));

		submitQuizButton = AppTheme.createPrimaryButton("Publish Quiz");
		submitQuizButton.addActionListener(e -> submitQuiz());
		JPanel publishRow = new JPanel(new BorderLayout());
		publishRow.setOpaque(false);
		publishRow.add(submitQuizButton, BorderLayout.CENTER);
		actions.add(publishRow);

		card.add(actions, BorderLayout.SOUTH);
		updateDraftState();
		return card;
	}

	private JPanel createLibraryView() {
		JPanel page = new JPanel(new BorderLayout(0, 20));
		page.setOpaque(false);
		page.add(createPageHeader("Quiz Library", "Search your quizzes, inspect response counts, and browse question-by-question analytics."), BorderLayout.NORTH);

		JSplitPane bodySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createQuizTableCard(), createAnalyticsCard());
		bodySplit.setResizeWeight(0.34);
		bodySplit.setDividerSize(10);
		bodySplit.setBorder(BorderFactory.createEmptyBorder());
		bodySplit.setOpaque(false);
		bodySplit.setContinuousLayout(true);
		page.add(bodySplit, BorderLayout.CENTER);

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
		tableScroll.getVerticalScrollBar().setUnitIncrement(16);
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

		JLabel questionLabel = createFormLabel("Question");
		questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		details.add(questionLabel);
		details.add(Box.createVerticalStrut(6));
		analyticsQuestionArea = AppTheme.createTextArea(4, 20, false);
		analyticsQuestionArea.setBackground(AppTheme.SURFACE_ALT);
		JScrollPane analyticsQuestionScroll = new JScrollPane(analyticsQuestionArea);
		analyticsQuestionScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
		analyticsQuestionScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
		analyticsQuestionScroll.setPreferredSize(new Dimension(0, 110));
		analyticsQuestionScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
		analyticsQuestionScroll.getVerticalScrollBar().setUnitIncrement(16);
		details.add(analyticsQuestionScroll);
		details.add(Box.createVerticalStrut(16));

		JPanel answersPanel = new JPanel(new GridLayout(4, 1, 0, 10));
		answersPanel.setOpaque(false);
		answersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		answersPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
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
		details.add(Box.createVerticalStrut(18));

		JPanel responseHistoryPanel = new JPanel(new BorderLayout(0, 8));
		responseHistoryPanel.setOpaque(false);
		responseHistoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		responseHistoryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		JPanel responseHistoryHeader = new JPanel();
		responseHistoryHeader.setOpaque(false);
		responseHistoryHeader.setLayout(new BoxLayout(responseHistoryHeader, BoxLayout.Y_AXIS));
		JLabel responseHistoryTitle = AppTheme.createSectionLabel("Response History");
		responseHistoryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		responseHistoryHeader.add(responseHistoryTitle);
		responseHistoryHeader.add(Box.createVerticalStrut(6));
		responseHistoryHintLabel = AppTheme.createMutedLabel("Timestamped anonymous submissions will appear here.");
		responseHistoryHintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		responseHistoryHeader.add(responseHistoryHintLabel);
		responseHistoryPanel.add(responseHistoryHeader, BorderLayout.NORTH);

		responseHistoryArea = AppTheme.createTextArea(12, 20, false);
		JScrollPane responseHistoryScroll = new JScrollPane(responseHistoryArea);
		responseHistoryScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
		responseHistoryScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
		responseHistoryScroll.setPreferredSize(new Dimension(0, 220));
		responseHistoryScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		responseHistoryScroll.getVerticalScrollBar().setUnitIncrement(16);
		responseHistoryPanel.add(responseHistoryScroll, BorderLayout.CENTER);
		details.add(responseHistoryPanel);

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
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
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
		updateDraftState();
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

	private DocumentListener createDraftStateListener() {
		return new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateDraftState();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateDraftState();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateDraftState();
			}
		};
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

		try (DbOperations manage = DbFactory.create()) {
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
		} catch (Exception ex) {
			showDatabaseError("create the quiz", ex);
		}
	}

	private void refreshQuizTable(String preferredCode) {
		String targetCode = preferredCode != null ? preferredCode : selectedQuizCode;

		try (DbOperations manage = DbFactory.create()) {
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
		} catch (Exception ex) {
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

		try (DbOperations manage = DbFactory.create()) {
			List<QuizQuestion> questions = manage.getQuizQuestions(selectedQuizCode);
			for (int index = 0; index < questions.size(); index++) {
				questions.get(index).setVoteCounts(manage.getVoteCounts(selectedQuizCode, index + 1));
			}
			selectedQuizQuestions = questions;
			selectedResponseRecords = manage.getQuizResponseRecords(selectedQuizCode, questions.size());
			selectedQuizHasLegacyResponses = manage.hasLegacyQuizResponses(selectedQuizCode);
			selectedQuestionIndex = 0;
			renderSelectedQuestion();
		} catch (Exception ex) {
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
		String responseText = selectedQuizResponses + (selectedQuizResponses == 1 ? " response recorded" : " responses recorded");
		if (!selectedResponseRecords.isEmpty()) {
			responseText += "  •  " + selectedResponseRecords.size() + (selectedResponseRecords.size() == 1 ? " detailed submission" : " detailed submissions");
		}
		selectedResponsesLabel.setText(responseText);
		analyticsQuestionArea.setText(question.getPrompt());
		analyticsQuestionArea.setCaretPosition(0);

		for (int index = 0; index < 4; index++) {
			analyticsOptionLabels[index].setText(AppTheme.html(question.getOption(index), 430));
			analyticsVoteLabels[index].setText(question.getVoteCount(index) + (question.getVoteCount(index) == 1 ? " vote" : " votes"));
		}

		previousQuestionButton.setEnabled(selectedQuestionIndex > 0);
		nextQuestionButton.setEnabled(selectedQuestionIndex < selectedQuizQuestions.size() - 1);
		deleteQuizButton.setEnabled(true);
		renderResponseHistory();
	}

	private void clearSelectedQuiz() {
		selectedQuizCode = null;
		selectedQuizResponses = 0;
		selectedQuizQuestions = new ArrayList<QuizQuestion>();
		selectedResponseRecords = new ArrayList<QuizResponseRecord>();
		selectedQuestionIndex = 0;
		selectedQuizHasLegacyResponses = false;

		if (selectedQuizCodeLabel != null) {
			selectedQuizCodeLabel.setText("Select a quiz");
		}
		if (selectedResponsesLabel != null) {
			selectedResponsesLabel.setText("Choose a quiz above to inspect its questions and votes.");
		}
		if (analyticsQuestionArea != null) {
			analyticsQuestionArea.setText("");
		}
		if (responseHistoryArea != null) {
			responseHistoryArea.setText("");
		}
		if (responseHistoryHintLabel != null) {
			responseHistoryHintLabel.setText("Choose a quiz above to inspect timestamped response entries.");
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

	private void renderResponseHistory() {
		if (responseHistoryArea == null || responseHistoryHintLabel == null) {
			return;
		}

		if (selectedQuizCode == null) {
			responseHistoryHintLabel.setText("Choose a quiz above to inspect timestamped response entries.");
			responseHistoryArea.setText("");
			return;
		}

		if (selectedResponseRecords.isEmpty()) {
			if (selectedQuizHasLegacyResponses) {
				responseHistoryHintLabel.setText("Legacy responses are counted above, but they do not include per-attempt timestamps or selected answers.");
				responseHistoryArea.setText("Detailed response history is only available for submissions recorded after this update.\n\nSubmit the quiz again to generate timestamped anonymous entries here.");
			} else {
				responseHistoryHintLabel.setText("No detailed submissions have been recorded for this quiz yet.");
				responseHistoryArea.setText("");
			}
			responseHistoryArea.setCaretPosition(0);
			return;
		}

		responseHistoryHintLabel.setText(selectedResponseRecords.size()
			+ (selectedResponseRecords.size() == 1 ? " anonymous submission shown below." : " anonymous submissions shown below."));

		StringBuilder history = new StringBuilder();
		for (int recordIndex = 0; recordIndex < selectedResponseRecords.size(); recordIndex++) {
			QuizResponseRecord record = selectedResponseRecords.get(recordIndex);
			history.append(record.getRespondentLabel())
				.append(" • ")
				.append(record.getSubmittedAtDisplay())
				.append('\n');

			for (int questionIndex = 0; questionIndex < selectedQuizQuestions.size(); questionIndex++) {
				QuizQuestion question = selectedQuizQuestions.get(questionIndex);
				history.append("Q")
					.append(questionIndex + 1)
					.append(": ")
					.append(question.getPreviewText())
					.append('\n')
					.append("   Chose: ");

				int selectedOption = record.getAnswer(questionIndex);
				if (selectedOption >= 1 && selectedOption <= 4) {
					history.append(selectedOption)
						.append(". ")
						.append(question.getOption(selectedOption - 1));
				} else {
					history.append("No answer recorded");
				}
				history.append('\n');
			}

			if (recordIndex < selectedResponseRecords.size() - 1) {
				history.append('\n');
			}
		}

		if (selectedQuizHasLegacyResponses) {
			history.append('\n')
				.append("Legacy responses recorded before this update are still included in the vote totals above, but they do not contain per-attempt timestamps or selected answers.");
		}

		responseHistoryArea.setText(history.toString());
		responseHistoryArea.setCaretPosition(0);
	}

	private JScrollPane createScrollableView(JPanel view) {
		ViewportPanel viewportPanel = new ViewportPanel(new BorderLayout());
		viewportPanel.setOpaque(false);
		viewportPanel.add(view, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(viewportPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setBackground(AppTheme.BACKGROUND);
		scrollPane.getVerticalScrollBar().setUnitIncrement(18);
		return scrollPane;
	}

	private static final class ViewportPanel extends JPanel implements Scrollable {
		private static final long serialVersionUID = 1L;

		private ViewportPanel(BorderLayout layout) {
			super(layout);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 18;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return orientation == SwingConstants.VERTICAL ? visibleRect.height - 18 : visibleRect.width - 18;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
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

		try (DbOperations manage = DbFactory.create()) {
			manage.removeSurvey(selectedQuizCode);
			refreshQuizTable(null);
		} catch (Exception ex) {
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

		try (DbOperations manage = DbFactory.create()) {
			if (!manage.validateUserPassword(id, currentPassword)) {
				JOptionPane.showMessageDialog(frame, "The current password is incorrect.", "Password Incorrect", JOptionPane.WARNING_MESSAGE);
				return;
			}

			manage.changePassword(id, newPassword);
			JOptionPane.showMessageDialog(frame, "Password updated successfully.", "Password Changed", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			showDatabaseError("change the password", ex);
		}
	}

	private void switchView(String viewName) {
		contentLayout.show(contentPanel, viewName);
		boolean builderActive = "builder".equals(viewName);
		AppTheme.setSidebarButtonState(addQuizNavButton, builderActive);
		AppTheme.setSidebarButtonState(viewQuizNavButton, !builderActive);
	}

	private void showDatabaseError(String action, Exception ex) {
		JOptionPane.showMessageDialog(frame,
			"Unable to " + action + ". An error occurred.\n\n" + ex.getMessage(),
			"Error",
			JOptionPane.ERROR_MESSAGE);
	}
}