import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class guest {
	
	private static final Color PRIMARY = new Color(99, 102, 241);
	private static final Color BG_COLOR = new Color(249, 250, 251);
	private static final Color CARD_COLOR = Color.WHITE;
	private static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 26);
	private static final Font QUESTION_FONT = new Font("SansSerif", Font.BOLD, 16);
	private static final Font OPTION_FONT = new Font("SansSerif", Font.PLAIN, 14);
	private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);

	SQLoperations manage;
	int[] opt;
	int k;
	
	public void guestView(String surveyCode) throws SQLException {
		
		manage = new SQLoperations();
		ResultSet rst = manage.getQuestions(surveyCode);
		opt = new int[50];
		
		JFrame frame = new JFrame("Quiz System - Taking Quiz");
		frame.setSize(650, 520);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setBackground(BG_COLOR);
		frame.setResizable(false);

		JLabel start = new JLabel("Taking Quiz: " + surveyCode);
		start.setBounds(0, 15, 650, 35);
		start.setHorizontalAlignment(JLabel.CENTER);
		start.setFont(HEADING_FONT);
		start.setForeground(PRIMARY);
		frame.add(start);

		JPanel card = new JPanel();
		card.setBounds(50, 65, 545, 350);
		card.setLayout(null);
		card.setBackground(CARD_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
			BorderFactory.createEmptyBorder(15, 15, 15, 15)
		));
		frame.add(card);
		
		JLabel ques = new JLabel("Loading question...");
		ques.setBounds(30, 20, 490, 30);
		ques.setFont(QUESTION_FONT);
		ques.setForeground(new Color(17, 24, 39));
		card.add(ques);
		
		JRadioButton op1 = new JRadioButton("Option 1");
		JRadioButton op2 = new JRadioButton("Option 2");
		JRadioButton op3 = new JRadioButton("Option 3");
		JRadioButton op4 = new JRadioButton("Option 4");
		
		ButtonGroup bgroup = new ButtonGroup();
		bgroup.add(op1);
		bgroup.add(op2);
		bgroup.add(op3);
		bgroup.add(op4);
		
		op1.setBounds(50, 70, 450, 35);
		op2.setBounds(50, 115, 450, 35);
		op3.setBounds(50, 160, 450, 35);
		op4.setBounds(50, 205, 450, 35);
		
		op1.setFont(OPTION_FONT);
		op2.setFont(OPTION_FONT);
		op3.setFont(OPTION_FONT);
		op4.setFont(OPTION_FONT);

		op1.setBackground(CARD_COLOR);
		op2.setBackground(CARD_COLOR);
		op3.setBackground(CARD_COLOR);
		op4.setBackground(CARD_COLOR);

		op1.setFocusPainted(false);
		op2.setFocusPainted(false);
		op3.setFocusPainted(false);
		op4.setFocusPainted(false);
		
		if(rst.next()) {
			ques.setText("Q1: " + rst.getString("question"));
			op1.setText(rst.getString("option1"));
			op2.setText(rst.getString("option2"));
			op3.setText(rst.getString("option3"));
			op4.setText(rst.getString("option4"));
		}
		
		card.add(op1);
		card.add(op2);
		card.add(op3);
		card.add(op4);
		k=0;
		
		JButton nextButton = new JButton("NEXT QUESTION");
		nextButton.setBounds(30, 275, 490, 45);
		nextButton.setFont(BUTTON_FONT);
		nextButton.setBackground(PRIMARY);
		nextButton.setForeground(Color.WHITE);
		nextButton.setFocusPainted(false);
		nextButton.setBorderPainted(false);
		nextButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		card.add(nextButton);
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int x;
				if(op1.isSelected()) {
					x=1;
				}
				else if(op2.isSelected()) {
					x=2;
				}
				else if(op3.isSelected()) {
					x=3;
				}
				else if(op4.isSelected()) {
					x=4;
				}
				else
					x=0;
				
				if(x!=0) {		
					opt[k] = x;
					k++;
					try {
						if(rst.next()) {
							ques.setText("Q" + (k + 1) + ": " + rst.getString("question"));
							op1.setText(rst.getString("option1"));
							op2.setText(rst.getString("option2"));
							op3.setText(rst.getString("option3"));
							op4.setText(rst.getString("option4"));
						}
						else {
							for(int j=0; j<k; j++) {
								manage.answerUpdt(surveyCode, j+1, opt[j]);
							}
							JOptionPane.showMessageDialog(frame, "Quiz completed. Thank you!", "Congratulations", JOptionPane.PLAIN_MESSAGE);
							manage.addTotal(surveyCode);
							frame.dispose();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				else {
					JOptionPane.showMessageDialog(frame, "Please select an option.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
				bgroup.clearSelection();
			}
		});
		
		frame.setVisible(true);
		
	}
}
