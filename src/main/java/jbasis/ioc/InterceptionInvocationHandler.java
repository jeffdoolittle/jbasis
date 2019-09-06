package jbasis.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Deque;

import jbasis.interception.Interceptor;
import jbasis.interception.WithInterceptor;
import jbasis.util.JBasisException;

class InterceptionInvocationHandler implements InvocationHandler {

  private final Map<String, Method> methods = new HashMap<>();
  private Container container;
  private Object target;

  /**
   * Proxy builder for Container registered services.
   * 
   * @param container the Container for resolving services.
   * @param target the target Object to be wrapped in a proxy.
   */
  public InterceptionInvocationHandler(Container container, Object target) {
    this.container = container;
    this.target = target;

    for (Method method : target.getClass().getDeclaredMethods()) {
      makeAccessible(method);
      this.methods.put(method.getName(), method);
    }
  }

  @Override
  public Object invoke(Object proxy, Method serviceMethod, Object[] args) {
    if (serviceMethod.getName().equals("toString")) {
      return this.target.toString();
    }
    if (serviceMethod.getName().equals("equals")) {
      return proxy == args[0];
    }

    Method implementationMethod = methods.get(serviceMethod.getName());
    Deque<Interceptor> interceptors = resolveInterceptors(implementationMethod);

    for (Interceptor interceptor : interceptors) {
      boolean continueExecuting = interceptor.beforeInvoke(proxy, target, implementationMethod, args);
      if (!continueExecuting) {
        return null;
      }
    }

    Object result = null;
    boolean wasHandled = false;
    try {
      
      result = implementationMethod.invoke(target, args);
    } catch (Exception ex) {
      for (Interceptor interceptor : interceptors) {
        boolean handled = interceptor.onError(proxy, target, implementationMethod, args, ex);
        if (handled) {
          wasHandled = true;
          break;
        }
      }
      if (!wasHandled) {
        Throwable cause = ex.getCause();
        throw new JBasisException(cause.getMessage(), cause);
      }
    }

    while (!interceptors.isEmpty()) {
      Interceptor interceptor = interceptors.pop();
      interceptor.afterInvoke(proxy, target, implementationMethod, args, result);
    }

    return result;
  }

  private Deque<Interceptor> resolveInterceptors(Method implementationMethod) {
    Deque<Interceptor> interceptors = new ArrayDeque<>();
    for (Annotation annotation : implementationMethod.getAnnotations()) {
      Interceptor interceptor = resolveInterceptor(annotation);
      interceptor.setContainer(container);
      interceptors.add(interceptor);
    }
    return interceptors;
  }

  private Interceptor resolveInterceptor(Annotation annotation) {
    Class<?> interceptorClass = null;

    try {
      WithInterceptor withInterceptor = annotation.annotationType()
          .getAnnotation(WithInterceptor.class);
  
      if (withInterceptor == null) {
        Method method;
        method = annotation.getClass().getMethod("withInterceptor");
        makeAccessible(method);
        interceptorClass = (Class<?>) method.invoke(annotation);
      } else {
        interceptorClass = withInterceptor.type();
      }
    } catch (InvocationTargetException e) {
      throw new JBasisException(e.getCause().getMessage(), e.getCause());
    } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
      throw new JBasisException(e.getMessage(), e);
    }

    Optional<Constructor<?>> optCtor = Arrays.asList(interceptorClass.getConstructors())
        .stream().findFirst();
    
    if (!optCtor.isPresent()) {
      throw new JBasisException("Constructor expected for interceptor but none was found.");
    }
    
    Constructor<?> constructor = optCtor.get();
  
    makeAccessible(constructor);

    try {
      return (Interceptor) constructor.newInstance(annotation);
    } catch (InvocationTargetException e) {
      throw new JBasisException(e.getCause().getMessage(), e.getCause());
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      throw new JBasisException(e.getMessage(), e);
    }
  }

  // suppress SonarQuber Security Hotspot warning
  @java.lang.SuppressWarnings("squid:S3011")
  private void makeAccessible(AccessibleObject member) {
    member.setAccessible(true); 
  }
}
