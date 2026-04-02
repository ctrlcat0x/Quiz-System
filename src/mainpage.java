import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class mainpage {
	
	private static final Color PRIMARY = new Color(99, 102, 241);
	private static final Color SIDEBAR_COLOR = new Color(17, 24, 39);
	private static final Color BG_COLOR = new Color(249, 250, 251);
	private static final Color CARD_COLOR = Color.WHITE;
	private static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 26);
	private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);
	private static final Font FIELD_FONT = new Font("SansSerif", Font.PLAIN, 14);
	private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);
	private static final Font SIDEBAR_FONT = new Font("SansSerif", Font.BOLD, 14);

	SQLoperations manage;
	JButton submit;
	String[] questionsArray, option1Array, option2Array, option3Array, option4Array;
	static DefaultTableModel model;
	String cd;
	
	int i=0, h=0;
	String[] queStr = new String[50];
	String[] op1Str = new String[50];
	String[] op2Str = new String[50];
	String[] op3Str = new String[50];
	String[] op4Str = new String[50];
	int id;

	public void mainPageView(int id) throws SQLException {
		this.id=id;
		questionsArray = new String[25];
		option1Array = new String[25]; 
		option2Array = new String[25];
		option3Array = new String[25];
		option4Array = new String[25];
		
		manage = new SQLoperations();
		
		JFrame frame = new JFrame("Quiz Management System");
		frame.setSize(850, 620);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setBackground(BG_COLOR);
		frame.setResizable(false);
		
		/* ========== ADD QUIZ PANEL ========== */
		JPanel addPanel = new JPanel();
		addPanel.setBounds(220, 0, 630, 620);
		addPanel.setLayout(null);
		addPanel.setBackground(BG_COLOR);

		JLabel start = new JLabel("Create a Quiz");
		start.setBounds(0, 15, 630, 40);
		start.setHorizontalAlignment(JLabel.CENTER);
		start.setFont(HEADING_FONT);
		start.setForeground(PRIMARY);
		addPanel.add(start);

		JPanel addCard = new JPanel();
		addCard.setBounds(40, 65, 550, 500);
		addCard.setLayout(null);
		addCard.setBackground(CARD_COLOR);
		addCard.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
			BorderFactory.createEmptyBorder(15, 15, 15, 15)
		));
		addPanel.add(addCard);

		JLabel questionCountLabel = new JLabel("Questions added: 0");
		questionCountLabel.setBounds(30, 15, 200, 20);
		questionCountLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		questionCountLabel.setForeground(new Color(34, 197, 94));
		addCard.add(questionCountLabel);

		JLabel question = new JLabel("Question");
		question.setBounds(30, 40, 100, 20);
		question.setFont(LABEL_FONT);
		question.setForeground(new Color(75, 85, 99));
		addCard.add(question);
		JTextField questionField = new JTextField();
		questionField.setBounds(30, 63, 490, 32);
		questionField.setFont(FIELD_FONT);
		questionField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		addCard.add(questionField);
		
		JLabel option1 = new JLabel("Option 1");
		option1.setBounds(30, 105, 100, 20);
		option1.setFont(LABEL_FONT);
		option1.setForeground(new Color(75, 85, 99));
		addCard.add(option1);
		JTextField option1Field = new JTextField();
		option1Field.setBounds(30, 127, 230, 32);
		option1Field.setFont(FIELD_FONT);
		option1Field.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		addCard.add(option1Field);
		
		JLabel option2 = new JLabel("Option 2");
		option2.setBounds(290, 105, 100, 20);
		option2.setFont(LABEL_FONT);
		option2.setForeground(new Color(75, 85, 99));
		addCard.add(option2);
		JTextField option2Field = new JTextField();
		option2Field.setBounds(290, 127, 230, 32);
		option2Field.setFont(FIELD_FONT);
		option2Field.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		addCard.add(option2Field);
		
		JLabel option3 = new JLabel("Option 3");
		option3.setBounds(30, 170, 100, 20);
		option3.setFont(LABEL_FONT);
		option3.setForeground(new Color(75, 85, 99));
		addCard.add(option3);
		JTextField option3Field = new JTextField();
		option3Field.setBounds(30, 192, 230, 32);
		option3Field.setFont(FIELD_FONT);
		option3Field.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		addCard.add(option3Field);
		
		JLabel option4 = new JLabel("Option 4");
		option4.setBounds(290, 170, 100, 20);
		option4.setFont(LABEL_FONT);
		option4.setForeground(new Color(75, 85, 99));
		addCard.add(option4);
		JTextField option4Field = new JTextField();
		option4Field.setBounds(290, 192, 230, 32);
		option4Field.setFont(FIELD_FONT);
		option4Field.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		addCard.add(option4Field);
		
		JButton next = new JButton("ADD QUESTION");
		next.setBounds(30, 250, 490, 36);
		next.setFont(BUTTON_FONT);
		next.setBackground(PRIMARY);
		next.setForeground(Color.WHITE);
		next.setFocusPainted(false);
		next.setBorderPainted(false);
		next.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addCard.add(next);
		next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String qn = questionField.getText();
				String op1 = option1Field.getText();
				String op2 = option2Field.getText();
				String op3 = option3Field.getText();
				String op4 = option4Field.getText();
				if(qn.equals("") || op1.equals("") || op2.equals("") || op3.equals("") || op4.equals("")) {
					JOptionPane.showMessageDialog(frame, "Please fill all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
				else {
					questionField.setText("");
					option1Field.setText("");
					option2Field.setText("");
					option3Field.setText("");
					option4Field.setText("");
					queStr[i] = qn;
					op1Str[i] = op1;
					op2Str[i] = op2;
					op3Str[i] = op3;
					op4Str[i] = op4;
					i++;
					submit.setEnabled(true);
					questionCountLabel.setText("Questions added: " + i);
				}
			}
		});
		
		submit = new JButton("SUBMIT QUIZ");
		submit.setBounds(30, 305, 235, 45);
		submit.setEnabled(false);
		submit.setFont(BUTTON_FONT);
		submit.setBackground(new Color(34, 197, 94));
		submit.setForeground(Color.WHITE);
		submit.setFocusPainted(false);
		submit.setBorderPainted(false);
		submit.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addCard.add(submit);
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String code = generateUniqueCode();
				String qn = questionField.getText();
				String op1 = option1Field.getText();
				String op2 = option2Field.getText();
				String op3 = option3Field.getText();
				String op4 = option4Field.getText();
				if(!(qn.equals("") && op1.equals("") && op2.equals("") && op3.equals("") && op4.equals(""))) {
					JOptionPane.showMessageDialog(frame, "Last details are not added. If not required, please clear the fields.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
				else {
					if(i==0) {
						JOptionPane.showMessageDialog(frame, "No questions added.", "Warning", JOptionPane.WARNING_MESSAGE);
					}
					else {
						try {
							manage.userQuestionAdd(id, code);
							for(int j=0; j<i; j++) {
								manage.newQuestion(code, queStr[j], op1Str[j], op2Str[j], op3Str[j], op4Str[j]);
							}
							JOptionPane.showMessageDialog(frame, "Quiz created! Your Quiz Code: " + code, "Congratulations", JOptionPane.PLAIN_MESSAGE);
							i = 0;
							questionCountLabel.setText("Questions added: 0");
						}
						catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
				}
				submit.setEnabled(false);
			}
		});
		
		JButton cancel = new JButton("CANCEL");
		cancel.setBounds(285, 305, 235, 45);
		cancel.setFont(BUTTON_FONT);
		cancel.setBackground(new Color(239, 68, 68));
		cancel.setForeground(Color.WHITE);
		cancel.setFocusPainted(false);
		cancel.setBorderPainted(false);
		cancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addCard.add(cancel);
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				questionField.setText("");
				option1Field.setText("");
				option2Field.setText("");
				option3Field.setText("");
				option4Field.setText("");
				i=0;
				submit.setEnabled(false);
				questionCountLabel.setText("Questions added: 0");
			}
		});
		
		frame.add(addPanel);
		
		/* ========== VIEW QUIZZES PANEL ========== */
		JPanel viewPanel = new JPanel();
		viewPanel.setBounds(220, 0, 630, 620);
		viewPanel.setLayout(null);
		viewPanel.setBackground(BG_COLOR);

		JLabel viewHeading = new JLabel("Your Quizzes");
		viewHeading.setBounds(0, 15, 630, 40);
		viewHeading.setHorizontalAlignment(JLabel.CENTER);
		viewHeading.setFont(HEADING_FONT);
		viewHeading.setForeground(PRIMARY);
		viewPanel.add(viewHeading);

		JLabel searchLabel = new JLabel("Search:");
		searchLabel.setBounds(80, 60, 60, 30);
		searchLabel.setFont(LABEL_FONT);
		searchLabel.setForeground(new Color(75, 85, 99));
		viewPanel.add(searchLabel);
		
		JTextField search = new JTextField();
		search.setBounds(140, 60, 350, 32);
		search.setFont(FIELD_FONT);
		search.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		viewPanel.add(search);
		search.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				tableupdate(search.getText());
			}

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {}
		});
		
		JTable table = new JTable() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		model = (DefaultTableModel) table.getModel();
		table.setBackground(Color.WHITE);
		table.setFont(FIELD_FONT);
		table.setRowHeight(28);
		table.getTableHeader().setFont(BUTTON_FONT);
		model.addColumn("Quiz Code");
		model.addColumn("Responses");
		tableupdate("");
		JScrollPane scPane = new JScrollPane(table);
		scPane.setBounds(80, 100, 470, 200);
		scPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
		viewPanel.add(scPane);
		
		JLabel quesView = new JLabel();
		quesView.setBounds(80, 350, 470, 30);
		quesView.setFont(new Font("SansSerif", Font.BOLD, 14));
		quesView.setForeground(new Color(17, 24, 39));
		viewPanel.add(quesView);
		
		JLabel op1View = new JLabel();
		op1View.setBounds(100, 380, 260, 25);
		op1View.setFont(LABEL_FONT);
		viewPanel.add(op1View);
		
		JLabel op2View = new JLabel();
		op2View.setBounds(100, 405, 260, 25);
		op2View.setFont(LABEL_FONT);
		viewPanel.add(op2View);
		
		JLabel op3View = new JLabel();
		op3View.setBounds(100, 430, 260, 25);
		op3View.setFont(LABEL_FONT);
		viewPanel.add(op3View);
		
		JLabel op4View = new JLabel();
		op4View.setBounds(100, 455, 260, 25);
		op4View.setFont(LABEL_FONT);
		viewPanel.add(op4View);
		
		JLabel op1Select = new JLabel();
		op1Select.setBounds(375, 380, 110, 25);
		op1Select.setFont(new Font("SansSerif", Font.BOLD, 13));
		op1Select.setForeground(PRIMARY);
		viewPanel.add(op1Select);
		
		JLabel op2Select = new JLabel();
		op2Select.setBounds(375, 405, 110, 25);
		op2Select.setFont(new Font("SansSerif", Font.BOLD, 13));
		op2Select.setForeground(PRIMARY);
		viewPanel.add(op2Select);
		
		JLabel op3Select = new JLabel();
		op3Select.setBounds(375, 430, 110, 25);
		op3Select.setFont(new Font("SansSerif", Font.BOLD, 13));
		op3Select.setForeground(PRIMARY);
		viewPanel.add(op3Select);
		
		JLabel op4Select = new JLabel();
		op4Select.setBounds(375, 455, 110, 25);
		op4Select.setFont(new Font("SansSerif", Font.BOLD, 13));
		op4Select.setForeground(PRIMARY);
		viewPanel.add(op4Select);
		
		JButton viewPrev = new JButton("PREVIOUS");
		viewPrev.setBounds(80, 310, 120, 36);
		viewPrev.setEnabled(false);
		viewPrev.setFont(BUTTON_FONT);
		viewPrev.setBackground(new Color(238, 242, 255));
		viewPrev.setForeground(PRIMARY);
		viewPrev.setFocusPainted(false);
		viewPrev.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
		viewPrev.setCursor(new Cursor(Cursor.HAND_CURSOR));
		viewPanel.add(viewPrev);
		viewPrev.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(h > 0) {
					h--;
					quesView.setText("Q" + (h + 1) + ": " + questionsArray[h]);
					op1View.setText("1. " + option1Array[h]);
					op2View.setText("2. " + option2Array[h]);
					op3View.setText("3. " + option3Array[h]);
					op4View.setText("4. " + option4Array[h]);
					try {
						op1Select.setText("Votes: " + manage.getCount(cd, h, 1));
						op2Select.setText("Votes: " + manage.getCount(cd, h, 2));
						op3Select.setText("Votes: " + manage.getCount(cd, h, 3));
						op4Select.setText("Votes: " + manage.getCount(cd, h, 4));
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JButton delete = new JButton("DELETE");
		delete.setBounds(220, 310, 130, 36);
		delete.setEnabled(false);
		delete.setFont(BUTTON_FONT);
		delete.setBackground(new Color(239, 68, 68));
		delete.setForeground(Color.WHITE);
		delete.setFocusPainted(false);
		delete.setBorderPainted(false);
		delete.setCursor(new Cursor(Cursor.HAND_CURSOR));
		viewPanel.add(delete);
		
		JButton viewNext = new JButton("NEXT");
		viewNext.setBounds(370, 310, 120, 36);
		viewNext.setEnabled(false);
		viewNext.setFont(BUTTON_FONT);
		viewNext.setBackground(new Color(238, 242, 255));
		viewNext.setForeground(PRIMARY);
		viewNext.setFocusPainted(false);
		viewNext.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
		viewNext.setCursor(new Cursor(Cursor.HAND_CURSOR));
		viewPanel.add(viewNext);
		viewNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (h + 1 < questionsArray.length && questionsArray[h + 1] != null) {
					h++;
					quesView.setText("Q" + (h + 1) + ": " + questionsArray[h]);
					op1View.setText("1. " + option1Array[h]);
					op2View.setText("2. " + option2Array[h]);
					op3View.setText("3. " + option3Array[h]);
					op4View.setText("4. " + option4Array[h]);
					try {
						op1Select.setText("Votes: " + manage.getCount(cd, h, 1));
						op2Select.setText("Votes: " + manage.getCount(cd, h, 2));
						op3Select.setText("Votes: " + manage.getCount(cd, h, 3));
						op4Select.setText("Votes: " + manage.getCount(cd, h, 4));
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this quiz?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					try {
						manage.removeSurvey(cd);
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					tableupdate(search.getText());
					quesView.setText("");
					op1View.setText(""); op2View.setText(""); op3View.setText(""); op4View.setText("");
					op1Select.setText(""); op2Select.setText(""); op3Select.setText(""); op4Select.setText("");
					delete.setEnabled(false);
					viewPrev.setEnabled(false);
					viewNext.setEnabled(false);
				}
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				h=0;
				delete.setEnabled(true);
				viewNext.setEnabled(true);
				viewPrev.setEnabled(true);
				int row = table.getSelectedRow();
				cd = String.valueOf(model.getValueAt(row, 0));
				// Reset arrays
				questionsArray = new String[25];
				option1Array = new String[25];
				option2Array = new String[25];
				option3Array = new String[25];
				option4Array = new String[25];
				try {
					ResultSet rst1 = manage.getQuestions(cd);
					for(int x=0; rst1.next(); x++) {
						questionsArray[x] = rst1.getString("question");
						option1Array[x] = rst1.getString("option1");
						option2Array[x] = rst1.getString("option2");
						option3Array[x] = rst1.getString("option3");
						option4Array[x] = rst1.getString("option4");
					}
					op1Select.setText("Votes: " + manage.getCount(cd, h, 1));
					op2Select.setText("Votes: " + manage.getCount(cd, h, 2));
					op3Select.setText("Votes: " + manage.getCount(cd, h, 3));
					op4Select.setText("Votes: " + manage.getCount(cd, h, 4));
				} 
				catch (SQLException e1) {
					e1.printStackTrace();
				}
				quesView.setText("Q1: " + questionsArray[h]);
				op1View.setText("1. " + option1Array[h]);
				op2View.setText("2. " + option2Array[h]);
				op3View.setText("3. " + option3Array[h]);
				op4View.setText("4. " + option4Array[h]);
			}
		});
	
		frame.add(viewPanel);
		
		/* ========== SIDEBAR ========== */
		JPanel optionPanel = new JPanel();
		optionPanel.setBounds(0, 0, 220, 620);
		optionPanel.setBackground(SIDEBAR_COLOR);
		optionPanel.setLayout(null);
		frame.add(optionPanel);

		JLabel sidebarTitle = new JLabel("Quiz System");
		sidebarTitle.setBounds(0, 25, 220, 30);
		sidebarTitle.setHorizontalAlignment(JLabel.CENTER);
		sidebarTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
		sidebarTitle.setForeground(Color.WHITE);
		optionPanel.add(sidebarTitle);
		
		JButton addSurvey = new JButton("ADD QUIZ");
		addSurvey.setBounds(25, 100, 170, 45);
		addSurvey.setFont(SIDEBAR_FONT);
		addSurvey.setBackground(PRIMARY);
		addSurvey.setForeground(Color.WHITE);
		addSurvey.setFocusPainted(false);
		addSurvey.setBorderPainted(false);
		addSurvey.setCursor(new Cursor(Cursor.HAND_CURSOR));
		optionPanel.add(addSurvey);
		addSurvey.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viewPanel.setVisible(false);
				addPanel.setVisible(true);
			}
		});
		
		JButton viewSurvey = new JButton("VIEW QUIZZES");
		viewSurvey.setBounds(25, 165, 170, 45);
		viewSurvey.setFont(SIDEBAR_FONT);
		viewSurvey.setBackground(new Color(30, 41, 59));
		viewSurvey.setForeground(Color.WHITE);
		viewSurvey.setFocusPainted(false);
		viewSurvey.setBorderPainted(false);
		viewSurvey.setCursor(new Cursor(Cursor.HAND_CURSOR));
		optionPanel.add(viewSurvey);
		viewSurvey.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tableupdate(search.getText());
				viewPanel.setVisible(true);
				addPanel.setVisible(false);
			}
		});

		JButton changePass = new JButton("CHANGE PASSWORD");
		changePass.setBounds(25, 230, 170, 45);
		changePass.setFont(new Font("SansSerif", Font.BOLD, 12));
		changePass.setBackground(new Color(30, 41, 59));
		changePass.setForeground(Color.WHITE);
		changePass.setFocusPainted(false);
		changePass.setBorderPainted(false);
		changePass.setCursor(new Cursor(Cursor.HAND_CURSOR));
		optionPanel.add(changePass);
		changePass.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPasswordField currentPassField = new JPasswordField();
				JPasswordField newPassField = new JPasswordField();
				JPasswordField confirmPassField = new JPasswordField();
				Object[] fields = {
					"Current Password:", currentPassField,
					"New Password:", newPassField,
					"Confirm New Password:", confirmPassField
				};
				int result = JOptionPane.showConfirmDialog(frame, fields, "Change Password", JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					String currentPass = new String(currentPassField.getPassword());
					String newPass = new String(newPassField.getPassword());
					String confirmPass = new String(confirmPassField.getPassword());
					if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
						JOptionPane.showMessageDialog(frame, "Please fill all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
					} else if (!newPass.equals(confirmPass)) {
						JOptionPane.showMessageDialog(frame, "New passwords do not match.", "Warning", JOptionPane.WARNING_MESSAGE);
					} else {
						try {
							SQLoperations sqlOp = new SQLoperations();
							int authResult = sqlOp.authUser(sqlOp.getUsername(id), currentPass);
							if (authResult <= 0) {
								JOptionPane.showMessageDialog(frame, "Current password is incorrect.", "Warning", JOptionPane.WARNING_MESSAGE);
							} else {
								sqlOp.changePassword(id, newPass);
								JOptionPane.showMessageDialog(frame, "Password changed successfully!", "Success", JOptionPane.PLAIN_MESSAGE);
							}
						} catch (SQLException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});
		
		JButton logout = new JButton("LOGOUT");
		logout.setBounds(25, 500, 170, 45);
		logout.setFont(SIDEBAR_FONT);
		logout.setBackground(new Color(239, 68, 68));
		logout.setForeground(Color.WHITE);
		logout.setFocusPainted(false);
		logout.setBorderPainted(false);
		logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
		optionPanel.add(logout);
		logout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				login login = new login();
				try {
					login.loginView();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				frame.dispose();
			}
		});
		
		viewPanel.setVisible(false);
		
		frame.setVisible(true);
	}
	
	public String generateUniqueCode() {
		String code;
		try {
			do {
				code = stringGenerator();
			} while (manage.check(code));
			return code;
		} catch (SQLException e) {
			e.printStackTrace();
			return stringGenerator();
		}
	}
	
	public String stringGenerator() {
		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			int index = (int)(AlphaNumericString.length() * Math.random());
			sb.append(AlphaNumericString.charAt(index));
		}
		return sb.toString();
	}
	
	public void tableupdate(String str) {
		try {
			SQLoperations man = new SQLoperations();
			ResultSet res = man.surveys(id, str);
			int rowCount = model.getRowCount();
			for (int i = rowCount - 1; i >= 0; i--)
			    model.removeRow(i);
			for(int i = 0; res.next(); i++) {
				model.addRow(new Object[0]);
		        model.setValueAt(res.getString("quizcode"), i, 0);
		        model.setValueAt(res.getInt("total"), i, 1);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}
