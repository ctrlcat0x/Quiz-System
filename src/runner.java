import javax.swing.SwingUtilities;

public class runner {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				AppTheme.applyLookAndFeel();
				new login().loginView();
			}
		});
	}
}