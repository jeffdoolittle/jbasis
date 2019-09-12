package jbasis.ioc;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jbasis.logging.Log4JLogger;
import jbasis.logging.LoggerFactory;

public class GH11Test {

  static Container _container;

  @BeforeAll public static void before() {
    LoggerFactory.registerFactory(cls -> new Log4JLogger(cls));
    _container = new ContainerImpl(cfg -> cfg.apply(OverloadedServiceRegistry.class));
  }

  @Test public void can_invoke_appropriate_method_on_service_with_overloaded_methods() {
    var svc = _container.resolve(OverloadedService.class);
    Throwable t = null;
    try {
      svc.foo("");
      svc.foo(1);
      } catch (Exception e) {
        t = e;
    }
    assertNull(t);
  }
}

class OverloadedServiceRegistry extends Registry {
  public OverloadedServiceRegistry() {
    register(cfg -> cfg.addTransient(OverloadedService.class, OverloadedServiceImpl.class));
  }
}

interface OverloadedService {
  void foo(String bar);
  void foo(int bar);
}

class OverloadedServiceImpl implements OverloadedService {

  @Override
  public void foo(String bar) {
  }

  @Override
  public void foo(int bar) {
  }
}