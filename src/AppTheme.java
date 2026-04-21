import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.Border;

public final class AppTheme {

	// ── Color tokens — clean minimal with indigo accent ─────────────────────
	public static final Color BACKGROUND    = new Color(248, 250, 252);
	public static final Color SURFACE       = Color.WHITE;
	public static final Color SURFACE_ALT   = new Color(241, 245, 249);
	public static final Color PRIMARY       = new Color(79, 70, 229);
	public static final Color PRIMARY_DARK  = new Color(55, 48, 163);
	public static final Color PRIMARY_SOFT  = new Color(238, 242, 255);
	public static final Color TEXT          = new Color(15, 23, 42);
	public static final Color TEXT_SECONDARY = new Color(51, 65, 85);
	public static final Color MUTED_TEXT    = new Color(100, 116, 139);
	public static final Color BORDER        = new Color(226, 232, 240);
	public static final Color BORDER_STRONG = new Color(203, 213, 225);
	public static final Color SUCCESS       = new Color(22, 163, 74);
	public static final Color DANGER        = new Color(220, 38, 38);
	public static final Color WARNING       = new Color(217, 119, 6);

	// ── Typography scale (8pt grid, modular) ─────────────────────────────────
	public static final Font TITLE_FONT    = new Font("SansSerif", Font.BOLD, 28);
	public static final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 14);
	public static final Font SECTION_FONT  = new Font("SansSerif", Font.BOLD, 17);
	public static final Font LABEL_FONT    = new Font("SansSerif", Font.BOLD, 12);
	public static final Font BODY_FONT     = new Font("SansSerif", Font.PLAIN, 14);
	public static final Font SMALL_FONT    = new Font("SansSerif", Font.PLAIN, 12);
	public static final Font BUTTON_FONT   = new Font("SansSerif", Font.BOLD, 13);

	private AppTheme() {
	}

	public static void applyLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			// Use the default look and feel if the system look and feel cannot be applied.
		}

		UIManager.put("OptionPane.background", BACKGROUND);
		UIManager.put("Panel.background", BACKGROUND);
		UIManager.put("OptionPane.messageForeground", TEXT);
		UIManager.put("Button.disabledText", MUTED_TEXT);
		UIManager.put("Label.disabledForeground", MUTED_TEXT);
	}

	public static JFrame createFrame(String title, int width, int height, int closeOperation) {
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(closeOperation);
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setBackground(BACKGROUND);
		return frame;
	}

	public static JPanel createCard(LayoutManager layout, int padding) {
		JPanel panel = new JPanel(layout);
		panel.setBackground(SURFACE);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER, 1),
			BorderFactory.createEmptyBorder(padding, padding, padding, padding)
		));
		return panel;
	}

	public static JLabel createTitleLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(TITLE_FONT);
		label.setForeground(TEXT);
		return label;
	}

	public static JLabel createSectionLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(SECTION_FONT);
		label.setForeground(TEXT);
		return label;
	}

	public static JLabel createBodyLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(BODY_FONT);
		label.setForeground(TEXT);
		return label;
	}

	public static JLabel createMutedLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(SUBTITLE_FONT);
		label.setForeground(TEXT_SECONDARY);
		return label;
	}

	public static JLabel createBadgeLabel(String text, Color background, Color foreground) {
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setOpaque(true);
		label.setBackground(background);
		label.setForeground(foreground);
		label.setFont(LABEL_FONT);
		label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		return label;
	}

	public static JTextField createTextField(int columns) {
		JTextField field = new JTextField(columns);
		field.setFont(BODY_FONT);
		field.setBackground(SURFACE);
		field.setForeground(TEXT);
		field.setCaretColor(PRIMARY);
		field.setBorder(createInputBorder());
		field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		return field;
	}

	public static JPasswordField createPasswordField(int columns) {
		JPasswordField field = new JPasswordField(columns);
		field.setFont(BODY_FONT);
		field.setBackground(SURFACE);
		field.setForeground(TEXT);
		field.setCaretColor(PRIMARY);
		field.setBorder(createInputBorder());
		field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		return field;
	}

	public static JTextArea createTextArea(int rows, int columns, boolean editable) {
		JTextArea area = new JTextArea(rows, columns);
		area.setEditable(editable);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setFont(BODY_FONT);
		area.setForeground(TEXT);
		area.setCaretColor(PRIMARY);
		area.setBackground(editable ? SURFACE : SURFACE_ALT);
		area.setBorder(editable ? createInputBorder() : BorderFactory.createEmptyBorder(10, 12, 10, 12));
		return area;
	}

	public static JButton createPrimaryButton(String text) {
		return createButton(text, PRIMARY, Color.WHITE, BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(PRIMARY_DARK, 1),
			BorderFactory.createEmptyBorder(9, 19, 9, 19)
		));
	}

	public static JButton createSecondaryButton(String text) {
		return createButton(text, SURFACE, TEXT_SECONDARY, BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_STRONG, 1),
			BorderFactory.createEmptyBorder(9, 19, 9, 19)
		));
	}

	public static JButton createDangerButton(String text) {
		return createButton(text, new Color(254, 242, 242), DANGER, BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(252, 165, 165), 1),
			BorderFactory.createEmptyBorder(9, 19, 9, 19)
		));
	}

	public static JButton createSidebarButton(String text, boolean active) {
		JButton button = new JButton(text);
		button.setFont(active ? new Font("SansSerif", Font.BOLD, 14) : BODY_FONT);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(active);
		button.setOpaque(active);
		button.setBackground(PRIMARY_SOFT);
		button.setForeground(active ? PRIMARY : TEXT_SECONDARY);
		button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return button;
	}

	public static void setSidebarButtonState(JButton button, boolean active) {
		button.setFont(active ? new Font("SansSerif", Font.BOLD, 14) : BODY_FONT);
		button.setContentAreaFilled(active);
		button.setOpaque(active);
		button.setForeground(active ? PRIMARY : TEXT_SECONDARY);
		button.repaint();
	}

	public static void copyToClipboard(String text) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
	}

	public static String html(String text, int width) {
		return "<html><div style='width:" + width + "px;'>" + escapeHtml(text) + "</div></html>";
	}

	public static void setPanelOpaque(Component component, boolean opaque) {
		if (component instanceof JPanel) {
			((JPanel) component).setOpaque(opaque);
		}
	}

	private static JButton createButton(String text, Color background, Color foreground, Border border) {
		JButton button = new JButton(text);
		button.setUI(new BasicButtonUI());
		button.setFont(BUTTON_FONT);
		button.setBackground(background);
		button.setForeground(foreground);
		button.setFocusPainted(false);
		button.setContentAreaFilled(true);
		button.setBorderPainted(true);
		button.setBorder(border);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setOpaque(true);
		return button;
	}

	private static Border createInputBorder() {
		return BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_STRONG, 1),
			BorderFactory.createEmptyBorder(9, 12, 9, 12)
		);
	}

	private static String escapeHtml(String text) {
		if (text == null) {
			return "";
		}

		return text
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\n", "<br>");
	}
}