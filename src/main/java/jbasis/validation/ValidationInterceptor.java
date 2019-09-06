package jbasis.validation;

import java.lang.reflect.Method;

import jbasis.interception.EmptyInterceptor;

public class ValidationInterceptor extends EmptyInterceptor {
  
  // validate preconditions of the target object and the arguments

  @Override
  public boolean beforeInvoke(Object proxy, Object target, Method method, Object[] args) {
    
    return super.beforeInvoke(proxy, target, method, args);
  }

  @Override
  public void afterInvoke(Object proxy, Object target, Method method, Object[] args, Object result) {
    
    // validate preconditions of the result

    super.afterInvoke(proxy, target, method, args, result);
  }
}
