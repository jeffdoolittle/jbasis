package jbasis.ioc;

import java.util.function.Function;

/**
 * A collection of service factories by service type
 */
public interface ServiceCollection extends Iterable<ServiceDescriptor> {

  public <T> void addSingleton(Class<T> serviceType, Function<Container, T> factory);

  public <T> void addScoped(Class<T> serviceType, Function<Container, T> factory);

  public <T> void addTransient(Class<T> serviceType, Function<Container, T> factory);
}
