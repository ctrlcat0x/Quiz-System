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

public class signup {

	private static final Color PRIMARY = new Color(99, 102, 241);
	private static final Color BG_COLOR = new Color(249, 250, 251);
	private static final Color CARD_COLOR = Color.WHITE;
	private static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 26);
	private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);
	private static final Font FIELD_FONT = new Font("SansSerif", Font.PLAIN, 14);
	private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);

	public void signUpView() {
		JFrame frame = new JFrame("Quiz System - Sign Up");
		frame.setSize(460, 480);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setBackground(BG_COLOR);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

		JPanel card = new JPanel();
		card.setBounds(30, 20, 390, 410);
		card.setLayout(null);
		card.setBackground(CARD_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
			BorderFactory.createEmptyBorder(20, 20, 20, 20)
		));
		frame.add(card);

		JLabel heading = new JLabel("Create Account");
		heading.setBounds(0, 15, 390, 35);
		heading.setHorizontalAlignment(JLabel.CENTER);
		heading.setFont(HEADING_FONT);
		heading.setForeground(PRIMARY);
		card.add(heading);

		JLabel fName = new JLabel("Full Name");
		fName.setBounds(35, 70, 150, 20);
		fName.setFont(LABEL_FONT);
		fName.setForeground(new Color(75, 85, 99));
		card.add(fName);

		JTextField fNameField = new JTextField();
		fNameField.setBounds(35, 92, 320, 32);
		fNameField.setFont(FIELD_FONT);
		fNameField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		card.add(fNameField);
		
		JLabel uName = new JLabel("Username");
		uName.setBounds(35, 132, 150, 20);
		uName.setFont(LABEL_FONT);
		uName.setForeground(new Color(75, 85, 99));
		card.add(uName);
		
		JTextField uNameField = new JTextField();
		uNameField.setBounds(35, 154, 320, 32);
		uNameField.setFont(FIELD_FONT);
		uNameField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		card.add(uNameField);
		
		JLabel uPass = new JLabel("Password");
		uPass.setBounds(35, 194, 150, 20);
		uPass.setFont(LABEL_FONT);
		uPass.setForeground(new Color(75, 85, 99));
		card.add(uPass);
		
		JPasswordField uPassField = new JPasswordField();
		uPassField.setBounds(35, 216, 150, 32);
		uPassField.setFont(FIELD_FONT);
		uPassField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		card.add(uPassField);
		
		JLabel uPass2 = new JLabel("Confirm Password");
		uPass2.setBounds(205, 194, 150, 20);
		uPass2.setFont(LABEL_FONT);
		uPass2.setForeground(new Color(75, 85, 99));
		card.add(uPass2);
		
		JPasswordField uPassField2 = new JPasswordField();
		uPassField2.setBounds(205, 216, 150, 32);
		uPassField2.setFont(FIELD_FONT);
		uPassField2.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
			BorderFactory.createEmptyBorder(4, 10, 4, 10)
		));
		card.add(uPassField2);
		
		JButton submit = new JButton("CREATE ACCOUNT");
		submit.setBounds(35, 275, 320, 38);
		submit.setFont(BUTTON_FONT);
		submit.setBackground(PRIMARY);
		submit.setForeground(Color.WHITE);
		submit.setFocusPainted(false);
		submit.setBorderPainted(false);
		submit.setCursor(new Cursor(Cursor.HAND_CURSOR));
		card.add(submit);
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String fname = fNameField.getText();
				String uname = uNameField.getText();
				String pass1 = new String(uPassField.getPassword());
				String pass2 = new String(uPassField2.getPassword());
				if(fname.isEmpty() || uname.isEmpty() || pass1.isEmpty()|| pass2.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please enter all information.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
				else {
					if(pass1.equals(pass2)) {
						try {
							SQLoperations manage = new SQLoperations();
							if (manage.checkUsername(uname)) {
								JOptionPane.showMessageDialog(frame, "Username already taken. Please choose another.", "Warning", JOptionPane.WARNING_MESSAGE);
								return;
							}
							manage.newUser(fname, uname, pass1);
							fNameField.setText("");
							uNameField.setText("");
							uPassField.setText("");
							uPassField2.setText("");
							JOptionPane.showMessageDialog(frame, "Account created successfully!", "Success", JOptionPane.PLAIN_MESSAGE);
							frame.dispose();
							
						} catch (SQLException e1) {
							JOptionPane.showMessageDialog(frame, "Please try again.", "Warning", JOptionPane.WARNING_MESSAGE);
						}
						
					}
					else {
						JOptionPane.showMessageDialog(frame, "Passwords do not match.", "Warning", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		
		frame.setVisible(true);
    }
}

