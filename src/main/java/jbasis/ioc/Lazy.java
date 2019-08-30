package jbasis.ioc;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Lazy will memoize a Supplier function so that the 
 * Supplier is executed no more than once and, if 
 * executed, will return the same value on all sub-
 * sequent calls to get().
 */
public final class Lazy<T> implements Supplier<T> {
  private Object lock = new Object();
  private Optional<T> instance = Optional.empty();
  private Supplier<T> supplier;

  public Lazy(Supplier<T> theSupplier) {
    supplier = theSupplier;
  }

  @Override
  public final T get() {
    synchronized (lock) {
      if (!instance.isPresent()) {
        instance = Optional.of(supplier.get());
      }

      return instance.get();
    }
  }

  public final boolean isPresent() {
    return instance.isPresent();
  }
}
