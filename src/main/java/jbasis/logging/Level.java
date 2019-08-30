package jbasis.logging;

/**
 * Logging level definitions
 */
public enum Level {
  TRACE {
    @Override
    public String abbreviation() {
      return "TRC";
    }
  },
  DEBUG {
    @Override
    public String abbreviation() {
      return "DBG";
    }
  },
  INFO {
    @Override
    public String abbreviation() {
      return "INF";
    }
  },
  WARN {
    @Override
    public String abbreviation() {
      return "WRN";
    }
  },
  ERROR {
    @Override
    public String abbreviation() {
      return "ERR";
    }
  };

  public abstract String abbreviation();
}