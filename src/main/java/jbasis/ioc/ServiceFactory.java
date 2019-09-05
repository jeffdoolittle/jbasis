package jbasis.ioc;

import java.util.Optional;

/**
 * A factory for Service resolution.
 */
public interface ServiceFactory {
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
   * Closes all registered services implementing Closeable or AutoCloseable.
   * Any services not implementing these interfaces are ignored.
   */
  void close();
}