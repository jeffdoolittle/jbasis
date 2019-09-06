package jbasis.validation.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jbasis.validation.ValidationContext;
import jbasis.validation.ValidationResult;
import jbasis.validation.Validator;

/**
 * String length validator
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@WithValidator(validator = StringLengthValidator.class)
public @interface StringLength {
  int min();
  int max();

  /**
   * %1 is the member name
   * %2 is the min length
   * %3 is the max length
   * 
   * @return the error message format string
   */
  String errorMessage() default "%1$s must be between %2$s and %3$s characters in length. ";
}

class StringLengthValidator implements Validator {

  @Override
  public ValidationResult validate(ValidationContext context) {
    return ValidationResult.NONE;
  }
}
