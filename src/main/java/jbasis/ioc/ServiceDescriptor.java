package jbasis.ioc;

import java.util.function.Function;

/**
 * Describes a service
 */
public final class ServiceDescriptor {

  private Function<Container, Object> factory;
  private Class<?> serviceType;
  private ServiceLifetime serviceLifetime;

  private ServiceDescriptor() {}

  /**
   * Describes a service.
   * 
   * @param <T> the type of service.
   * @param serviceType the type of service.
   * @param lifetime the lifetime of the service.
   * @param factory a factory for instantiating an instance of the service.
   * @return the service instance.
   */
  public static <T> ServiceDescriptor init(Class<T> serviceType, ServiceLifetime lifetime,
      Function<Container, T> factory) {
    ServiceDescriptor descriptor = new ServiceDescriptor();
    descriptor.serviceType = serviceType;
    descriptor.serviceLifetime = lifetime;
    descriptor.factory = factory::apply;
    return descriptor;
  }

  public Function<Container, Object> getFactory() {
    return this.factory;
  }

  public Class<?> getServiceType() {
    return this.serviceType;
  }

  public ServiceLifetime getServiceLifetime() {
    return this.serviceLifetime;
  }
}
