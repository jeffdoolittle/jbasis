package jbasis.ioc;

import java.util.function.Function;

/**
 * Describes a service
 */
public final class ServiceDescriptor {

  private Function<ServiceFactory, Object> factory;
  private Class<?> serviceType;
  private Class<?> implementationType;
  private ServiceLifetime serviceLifetime;

  private ServiceDescriptor() {}

  /**
   * Describes a service.
   * 
   * @param <S> the type of service
   * @param <I> the type of implementation
   * @param serviceType the service class type
   * @param lifetime the service lifetime
   * @param factory a factory for instantiating an instance of the service.
   * @return the service instance.
   */
  public static <S, I extends S> ServiceDescriptor init(Class<S> serviceType, 
      ServiceLifetime lifetime, Function<ServiceFactory, I> factory) {
    ServiceDescriptor descriptor = new ServiceDescriptor();
    descriptor.serviceType = serviceType;
    descriptor.serviceLifetime = lifetime;
    descriptor.factory = factory::apply;
    return descriptor;
  }

  /**
   * Describes a service.
   * 
   * @param <S> the type of service
   * @param <I> the type of implementation
   * @param serviceType the service class type
   * @param lifetime the service lifetime
   * @param implementationType the implementation class type
   * @return the service instance.
   */
  public static <S, I extends S> ServiceDescriptor init(Class<S> serviceType, 
      ServiceLifetime lifetime, Class<I> implementationType) {
    ServiceDescriptor descriptor = new ServiceDescriptor();
    descriptor.serviceType = serviceType;
    descriptor.serviceLifetime = lifetime;
    descriptor.implementationType = implementationType;
    return descriptor;
  }

  public Function<ServiceFactory, Object> getFactory() {
    return this.factory;
  }

  public Class<?> getServiceType() {
    return this.serviceType;
  }

  public Class<?> getImplementationType() {
    return this.implementationType;
  }

  public ServiceLifetime getServiceLifetime() {
    return this.serviceLifetime;
  }
}
