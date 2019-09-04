package jbasis.ioc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

import jbasis.logging.Logger;
import jbasis.logging.LoggerFactory;

/**
 * Default implementation of ServiceCollection
 */
public final class ServiceCollectionImpl implements ServiceCollection {

  private Logger logger = LoggerFactory.get(getClass());
  private Map<String, ServiceDescriptor> services;

  public ServiceCollectionImpl() {
    this.services = new HashMap<>();
  }

  @Override
  public <S, I extends S> void add(Class<S> serviceType, ServiceLifetime lifetime, 
      Function<Container, I> factory) {
    addDescriptor(serviceType, lifetime, factory);
  }

  @Override
  public <S, I extends S> void add(Class<S> serviceType, ServiceLifetime lifetime, 
      Class<I> implementationType) {
    addDescriptor(serviceType, lifetime, implementationType);
  }

  /**
   * Adds a service descriptor to the collection.

   * @param <S> the type of service
   * @param <I> the type of implementation
   * @param serviceType the service class type
   * @param lifetime the service lifecycle
   * @param factory the factory for creating a service instance
   */
  public <S, I extends S> void addDescriptor(Class<S> serviceType, ServiceLifetime lifetime, 
      Function<Container, I> factory) {
    String serviceName = serviceType.getCanonicalName();
    if(serviceAlreadyRegistered(serviceName)) {
      logger.warn("{} already registered. Replacing with updated instance", serviceName);
    }
    ServiceDescriptor sd = ServiceDescriptor.init(serviceType, lifetime, factory);
    services.put(serviceName, sd);
  }

  /**
   * Adds a service descriptor to the collection.

   * @param <S> the type of service
   * @param <I> the type of implementation
   * @param serviceType the service class type
   * @param lifetime the service lifecycle
   * @param implementationType the implementation class type
   */
  public <S, I extends S> void addDescriptor(Class<S> serviceType, ServiceLifetime lifetime, 
      Class<I> implementationType) {
    String serviceName = serviceType.getCanonicalName();
    if(serviceAlreadyRegistered(serviceName)) {
      logger.warn("{} already registered. Replacing with updated instance", serviceName);
    }
    ServiceDescriptor sd = ServiceDescriptor.init(serviceType, lifetime, implementationType);
    services.put(serviceName, sd);
  }

  private boolean serviceAlreadyRegistered(String serviceName) {
    return services.containsKey(serviceName);
  }

  @Override
  public Iterator<ServiceDescriptor> iterator() {
    return services.values().iterator();
  }

  @Override
  public void forEach(Consumer<? super ServiceDescriptor> action) {
    services.values().forEach(action);
  }

  @Override
  public Spliterator<ServiceDescriptor> spliterator() {
    return services.values().spliterator();
  }
}
