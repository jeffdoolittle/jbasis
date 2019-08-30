package jbasis.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import jbasis.ioc.interception.InterceptionInvocationHandler;
import jbasis.logging.Logger;
import jbasis.logging.LoggerFactory;

/**
 * Default Container implementation
 */
public final class ContainerImpl implements Container, AutoCloseable {
 
  private final Logger logger = LoggerFactory.get(getClass());
  private Map<String, ServiceFactory> suppliers = new HashMap<>();

  /**
   * Container implementation.
   * @param configure a consumer that allows configuring the Container
   */
  public ContainerImpl(Consumer<ContainerConfigurer> configure) {

    ContainerConfigurer configurer = new ContainerConfigurerImpl();
    configure.accept(configurer);
    try {
      Registry registry = configurer.getRegistry();

      ServiceCollectionImpl services = new ServiceCollectionImpl();
      registry.configure(services);

      for (ServiceDescriptor descriptor : services) {
        String name = descriptor.getServiceType().getTypeName();

        ServiceFactory sf = new ServiceFactory();
        sf.lifetime = descriptor.getServiceLifetime();
        sf.serviceType = descriptor.getServiceType();

        if (descriptor.getServiceLifetime() == ServiceLifetime.SINGLETON) {
          sf.factory = new Lazy<>(() -> {
            debug("Initializing Singleton Service {}", name);
            Object service = createProxy(descriptor, sf);
            logger.info("Initialized Singleton Service {} for {}", service, name);
            return service;
          });
        } else {
          sf.factory = () -> {
            debug("Initializing Transient Service {}", name);
            Object service = createProxy(descriptor, sf);
            logger.info("Initialized Transient Service {} for {}", service, name);
            return service;
          };
        }

        suppliers.put(name, sf);
      }

    } catch (Exception t) {
      throw new JBasisException(t.getMessage(), t);
    }
  }

  private Object createProxy(ServiceDescriptor descriptor, ServiceFactory sf) {
    Object service = descriptor.getFactory().apply(this);
    if (sf.serviceType.isInterface()) {
      service = Proxy.newProxyInstance(ContainerImpl.class.getClassLoader(),
          new Class[] {sf.serviceType}, new InterceptionInvocationHandler(this, service));
    }
    return service;
  }

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

  private class ContainerConfigurerImpl implements ContainerConfigurer {
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

  private class ServiceFactory {
    ServiceLifetime lifetime;
    Supplier<?> factory;
    Class<?> serviceType;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T resolve(Class<T> cls) {
    debug("Resolving Service " + cls.getName());
    String key = getKey(cls);
    if (suppliers.containsKey(key)) {
      ServiceFactory sf = suppliers.get(key);
      Object service = sf.factory.get();
      logger.info("Resolved {} Service {} for {}" + cls.getName(), sf.lifetime, service, 
          cls.getName());
      return (T) service;
    }
    logger.error("Error - unable to resolve Service {}", cls.getName());
    throw new JBasisException("Unable to resolve " + cls.toString());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Optional<T> tryResolve(Class<T> cls) {
    final String warning = "Warning - unable to resolve Service {}";
    debug("Resolving Service {}", cls.getName());
    try {
      String key = getKey(cls);
      if (suppliers.containsKey(key)) {
        ServiceFactory sf = suppliers.get(key);
        Object service = sf.factory.get();
        logger.info("Resolved {} Service {} for {}" + cls.getName(), sf.lifetime, service, 
            cls.getName());
        return Optional.of((T) service);
      }
    } catch (Exception e) {
      logger.warn(warning, cls.getName(), e);
      return Optional.empty();
    }
    logger.warn(warning, cls.getName());
    return Optional.empty();
  }

  private String getKey(Class<?> cls) {
    return cls.getTypeName();
  }

  @Override
  public void close() {
    debug("Closing Container");
    for (Entry<String, ServiceFactory> entry : suppliers.entrySet()) {
      ServiceFactory sf = entry.getValue();
      if (!isPotentiallyCloseable(sf)) {
        continue;
      }
      Object service = sf.factory.get();
      Class<?> serviceType = sf.serviceType;
      try {
        if (AutoCloseable.class.isInstance(service)) {
          AutoCloseable c = (AutoCloseable) service;
          c.close();
          debug("Closed service {}", serviceType.getName());
        } else {
          debug("Service {} not closeable. Skipping...", serviceType.getName());
        }
      } catch (Exception e) {
        logger.error("Error closing Container: " + e.getMessage(), e);
      }
    }
    suppliers.clear();
    logger.info("Closed Container");
  }

  /** 
   * Determines if the service is potentially closeable.
   * 
   * If TRANSIENT - not closeable. It is up to the caller to clean up
   * their own services if they are TRANSIENT.
   * 
   * If SINGLETON and service has not been instantiated, then it is 
   * not considered closeable. This is to avoid creating an instance 
   * of the service for the sole purpose of then closing it after the 
   * fact.
   */
  private boolean isPotentiallyCloseable(ServiceFactory sf) {
    boolean potentiallyCloseable = true;
    if (sf.lifetime == ServiceLifetime.TRANSIENT) {
      potentiallyCloseable = false;
    } else {
      Lazy<?> lazy = (Lazy<?>)sf.factory;
      if (!lazy.isPresent()) {
        potentiallyCloseable = false;
      }
    }
    return potentiallyCloseable;
  }

  private void debug(String format, Object... params) {
    if (logger.isDebugEnabled()) {
      logger.debug(format, params);
    }
  }
}

