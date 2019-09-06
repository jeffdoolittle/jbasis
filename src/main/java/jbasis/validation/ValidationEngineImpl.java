package jbasis.validation;

import java.util.ArrayList;
import java.util.List;

import jbasis.ioc.ServiceFactory;

public class ValidationEngineImpl implements ValidationEngine {

  private ServiceFactory serviceFactory;

  public ValidationEngineImpl(ServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  @Override
  public Iterable<ValidationResult> validate(Object target) {
    var context = new ValidationContext(target, this.serviceFactory);
    var results = new ArrayList<ValidationResult>();
    validate(context, results);
    return results;
  }

  private void validate(ValidationContext context, List<ValidationResult> results) {

  }
}