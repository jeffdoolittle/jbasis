package jbasis.interception;

import java.lang.reflect.Method;
import jbasis.ioc.Container;

/**
 * Interceptors provide aspect oriented functionality to clases.
 */
public interface Interceptor {
  /**
   * Provides a container which can be used to resolve other 
   * services on which an Interceptor may depend.
   * 
   * @param container the container to be used for service resolution
   */
  void setContainer(Container container);

  /**
   * Executes before a method is invoked.
   * 
   * @param proxy the proxy that wraps the target object
   * @param target the target object
   * @param method the method about to be invoked
   * @param args the args to be passed to the method
   * @return true to continue execution, otherwise false
   */
  boolean beforeInvoke(Object proxy, Object target, Method method, Object[] args);

  /**
   * Executes after a method has been invoked.
   * 
   * @param proxy the proxy that wraps the target object
   * @param target the target object
   * @param method the method about to be invoked
   * @param args the args to be passed to the method
   * @param result the output result of the invoked method
   */
  void afterInvoke(Object proxy, Object target, Method method, Object[] args, Object result);

  /**
   * Executes if a method throws an exception.
   * 
   * @param proxy the proxy that wraps the target object
   * @param target the target object
   * @param method the method about to be invoked
   * @param args the args to be passed to the method
   * @param t the exception describing the error condition
   * @return true if the exception has been handled, otherwise false
   */
  boolean onError(Object proxy, Object target, Method method, Object[] args, Throwable t);
}
