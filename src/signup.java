import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class signup {

	public void signUpView() {
		final JFrame frame = AppTheme.createFrame("Create Account", 560, 620, JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);
		frame.setLayout(new GridBagLayout());

		JPanel shell = new JPanel();
		shell.setOpaque(false);
		shell.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		shell.setLayout(new BoxLayout(shell, BoxLayout.Y_AXIS));

		JPanel card = AppTheme.createCard(new GridBagLayout(), 30);
		card.setAlignmentX(Component.CENTER_ALIGNMENT);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 8, 0);

		JLabel title = AppTheme.createTitleLabel("Create your account");
		card.add(title, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 24, 0);
		card.add(AppTheme.createMutedLabel("Use a unique username so guests can find your quizzes later."), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 6, 0);
		card.add(createFieldLabel("Full name"), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 14, 0);
		final JTextField fullNameField = AppTheme.createTextField(24);
		card.add(fullNameField, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 6, 0);
		card.add(createFieldLabel("Username"), gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 14, 0);
		final JTextField usernameField = AppTheme.createTextField(24);
		card.add(usernameField, gbc);

		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 0, 6, 10);
		card.add(createFieldLabel("Password"), gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(0, 10, 6, 0);
		card.add(createFieldLabel("Confirm password"), gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.insets = new Insets(0, 0, 20, 10);
		final JPasswordField passwordField = AppTheme.createPasswordField(12);
		card.add(passwordField, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(0, 10, 20, 0);
		final JPasswordField confirmPasswordField = AppTheme.createPasswordField(12);
		card.add(confirmPasswordField, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		javax.swing.JButton createButton = AppTheme.createPrimaryButton("Create Account");
		card.add(createButton, gbc);

		shell.add(card);
		frame.add(shell);
		frame.getRootPane().setDefaultButton(createButton);

		createButton.addActionListener(e -> handleSignup(frame, fullNameField, usernameField, passwordField, confirmPasswordField));

		frame.setVisible(true);
	}

	private void handleSignup(JFrame frame, JTextField fullNameField, JTextField usernameField, JPasswordField passwordField,
			JPasswordField confirmPasswordField) {
		String fullName = fullNameField.getText().trim();
		String username = usernameField.getText().trim();
		String password = new String(passwordField.getPassword());
		String confirmPassword = new String(confirmPasswordField.getPassword());

		if (fullName.isEmpty() || username.isEmpty() || password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Fill in every field before creating the account.", "Missing Information", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (username.contains(" ")) {
			JOptionPane.showMessageDialog(frame, "Usernames cannot contain spaces.", "Invalid Username", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (password.length() < 6) {
			JOptionPane.showMessageDialog(frame, "Passwords should be at least 6 characters long.", "Weak Password", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (!password.equals(confirmPassword)) {
			JOptionPane.showMessageDialog(frame, "The passwords do not match.", "Password Mismatch", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try (DbOperations manage = DbFactory.create()) {
			if (manage.checkUsername(username)) {
				JOptionPane.showMessageDialog(frame, "That username is already in use.", "Username Taken", JOptionPane.WARNING_MESSAGE);
				return;
			}

			manage.newUser(fullName, username, password);
			JOptionPane.showMessageDialog(frame, "Account created. You can now sign in.", "Success", JOptionPane.INFORMATION_MESSAGE);
			frame.dispose();
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(frame, ex.getMessage(), "Invalid Information", JOptionPane.WARNING_MESSAGE);
		} catch (Exception ex) {
			String message = "Unable to create the account. An error occurred.\n\n" + ex.getMessage();
			JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private JLabel createFieldLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(AppTheme.LABEL_FONT);
		label.setForeground(AppTheme.TEXT_SECONDARY);
		return label;
	}
}