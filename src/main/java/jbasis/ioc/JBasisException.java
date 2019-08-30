package jbasis.ioc;

/**
 * Exception type for all jbasis exceptions.
 * <p>
 * Note that this is a RuntimeException. Checked exceptions 
 * have their place but can lead to bloated exception 
 * handling that is better addressed with test coverage.
 * <p>
 * Let the exceptions bubble up and deal with them at the 
 * edges.
 */
public class JBasisException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public JBasisException(String message) {
    super(message);
  }

  public JBasisException(String message, Throwable cause) {
    super(message, cause);
  }
}