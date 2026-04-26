import javax.swing.SwingUtilities;

public class fallback_runner {

public static void main(String[] args) {
DbFactory.setFallbackMode(true);

SwingUtilities.invokeLater(new Runnable() {
@Override
public void run() {
try {
AppTheme.applyLookAndFeel();

try (DbOperations dbOps = DbFactory.create()) {
if (!dbOps.checkUsername("demo")) {
dbOps.newUser("Demo User", "demo", "demo123");
}
} catch (Exception ex) {
// Demo account may already exist
}

new login().loginView();

} catch (Exception ex) {
System.err.println("Error starting application: " + ex.getMessage());
ex.printStackTrace();
System.exit(1);
}
}
});
}
}