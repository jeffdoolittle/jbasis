package jbasis.validation;

public interface ValidationEngine {
  Iterable<ValidationResult> validate(Object target);
}
