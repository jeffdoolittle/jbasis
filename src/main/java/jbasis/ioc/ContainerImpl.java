package jbasis.ioc;

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
  private boolean isScoped = false;

  private ContainerImpl() {}

  /**
   * Container implementation.
   * @param configure a consumer that allows configuring the Container
   */
  public ContainerImpl(Consumer<ContainerConfigurer> configure) {

    logger.info("Initializing Root Scope");
    ContainerConfigurer configurer = new ContainerConfigurerImpl();
    configure.accept(configurer);
    try {
      Registry registry = configurer.getRegistry();

      ServiceCollectionImpl services = new ServiceCollectionImpl();
      registry.configure(services);

      for (ServiceDescriptor descriptor : services) {
        register(descriptor);
      }

    } catch (Exception t) {
      throw new JBasisException(t.getMessage(), t);
    }
  }

  private void register(ServiceDescriptor descriptor) {
    String name = descriptor.getServiceType().getTypeName();

    ServiceFactory sf = new ServiceFactory();
    sf.lifetime = descriptor.getServiceLifetime();
    sf.serviceType = descriptor.getServiceType();

    final String initializingFormat = "Initializing {} Service {}";
    final String initializedFormat = "Initialized {} Service {} for {}";

    if (descriptor.getServiceLifetime() == ServiceLifetime.SINGLETON) {
      sf.factory = new Lazy<>(() -> {
        debug(initializingFormat, ServiceLifetime.SINGLETON, name);
        Object service = createProxy(descriptor, sf);
        logger.info(initializedFormat, ServiceLifetime.SINGLETON, service, name);
        return service;
      });
    } else if (descriptor.getServiceLifetime() == ServiceLifetime.SCOPED) {
      sf.factory = () -> {
        debug(initializingFormat, ServiceLifetime.SINGLETON, name);
        Object service = createProxy(descriptor, sf);
        logger.info(initializedFormat, ServiceLifetime.SINGLETON, service, name);
        return service;
      };
    } else {
      sf.factory = () -> {
        debug(initializingFormat, ServiceLifetime.SINGLETON, name);
        Object service = createProxy(descriptor, sf);
        logger.info(initializedFormat, ServiceLifetime.SINGLETON, service, name);
        return service;
      };
    }

    suppliers.put(name, sf);
  }

  private Object createProxy(ServiceDescriptor descriptor, ServiceFactory sf) {
    Object service = descriptor.getFactory().apply(this);
    if (sf.serviceType.isInterface()) {
      service = Proxy.newProxyInstance(ContainerImpl.class.getClassLoader(),
          new Class[] {sf.serviceType}, new InterceptionInvocationHandler(this, service));
    }
    return service;
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
      if (sf.lifetime == ServiceLifetime.SCOPED 
          && !isScoped) {
        throw new JBasisException("Cannot resolve scoped services in root scope");
      }
      Object service = sf.factory.get();
      logger.info("Resolved {} Service {} for {}", sf.lifetime, cls.getName(), service);
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
        if (sf.lifetime == ServiceLifetime.SCOPED 
            && !isScoped) {
          throw new JBasisException("Cannot resolve scoped services in root scope");
        }
        Object service = sf.factory.get();
        logger.info("Resolved {} Service {} for {}", sf.lifetime,  cls.getName(), service);
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
    String type = isScoped ? "Scope" : "Root Scope";
    debug("Closing " + type);
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
    logger.info("Closed " + type);
  }

  @Override
  public Container createScope() {
    ContainerImpl scope = new ContainerImpl();
    scope.isScoped = true;
    for(Entry<String, ServiceFactory> entry : suppliers.entrySet()) {
      ServiceFactory sf = entry.getValue();
      if (sf.lifetime == ServiceLifetime.TRANSIENT 
          || sf.lifetime == ServiceLifetime.SINGLETON) {
        scope.suppliers.put(entry.getKey(), sf);
      } else {
        ServiceFactory scopedFactory = new ServiceFactory();
        scopedFactory.serviceType = sf.serviceType;
        scopedFactory.lifetime = sf.lifetime;
        scopedFactory.factory = new Lazy<>(sf.factory);
        scope.suppliers.put(entry.getKey(), scopedFactory);
      }
    }
    logger.info("New scope created");
    return scope;
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
    } else if (sf.lifetime == ServiceLifetime.SCOPED) {
      if (isScoped) {
        Lazy<?> lazy = (Lazy<?>)sf.factory;
        if (!lazy.isPresent()) {
          potentiallyCloseable = false;
        }
      } else {
      potentiallyCloseable = false;
      }      
    } else {
      Lazy<?> lazy = (Lazy<?>)sf.factory;
      if (!lazy.isPresent()) {
        potentiallyCloseable = false;
      } else {
        potentiallyCloseable = !isScoped;
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

