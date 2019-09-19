package jbasis.interception;

import java.lang.reflect.Method;
import jbasis.ioc.ServiceFactory;

public abstract class EmptyInterceptor implements Interceptor {

  private ServiceFactory serviceFactory;

  protected ServiceFactory getServiceFactory() {
    return this.serviceFactory;
  }

  @Override
  public boolean beforeInvoke(Object proxy, Object target, Method method, Object[] args) {
    return true;
  }

  @Override
  public void afterInvoke(Object proxy, Object target, Method method, Object[] args, 
      Object result) {}

  @Override
  public boolean onError(Object proxy, Object target, Method method, Object[] args, 
      Throwable throwable) {
    return false;
  }

  @Override
  public final void setServiceFactory(ServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }
}
