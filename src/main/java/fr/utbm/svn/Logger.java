package fr.utbm.svn;

public class Logger {

    private static volatile Logger instance = null;
    private static boolean DEBUG = false;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";

    private Logger() {}

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

    public void log(String msg) {
        if (DEBUG) {
            System.out.println("[SVN] " + msg);
        }
    }

    public void warn(String msg) {
        if (DEBUG) {
            System.out.println(ANSI_YELLOW + "[SVN][WARN] " + msg + ANSI_RESET);
        }
    }

    public void error(String msg) {
        System.err.println(ANSI_RED + "[SVN][ERROR] " + msg + ANSI_RESET);
    }

    public void setDebug(Boolean isDebug) {
        DEBUG = isDebug;
    }
}