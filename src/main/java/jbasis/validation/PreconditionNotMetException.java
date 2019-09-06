package jbasis.validation;

import jbasis.util.JBasisException;

/**
 * An exception that is thrown when validation preconditions 
 * are not met.
 */
public class PreconditionNotMetException extends JBasisException {
  private static final long serialVersionUID = 1L;

  public PreconditionNotMetException(String message) {
    super(message);
  }

  public PreconditionNotMetException(String message, Throwable cause) {
    super(message, cause);
  }
}