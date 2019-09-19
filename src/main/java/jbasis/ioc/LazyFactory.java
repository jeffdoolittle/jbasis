package jbasis.ioc;

import java.util.Optional;
import java.util.function.Function;

/**
 * Lazy will memoize a Supplier function so that the Supplier 
 * is executed no more than once and, if executed, will return 
 * the same value on all subsequent calls to get().
 */
public final class LazyFactory<T> implements Function<ServiceFactory, T> {
  private Object lock = new Object();
  private Optional<T> instance = Optional.empty();
  private Function<ServiceFactory, T> function;

  public LazyFactory(Function<ServiceFactory, T> theFunction) {
    function = theFunction;
  }

  @Override
  public final T apply(ServiceFactory sf) {
    synchronized (lock) {
      if (!instance.isPresent()) {
        instance = Optional.of(function.apply(sf));
      }

      return instance.get();
    }
  }

  public final boolean isPresent() {
    return instance.isPresent();
  }
}
