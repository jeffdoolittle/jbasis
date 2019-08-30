package jbasis.ioc;

/**
 * Defines the lifetime of a service within 
 * its registered container.
 */
public enum ServiceLifetime {
  /**
   * Only one instance will be created within a Container
   */
  SINGLETON, // a single instance
  
  /**
   * Only one instance will be created within a Scoped Container
   */
  // SCOPED, // a scoped instance // TODO: implement scoping
  
  /**
   * An instance will be created per call to Container.resolve
   */
  TRANSIENT, // a new instance every time requested
}
