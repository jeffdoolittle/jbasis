package jbasis.ioc;

import java.util.function.Function;

/**
 * A collection of service factories by service type
 */
public interface ServiceCollection extends Iterable<ServiceDescriptor> {

  public <S, I extends S> void add(Class<S> serviceType, ServiceLifetime lifetime, 
      Function<ServiceFactory, I> factory);

  public <S, I extends S> void add(Class<S> serviceType, 
      ServiceLifetime lifetime, Class<I> implementationType);
}
