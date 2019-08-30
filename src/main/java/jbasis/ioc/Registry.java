package jbasis.ioc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import jbasis.logging.Logger;
import jbasis.logging.LoggerFactory;

/**
 * Registry classes are used to organization service registrations 
 * into logical, cohesive groupings.
 */
public abstract class Registry {

  private final Logger logger = LoggerFactory.get(getClass());
  private final ArrayList<Consumer<ServiceCollection>> actions;
  private final RegistryConfigurerImpl configurer;

  /**
   * Base class for defining Registries for Container configuration.
   */
  public Registry() {
    actions = new ArrayList<>();
    configurer = new RegistryConfigurerImpl(logger, actions);
    logger.info("Initialized {}", getClass().getName());
  }

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

  final void configure(ServiceCollection serviceCollection) {
    actions.forEach(action -> action.accept(serviceCollection));
  }

  /**
   * Allows for creating composite registries that are built up from
   * smaller, more specific registries.
   * 
   * @param registry the registry to add
   */
  public void includeRegistry(Registry registry) {
    for (Consumer<ServiceCollection> action : registry.actions) {
      this.actions.add(action);
    }
  }

  /**
   * Allows for creating composite registries that are built up from
   * smaller, more specific registries.
   * 
   * @param action an action that configures the registry
   */
  public final void register(Consumer<RegistryConfigurer> action) {
    action.accept(configurer);
  }

  private class RegistryConfigurerImpl implements RegistryConfigurer {

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
    public <T> RegistryConfigurer addTransient(Class<T> serviceType,
        Function<Container, T> factory) {
      actions.add(x -> x.addTransient(serviceType, factory));
      logger.info("Registered Transient Service " + serviceType.getName());
      return this;
    }

  }
}
