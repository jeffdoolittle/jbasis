package jbasis.ioc;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import jbasis.logging.Logger;
import jbasis.logging.LoggerFactory;

/**
 * DSL entry point for registry configuration
 */
public interface RegistryConfigurer {

  /**
   * Adds a service factory with the SINGLETON lifecycle to the 
   * registry configuration.
   * 
   * @param <S> the type of service
   * @param <I> the type of implementation
   * @param serviceType the service class type
   * @param factory the factory for creating the service instance
   * @return the service instance
  */
  public <S, I extends S> RegistryConfigurer addSingleton(Class<S> serviceType,
      Function<ServiceFactory, I> factory);

  /**
   * Adds a service factory with the SINGLETON lifecycle to the 
   * registry configuration.
   *
   * @param <S> the type of service
   * @param <I> the type of implementation
   * @param serviceType the service class type
   * @param implementationType the implementation class type
   * @return the service instance
   */
  public <S, I extends S> RegistryConfigurer addSingleton(
      Class<S> serviceType, Class<I> implementationType);

  /**
   * Adds a service factory with the SCOPED lifecycle to the 
   * registry configuration.
   * 
   * @param <S> the type of service
   * @param <I> the type of implementation
   * @param serviceType the service class type
   * @param factory the factory for creating the service instance
   * @return the service instance
   */
  public <S, I extends S> RegistryConfigurer addScoped(Class<S> serviceType,
      Function<ServiceFactory, I> factory);

  /**
   * Adds a service factory with the SCOPED lifecycle to the 
   * registry configuration.
   *
   * @param <S> the type of service
   * @param <I> the type of implementation
   * @param serviceType the service class type
   * @param implementationType the implementation class type
   * @return the service instance
   */
  public <S, I extends S> RegistryConfigurer addScoped(
      Class<S> serviceType, Class<I> implementationType);

  /**
   * Adds a service factory with the TRANSIENT lifecycle to the 
   * registry configuration.
   * 
   * @param <S> the type of service
   * @param <I> the type of implementation
   * @param serviceType the service class type
   * @param factory the factory for creating the service instance
   * @return the service instance
   */
  public <S, I extends S> RegistryConfigurer addTransient(Class<S> serviceType,
      Function<ServiceFactory, I> factory);

  /**
   * Adds a service factory with the TRANSIENT lifecycle to the 
   * registry configuration.
   *
   * @param <S> the type of service
   * @param <I> the type of implementation
   * @param serviceType the service class type
   * @param implementationType the implementation class type
   * @return the service instance
   */
  public <S, I extends S> RegistryConfigurer addTransient(
      Class<S> serviceType, Class<I> implementationType);

}

class RegistryConfigurerImpl implements RegistryConfigurer {

  private static final Logger logger = LoggerFactory.get(RegistryConfigurer.class);
  private final List<Consumer<ServiceCollection>> actions;

  public RegistryConfigurerImpl(List<Consumer<ServiceCollection>> actions) {
    this.actions = actions;
  }

  private <S, I extends S> void add(Class<S> serviceType, ServiceLifetime lifetime,
    Function<ServiceFactory, I> factory) {
      actions.add(x -> x.add(serviceType, lifetime, factory));
      logger.info("Registered {} {}", lifetime, serviceType.getName());
    }

  private <S, I extends S> void add(Class<S> serviceType, ServiceLifetime lifetime,
      Class<I> implementationType) {
    actions.add(x -> x.add(serviceType, lifetime, implementationType));
    logger.info("Registered {} {} -> {}", lifetime, serviceType.getName(), implementationType.getName());
  }

  @Override
  public <S, I extends S> RegistryConfigurer addSingleton(Class<S> serviceType,
      Function<ServiceFactory, I> factory) {
    add(serviceType, ServiceLifetime.SINGLETON, factory);
    return this;
  }

  @Override
  public <S, I extends S> RegistryConfigurer addScoped(Class<S> serviceType,
      Function<ServiceFactory, I> factory) {
    add(serviceType, ServiceLifetime.SCOPED, factory);
    return this;
  }

  @Override
  public <S, I extends S> RegistryConfigurer addTransient(Class<S> serviceType,
      Function<ServiceFactory, I> factory) {
    add(serviceType, ServiceLifetime.TRANSIENT, factory);
    return this;
  }

  @Override
  public <S, I extends S> RegistryConfigurer addSingleton(Class<S> serviceType, 
      Class<I> implementationType) {
    add(serviceType, ServiceLifetime.SINGLETON, implementationType);
    return this;
  }

  @Override
  public <S, I extends S> RegistryConfigurer addScoped(Class<S> serviceType, 
      Class<I> implementationType) {
    add(serviceType, ServiceLifetime.SCOPED, implementationType);
    return this;
  }

  @Override
  public <S, I extends S> RegistryConfigurer addTransient(Class<S> serviceType, 
      Class<I> implementationType) {
    add(serviceType, ServiceLifetime.TRANSIENT, implementationType);
    return this;
  }
}