package jbasis.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import jbasis.logging.Logger;
import jbasis.logging.LoggerFactory;
import jbasis.util.JBasisException;

/**
 * Default Container implementation
 */
public final class ContainerImpl implements Container, AutoCloseable {
 
  private final Logger logger = LoggerFactory.get(getClass());
  private Map<String, FactoryDescriptor> descriptors = new HashMap<>();
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

    FactoryDescriptor sf = new FactoryDescriptor();
    sf.lifetime = descriptor.getServiceLifetime();
    sf.serviceType = descriptor.getServiceType();

    final String initializingFormat = "Initializing {} Service {}";
    final String initializedFormat = "Initialized {} Service {} for {}";

    if (descriptor.getServiceLifetime() == ServiceLifetime.SINGLETON) {
      sf.factory = new LazyFactory<>(ctx -> {
        debug(initializingFormat, ServiceLifetime.SINGLETON, name);
        Object service = createProxy(ctx, descriptor);
        logger.info(initializedFormat, ServiceLifetime.SINGLETON, service, name);
        return service;
      });
    } else if (descriptor.getServiceLifetime() == ServiceLifetime.SCOPED) {
      sf.factory = ctx -> {
        debug(initializingFormat, ServiceLifetime.SCOPED, name);
        Object service = createProxy(ctx, descriptor);
        logger.info(initializedFormat, ServiceLifetime.SCOPED, service, name);
        return service;
      };
    } else {
      sf.factory = ctx -> {
        debug(initializingFormat, ServiceLifetime.TRANSIENT, name);
        Object service = createProxy(ctx, descriptor);
        logger.info(initializedFormat, ServiceLifetime.TRANSIENT, service, name);
        return service;
      };
    }

    descriptors.put(name, sf);
  }

  private Object createProxy(ServiceFactory context, ServiceDescriptor descriptor) {
    Function<ServiceFactory, Object> factory = descriptor.getFactory();
    Object service = null;

    if (factory != null) {
      service = factory.apply(context);
    } else {
      service = constructService(descriptor);
    }

    if (descriptor.getServiceType().isInterface()) {
      service = Proxy.newProxyInstance(ContainerImpl.class.getClassLoader(),
          new Class[] {descriptor.getServiceType()}, new InterceptionInvocationHandler(context, service));
    }
    return service;
  }

  private Object constructService(ServiceDescriptor descriptor) {
    Constructor<?>[] constructors = descriptor.getImplementationType().getDeclaredConstructors();

    Optional<Constructor<?>> optionalConstructor = Arrays.stream(constructors)
      .sorted((x,y) -> Long.compare(y.getParameterTypes().length, x.getParameterTypes().length))
      .findFirst();
    
    if (!optionalConstructor.isPresent()) {
      throw new JBasisException("No constructors found for " + descriptor.getImplementationType().getTypeName());
    }

    Constructor<?> constructor = optionalConstructor.get();

    List<Object> dependencies = resolveServiceDependencies(constructor, descriptor.getImplementationType().getName());

    try {
      return constructor.newInstance(dependencies.toArray());
    } catch (InvocationTargetException e) {
      throw new JBasisException(e.getCause().getMessage(), e.getCause());
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      throw new JBasisException(e.getMessage(), e);
    }
  }

  private List<Object> resolveServiceDependencies(Constructor<?> constructor, String implementationTypeName) {
    Class<?>[] dependencyTypes = constructor.getParameterTypes();
    
    List<Object> dependencies = new ArrayList<>();
    for(Class<?> dependencyType : dependencyTypes) {
      String typeName = dependencyType.getTypeName();
      if (!descriptors.containsKey(typeName)){
        dependencies.add(null);
      } else {
        logger.debug("AUTORESOLVE - {} for {}", typeName, implementationTypeName);
        FactoryDescriptor dependencyFactory = descriptors.get(typeName);
        Object dependency = dependencyFactory.factory.apply(this);
        dependencies.add(dependency);
        logger.info("Resolved {} dependency for {}", typeName, implementationTypeName);
      }
    }
    return dependencies;
  }

  private class FactoryDescriptor {
    ServiceLifetime lifetime;
    Function<ServiceFactory, ?> factory;
    Class<?> serviceType;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T resolve(Class<T> cls) {
    debug("Resolving Service " + cls.getName());
    String key = getKey(cls);
    if (descriptors.containsKey(key)) {
      FactoryDescriptor sf = descriptors.get(key);
      if (sf.lifetime == ServiceLifetime.SCOPED 
          && !isScoped) {
        throw new JBasisException("Cannot resolve scoped services in root scope");
      }
      Object service = sf.factory.apply(this);
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
      if (descriptors.containsKey(key)) {
        FactoryDescriptor sf = descriptors.get(key);
        if (sf.lifetime == ServiceLifetime.SCOPED 
            && !isScoped) {
          throw new JBasisException("Cannot resolve scoped services in root scope");
        }
        Object service = sf.factory.apply(this);
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
    for (Entry<String, FactoryDescriptor> entry : descriptors.entrySet()) {
      FactoryDescriptor sf = entry.getValue();
      if (!isPotentiallyCloseable(sf)) {
        continue;
      }
      Object service = sf.factory.apply(this);
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
    descriptors.clear();
    logger.info("Closed " + type);
  }

  @Override
  public Container createScope() {
    ContainerImpl scope = new ContainerImpl();
    scope.isScoped = true;
    for(Entry<String, FactoryDescriptor> entry : descriptors.entrySet()) {
      FactoryDescriptor sf = entry.getValue();
      if (sf.lifetime == ServiceLifetime.TRANSIENT 
          || sf.lifetime == ServiceLifetime.SINGLETON) {
        scope.descriptors.put(entry.getKey(), sf);
      } else {
        FactoryDescriptor scopedFactory = new FactoryDescriptor();
        scopedFactory.serviceType = sf.serviceType;
        scopedFactory.lifetime = sf.lifetime;
        scopedFactory.factory = new LazyFactory<>(sf.factory);
        scope.descriptors.put(entry.getKey(), scopedFactory);
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
  private boolean isPotentiallyCloseable(FactoryDescriptor sf) {
    boolean potentiallyCloseable = true;
    if (sf.lifetime == ServiceLifetime.TRANSIENT) {
      potentiallyCloseable = false;
    } else if (sf.lifetime == ServiceLifetime.SCOPED) {
      if (isScoped) {
        LazyFactory<?> lazy = (LazyFactory<?>)sf.factory;
        if (!lazy.isPresent()) {
          potentiallyCloseable = false;
        }
      } else {
      potentiallyCloseable = false;
      }      
    } else {
      LazyFactory<?> lazy = (LazyFactory<?>)sf.factory;
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

