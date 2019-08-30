package jbasis.logging;

/**
 * Logger facade
 */
public interface Logger {
  /**
   * @return true if debugging is enabled
   */
  boolean isDebugEnabled();

  /**
   * writes out a log message for the specified Level
   * 
   * @param level the logging level
   * @param format a log format string
   * @param args passed in to format string
   */
  void log(Level level, String format, Object... args);

  default void debug(String format, Object... args) {
    log(Level.DEBUG, format, args);
  }
  
  default void info(String format, Object... args) {
    log(Level.INFO, format, args);
  }
  
  default void warn(String format, Object... args) {
    log(Level.WARN, format, args);
  }
  
  default void error(String format, Object... args) {
    log(Level.ERROR, format, args);
  }
}
