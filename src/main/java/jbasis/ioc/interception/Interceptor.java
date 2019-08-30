package jbasis.ioc.interception;

import java.lang.reflect.Method;
import jbasis.ioc.Container;

public interface Interceptor {
  void setContainer(Container container);

  void beforeInvoke(Object proxy, Object target, Method method, Object[] args);

  void afterInvoke(Object proxy, Object target, Method method, Object[] args, Object result);

  boolean onError(Object proxy, Object target, Method method, Object[] args, Throwable t);
}
