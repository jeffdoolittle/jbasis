package jbasis.ioc;

import java.util.Optional;

/**
 * A Container for Service resolution
 */
public interface Container extends AutoCloseable {

  /**
   * Resolves a service type type. Throws an exception 
   * if not found.
   * 
   * @param <T> The service type
   * @param cls A Class reference to the service type
   * @return an instance of the service type
   */
  <T> T resolve(Class<T> cls);

  /**
   * Attempts to resolve a service by type. Returns Optional.empty() 
   * if not found or an error occurs.
   * 
   * @param <T> The service type
   * @param cls A Class reference to the service type
   * @return an Optional instance of the service type if resolved
   */
  <T> Optional<T> tryResolve(Class<T> cls);

  /**
   * Creates a scoped container based on the configuration of 
   * the parent container.
   * <p>
   * Singletons will be resolved from the parent container.
   * <p>
   * Scoped services will be resolved within the Scoped 
   * container and will operate as Singletons within the 
   * Scoped context.
   * <p>
   * Transient services will be resolved on a per-call basis 
   * in the same fashion as they would be from the parent
   * container.
   * 
   * @return the scoped container
   */
  Container createScope();

  /**
   * Closes all registered services implementing Closeable or AutoCloseable.
   * Any services not implementing these interfaces are ignored.
   */
  void close();
}
