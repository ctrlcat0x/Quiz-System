import java.sql.SQLException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class runner {
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fall back to default L&F
            }
            try {
                login login = new login();
                login.loginView();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}