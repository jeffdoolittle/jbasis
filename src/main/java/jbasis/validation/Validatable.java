package jbasis.validation;

public interface Validatable {
  Iterable<ValidationResult> validate(ValidationContext context);
}