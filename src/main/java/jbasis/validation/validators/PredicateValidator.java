package jbasis.validation.validators;

import java.util.function.BiFunction;

import jbasis.ioc.ServiceFactory;
import jbasis.validation.ValidationContext;
import jbasis.validation.ValidationResult;
import jbasis.validation.Validator;

public class PredicateValidator<T> implements Validator {

  private BiFunction<T, ServiceFactory, String> predicate;

  public PredicateValidator(BiFunction<T, ServiceFactory, String> predicate) {
    this.predicate = predicate;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ValidationResult validate(ValidationContext context) {
    var target = (T) context.target();
    var message = predicate.apply(target, context.serviceFactory());

    if (message == null || message.length() == 0) {
      return ValidationResult.OK;
    }
    return new ValidationResult(message);
  }
}