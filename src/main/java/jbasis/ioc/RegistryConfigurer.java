package jbasis.ioc;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import jbasis.logging.Logger;

/**
 * DSL entry point for registry configuration
 */
public interface RegistryConfigurer {

  /**
   * Adds a service factory with the SINGLETON lifecycle to the 
   * registry configuration.
   * 
   * @param <T> the type of service
   * @param serviceType the service class type
   * @param factory the factory for creating the service instance
   * @return the service instance
   */
  public <T> RegistryConfigurer addSingleton(Class<T> serviceType,
      Function<Container, T> factory);

  /**
   * Adds a service factory with the SCOPED lifecycle to the 
   * registry configuration.
   * 
   * @param <T> the type of service
   * @param serviceType the service class type
   * @param factory the factory for creating the service instance
   * @return the service instance
   */
  public <T> RegistryConfigurer addScoped(Class<T> serviceType,
      Function<Container, T> factory);

  /**
   * Adds a service factory with the TRANSIENT lifecycle to the 
   * registry configuration.
   * 
   * @param <T> the type of service
   * @param serviceType the service class type
   * @param factory the factory for creating a service instance
   * @return a service instance
   */
  public <T> RegistryConfigurer addTransient(Class<T> serviceType,
      Function<Container, T> factory);
}

class RegistryConfigurerImpl implements RegistryConfigurer {

  private final Logger logger;
  private final List<Consumer<ServiceCollection>> actions;

  public RegistryConfigurerImpl(Logger logger, List<Consumer<ServiceCollection>> actions) {
    this.logger = logger;
    this.actions = actions;
  }

  @Override
  public <T> RegistryConfigurer addSingleton(Class<T> serviceType,
      Function<Container, T> factory) {
    actions.add(x -> x.addSingleton(serviceType, factory));
    logger.info("Registered Singleton Service " + serviceType.getName());
    return this;
  }

  @Override
  public <T> RegistryConfigurer addScoped(Class<T> serviceType,
      Function<Container, T> factory) {
    actions.add(x -> x.addScoped(serviceType, factory));
    logger.info("Registered Scoped Service " + serviceType.getName());
    return this;
  }

  @Override
  public <T> RegistryConfigurer addTransient(Class<T> serviceType,
      Function<Container, T> factory) {
    actions.add(x -> x.addTransient(serviceType, factory));
    logger.info("Registered Transient Service " + serviceType.getName());
    return this;
  }

}