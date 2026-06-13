package fr.utbm.svn;

/**
 * Thread-safe singleton logger for the SVN plugin.
 *
 * <p>Provides colored console output for three severity levels: info, warning, and error.
 * Info and warning messages are gated behind a {@code DEBUG} flag and are suppressed in
 * production. Error messages are always printed to {@code stderr}.</p>
 */
public class Logger {

    private static volatile Logger instance = null;
    private static boolean DEBUG = false;

    private static final String ANSI_RESET = "[0m";
    private static final String ANSI_YELLOW = "[33m";
    private static final String ANSI_RED = "[31m";

    private Logger() {}

    /**
     * Returns the singleton instance, creating it on first call (double-checked locking).
     *
     * @return the unique {@code Logger} instance
     */
    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    /**
     * Prints an info-level message to {@code stdout}.
     * No-op when debug mode is disabled.
     *
     * @param msg the message to print
     */
    public void log(String msg) {
        if (DEBUG) {
            System.out.println("[SVN] " + msg);
        }
    }

    /**
     * Prints a warning-level message to {@code stdout} in yellow.
     * No-op when debug mode is disabled.
     *
     * @param msg the message to print
     */
    public void warn(String msg) {
        if (DEBUG) {
            System.out.println(ANSI_YELLOW + "[SVN][WARN] " + msg + ANSI_RESET);
        }
    }

    /**
     * Prints an error-level message to {@code stderr} in red.
     * Always printed regardless of debug mode.
     *
     * @param msg the message to print
     */
    public void error(String msg) {
        System.err.println(ANSI_RED + "[SVN][ERROR] " + msg + ANSI_RESET);
    }

    /**
     * Enables or disables debug logging.
     *
     * @param isDebug {@code true} to enable info/warning output, {@code false} to suppress it
     */
    public void setDebug(Boolean isDebug) {
        DEBUG = isDebug;
    }
}
