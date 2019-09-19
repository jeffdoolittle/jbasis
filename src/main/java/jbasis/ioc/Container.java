package jbasis.ioc;

/**
 * A Root Scope Container for Service resolution.
 */
public interface Container extends ServiceFactory {

  /**
   * Creates a scoped service factory based on the configuration 
   * of the Root Scope container.
   * <p>
   * Singletons will be resolved from the Root Scope service 
   * factory.
   * <p>
   * Scoped services will be resolved within the Scoped 
   * factory and will operate as Singletons within the 
   * Scoped context.
   * <p>
   * Transient services will be resolved on a per-call basis 
   * in the same fashion as they would be from the Root Scope
   * container.
   * 
   * @return the scoped container
   */
  ServiceFactory createScope();
}
