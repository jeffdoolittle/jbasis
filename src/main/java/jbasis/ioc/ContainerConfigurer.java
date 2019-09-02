package jbasis.ioc;

import java.lang.reflect.Constructor;

/**
 * Container configuration DSL entry point.
 * 
 */
public interface ContainerConfigurer {

  /**
   * Applies a class of Registry type to the 
   * container configuration.
   *
   * @param <R> the type of register
   * @param registryType the class registry type
   */
  <R extends Registry> void apply(Class<R> registryType);

  /**
   * Applies an instance of a Registry class to the 
   * container configuration.
   * 
   * @param <R> the type of register
   * @param registry a registry instance
   */
  <R extends Registry> void apply(R registry);

  /**
   * Gets the configured registry.
   * 
   * @return the configured registry
   */
  Registry getRegistry();
}

class ContainerConfigurerImpl implements ContainerConfigurer {
  private Registry registry;

  @Override
  public <R extends Registry> void apply(Class<R> registryType) {
    try {
      Constructor<R> constructor = registryType.getConstructor();
      this.registry = constructor.newInstance();
    } catch (Exception t) {
      throw new JBasisException(t.getMessage(), t);
    }
  }

  @Override
  public <R extends Registry> void apply(R registry) {
    this.registry = registry;
  }

  @Override
  public Registry getRegistry() {
    return this.registry;
  }
}
