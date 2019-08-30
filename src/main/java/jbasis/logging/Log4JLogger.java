package jbasis.logging;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 * Logger facade implementation for log4j
 */
public class Log4JLogger implements jbasis.logging.Logger {
  private Logger logger;

  public Log4JLogger(Class<?> cls) {
    this.logger = LogManager.getLogger(cls);
  }

  @Override
  public boolean isDebugEnabled() {
    return this.logger.isDebugEnabled();
  }

  @Override
  public void log(jbasis.logging.Level level, String format, Object... args) {
    Level log4jlevel = Level.getLevel(level.name());
    this.logger.log(log4jlevel, format, args);
  }
}
