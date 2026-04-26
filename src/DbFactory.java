public class DbFactory {

	private static boolean fallbackMode = false;

	public static void setFallbackMode(boolean enabled) {
		fallbackMode = enabled;
	}

	public static boolean isFallbackMode() {
		return fallbackMode;
	}

	public static DbOperations create() throws Exception {
		if (fallbackMode) {
			return new JSONDatabaseOperations();
		}
		return new SQLoperations();
	}
}
