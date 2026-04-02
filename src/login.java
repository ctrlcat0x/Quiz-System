import java.awt.Font;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class login {
    int id;

    private static final Color PRIMARY = new Color(99, 102, 241);
    private static final Color BG_COLOR = new Color(249, 250, 251);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FIELD_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);

    public void loginView() throws SQLException {
		SQLoperations manage = new SQLoperations();
		JFrame frame = new JFrame("Quiz Management System - Login");
		frame.setSize(520, 490);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(BG_COLOR);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

		// Card panel
		JPanel card = new JPanel();
		card.setBounds(60, 30, 390, 415);
		card.setLayout(null);
		card.setBackground(CARD_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
			BorderFactory.createEmptyBorder(20, 20, 20, 20)
		));
		frame.add(card);

		JLabel heading = new JLabel("Quiz System");
		heading.setBounds(0, 20, 390, 40);
		heading.setHorizontalAlignment(JLabel.CENTER);
		heading.setFont(HEADING_FONT);
		heading.setForeground(PRIMARY);
		card.add(heading);

		JLabel subtitle = new JLabel("Sign in to manage your quizzes");
		subtitle.setBounds(0, 58, 390, 22);
		subtitle.setHorizontalAlignment(JLabel.CENTER);
		subtitle.setFont(LABEL_FONT);
		subtitle.setForeground(new Color(107, 114, 128));
		card.add(subtitle);
		
		JLabel uname = new JLabel("Username");
		uname.setBounds(40, 100, 150, 20);
		uname.setFont(LABEL_FONT);
		uname.setForeground(new Color(75, 85, 99));
		card.add(uname);
		
		JTextField name = new JTextField();
		name.setBounds(40, 122, 310, 34);
		name.setFont(FIELD_FONT);
		name.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		card.add(name);
		
		JLabel upass = new JLabel("Password");
		upass.setBounds(40, 164, 150, 20);
		upass.setFont(LABEL_FONT);
		upass.setForeground(new Color(75, 85, 99));
		card.add(upass);
		
		JPasswordField pass = new JPasswordField();
		pass.setBounds(40, 186, 310, 34);
		pass.setFont(FIELD_FONT);
		pass.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		card.add(pass);

		JButton loginBtn = new JButton("LOGIN");
		loginBtn.setBounds(40, 250, 148, 38);
		loginBtn.setFont(BUTTON_FONT);
		loginBtn.setBackground(PRIMARY);
		loginBtn.setForeground(Color.WHITE);
		loginBtn.setFocusPainted(false);
		loginBtn.setBorderPainted(false);
		loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		card.add(loginBtn);
		loginBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String username = name.getText();
				String password = new String(pass.getPassword());
				if(username.isEmpty() || password.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please enter all information.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
				else {
					try {
						SQLoperations manage = new SQLoperations();
						id = manage.authUser(username, password);
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					if (id == -1) {
						JOptionPane.showMessageDialog(frame, "User does not exist.", "Warning", JOptionPane.WARNING_MESSAGE);
					}
					else if(id == 0) {
						JOptionPane.showMessageDialog(frame, "Incorrect password, please try again.", "Warning", JOptionPane.WARNING_MESSAGE);
					}
					else {
						mainpage mainPage = new mainpage();
						try {
							mainPage.mainPageView(id);
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						frame.dispose();
					}
				}
			}
		});
		
		JButton signUp = new JButton("SIGN UP");
		signUp.setBounds(202, 250, 148, 38);
		signUp.setFont(BUTTON_FONT);
		signUp.setBackground(new Color(238, 242, 255));
		signUp.setForeground(PRIMARY);
		signUp.setFocusPainted(false);
		signUp.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
		signUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
		card.add(signUp);
		signUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				signup signup = new signup();
				signup.signUpView();
			}
		});

		JButton attend = new JButton("Complete Quiz as Guest");
		attend.setBounds(40, 308, 310, 38);
		attend.setFont(BUTTON_FONT);
		attend.setBackground(BG_COLOR);
		attend.setForeground(new Color(107, 114, 128));
		attend.setFocusPainted(false);
		attend.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
		attend.setCursor(new Cursor(Cursor.HAND_CURSOR));
		card.add(attend);
		attend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String surveyCode = JOptionPane.showInputDialog(frame, "Enter the 5-character Quiz Code:");
				try {
					if(surveyCode != null && !surveyCode.isEmpty() && surveyCode.length() == 5) {
						if(manage.check(surveyCode)) {
							guest guest = new guest();
							guest.guestView(surveyCode);
						}
						else {
							JOptionPane.showMessageDialog(frame, "Invalid quiz code, please try again.", "Warning", JOptionPane.WARNING_MESSAGE);
						}
					}
					else if (surveyCode != null) {
						JOptionPane.showMessageDialog(frame, "Quiz code must be exactly 5 characters.", "Warning", JOptionPane.WARNING_MESSAGE);
					}
				}
				catch(Exception e2) {
					e2.printStackTrace();
				}
			}
		});
		
		frame.setVisible(true);
	}
}
