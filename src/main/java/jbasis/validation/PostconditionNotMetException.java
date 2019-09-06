package jbasis.validation;

import jbasis.util.JBasisException;

/**
 * An exception that is thrown when validation postconditions 
 * are not met.
 */
public class PostconditionNotMetException extends JBasisException {
  private static final long serialVersionUID = 1L;

  public PostconditionNotMetException(String message) {
    super(message);
  }

  public PostconditionNotMetException(String message, Throwable cause) {
    super(message, cause);
  }
}