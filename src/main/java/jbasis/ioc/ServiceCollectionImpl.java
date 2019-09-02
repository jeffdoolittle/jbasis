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
  public <T> void addSingleton(Class<T> serviceType, Function<Container, T> factory) {
    addDescriptor(ServiceLifetime.SINGLETON, serviceType, factory);
  }

  @Override
  public <T> void addScoped(Class<T> serviceType, Function<Container, T> factory) {
    addDescriptor(ServiceLifetime.SCOPED, serviceType, factory);
  }

  @Override
  public <T> void addTransient(Class<T> serviceType, Function<Container, T> factory) {
    addDescriptor(ServiceLifetime.TRANSIENT, serviceType, factory);
  }

  /**
   * Adds a service descriptor to the collection.

   * @param <T> the service type
   * @param lifetime the service lifecycle
   * @param serviceType the service type class
   * @param factory the factory for creating a service instance
   */
  public <T> void addDescriptor(ServiceLifetime lifetime, Class<T> serviceType,
      Function<Container, T> factory) {
    String serviceName = serviceType.getCanonicalName();
    if(serviceAlreadyRegistered(serviceName)) {
      logger.warn("{} already registered. Replacing with updated instance", serviceName);
    }
    ServiceDescriptor sd = ServiceDescriptor.init(serviceType, lifetime, factory);
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
