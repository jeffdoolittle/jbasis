package jbasis.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
  public static final ValidationResult OK = new ValidationResult(null);

  private String message;
  private Iterable<String> memberNames;

  public ValidationResult(String message) {
    this.message = message;
    this.memberNames = new ArrayList<>();
  }

  public ValidationResult(String message, Iterable<String> memberNames) {
    this.message = message;
    List<String> members = new ArrayList<String>();
    memberNames.forEach(members::add);
    this.memberNames = members;    
  }

  public boolean isOk() {
    return message == null || message.length() == 0;
  }

  public String message() {
    return this.message;
  }

  public Iterable<String> memberNames() {
    return this.memberNames;
  }
}