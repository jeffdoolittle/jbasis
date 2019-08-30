package jbasis.ioc.interception;

import java.lang.reflect.Method;
import jbasis.ioc.Container;

public abstract class EmptyInterceptor implements Interceptor {

  private Container container;

  protected Container getContainer() {
    return this.container;
  }

  @Override
  public void beforeInvoke(Object proxy, Object target, Method method, Object[] args) {}

  @Override
  public void afterInvoke(Object proxy, Object target, Method method, Object[] args, 
      Object result) {}

  @Override
  public boolean onError(Object proxy, Object target, Method method, Object[] args, 
      Throwable throwable) {
    return false;
  }

  @Override
  public final void setContainer(Container container) {
    this.container = container;
  }
}
