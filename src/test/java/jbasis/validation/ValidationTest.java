package jbasis.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

import jbasis.validation.validators.StringLength;

public class ValidationTest {

  @Test
  public void first() {
    assertTrue(true);
  }

  public class Target implements Validatable {
    private String value;

    @StringLength(min = 1, max = 20)
    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    @Override
    public Iterable<ValidationResult> validate(ValidationContext context) {
      // ResourceBundle bundle = ResourceBundle.getBundle("", Locale.US);

      // TODO Auto-generated method stub
      return null;
    }
  }

  interface Validator<T> {
    Iterable<ValidationResult> Validate(ValidationContext context);
  }

  abstract class ValidatorBase<T> implements Validator<T> {

    @Override
    public Iterable<ValidationResult> Validate(ValidationContext context) {
      // TODO Auto-generated method stub
      return null;
    }

  }

  class TargetValidator extends ValidatorBase<Target> {

  }
}