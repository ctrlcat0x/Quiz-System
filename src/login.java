import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class login {

	public void loginView() {
		final JFrame frame = AppTheme.createFrame("Quiz Studio", 520, 580, JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLayout(new java.awt.GridBagLayout());

		JPanel loginCard = AppTheme.createCard(new GridBagLayout(), 36);
		loginCard.setPreferredSize(new Dimension(420, 490));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 20, 0);

		JLabel badge = AppTheme.createBadgeLabel("Quiz Studio", AppTheme.PRIMARY_SOFT, AppTheme.PRIMARY);
		loginCard.add(badge, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(16, 0, 6, 0);
		JLabel title = AppTheme.createTitleLabel("Welcome back");
		loginCard.add(title, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 28, 0);
		loginCard.add(AppTheme.createMutedLabel("Sign in to your account to continue."), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 6, 0);
		loginCard.add(createFieldLabel("Username"), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 16, 0);
		final JTextField usernameField = AppTheme.createTextField(20);
		loginCard.add(usernameField, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 6, 0);
		loginCard.add(createFieldLabel("Password"), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 22, 0);
		final JPasswordField passwordField = AppTheme.createPasswordField(20);
		loginCard.add(passwordField, gbc);

		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 0, 10, 8);
		final JButton loginButton = AppTheme.createPrimaryButton("Sign In");
		loginCard.add(loginButton, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(0, 8, 10, 0);
		JButton signupButton = AppTheme.createSecondaryButton("Create Account");
		loginCard.add(signupButton, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		JButton guestButton = AppTheme.createSecondaryButton("Continue as Guest");
		loginCard.add(guestButton, gbc);

		frame.add(loginCard);
		frame.getRootPane().setDefaultButton(loginButton);

		loginButton.addActionListener(e -> handleLogin(frame, usernameField, passwordField));
		signupButton.addActionListener(e -> new signup().signUpView());
		guestButton.addActionListener(e -> handleGuestAccess(frame));

		frame.setVisible(true);
	}

	private void handleLogin(JFrame frame, JTextField usernameField, JPasswordField passwordField) {
		String username = usernameField.getText().trim();
		String password = new String(passwordField.getPassword());

		if (username.isEmpty() || password.trim().isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Enter both username and password.", "Missing Information", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try (SQLoperations manage = new SQLoperations()) {
			int authResult = manage.authUser(username, password);
			if (authResult == -1) {
				JOptionPane.showMessageDialog(frame, "No account was found for that username.", "Sign In Failed", JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (authResult == 0) {
				JOptionPane.showMessageDialog(frame, "The password is incorrect.", "Sign In Failed", JOptionPane.WARNING_MESSAGE);
				return;
			}

			new mainpage().mainPageView(authResult);
			frame.dispose();
		} catch (SQLException ex) {
			showDatabaseError(frame, "sign in", ex);
		}
	}

	private void handleGuestAccess(JFrame frame) {
		JTextField codeField = AppTheme.createTextField(10);
		codeField.setHorizontalAlignment(SwingConstants.CENTER);

		JPanel prompt = new JPanel(new BorderLayout(0, 10));
		prompt.setBackground(AppTheme.BACKGROUND);
		prompt.add(AppTheme.createBodyLabel("Enter the 5-character quiz code."), BorderLayout.NORTH);
		prompt.add(codeField, BorderLayout.CENTER);

		int result = JOptionPane.showConfirmDialog(frame, prompt, "Open Quiz", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		String surveyCode = codeField.getText().trim().toUpperCase();
		if (surveyCode.length() != 5) {
			JOptionPane.showMessageDialog(frame, "Quiz codes must be exactly 5 characters.", "Invalid Code", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try (SQLoperations manage = new SQLoperations()) {
			if (!manage.check(surveyCode)) {
				JOptionPane.showMessageDialog(frame, "That quiz code does not exist.", "Invalid Code", JOptionPane.WARNING_MESSAGE);
				return;
			}

			new guest().guestView(surveyCode);
		} catch (SQLException ex) {
			showDatabaseError(frame, "open the quiz", ex);
		}
	}

	private JLabel createFieldLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(AppTheme.LABEL_FONT);
		label.setForeground(AppTheme.TEXT_SECONDARY);
		return label;
	}

	private void showDatabaseError(Component parent, String action, SQLException ex) {
		String message = "Unable to " + action + ". Check your database settings and make sure MySQL is running.\n\n" + ex.getMessage();
		JOptionPane.showMessageDialog(parent, message, "Database Error", JOptionPane.ERROR_MESSAGE);
	}
}