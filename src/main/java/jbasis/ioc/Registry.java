package jbasis.ioc;

import java.util.ArrayList;
import java.util.function.Consumer;

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

  
}
