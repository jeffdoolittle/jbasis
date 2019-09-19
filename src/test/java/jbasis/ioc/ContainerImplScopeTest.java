package jbasis.ioc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jbasis.logging.Log4JLogger;
import jbasis.logging.LoggerFactory;
import jbasis.util.JBasisException;

public class ContainerImplScopeTest {

  @BeforeAll public static void beforeAll() {
    LoggerFactory.registerFactory(cls -> new Log4JLogger(cls));
  }

  @Test public void singleton_and_transient_function_normally_in_root_container_with_scoped_service_registered() {
    Container container = new ContainerImpl(cfg -> cfg.apply(ScopeTestRegistry.class));

    ScopeTestSingletonService s1 = container.resolve(ScopeTestSingletonService.class);
    ScopeTestSingletonService s2 = container.resolve(ScopeTestSingletonService.class);

    ScopeTestTransientService t1 = container.resolve(ScopeTestTransientService.class);
    ScopeTestTransientService t2 = container.resolve(ScopeTestTransientService.class);

    assertEquals(s1, s2);
    assertNotEquals(t1, t2);

    container.close();
  }

  @Test public void singleton_and_transient_can_be_resolved_from_scope() {
    Container container = new ContainerImpl(cfg -> cfg.apply(ScopeTestRegistry.class));

    ServiceFactory scope = container.createScope();

    ScopeTestSingletonService s1 = scope.resolve(ScopeTestSingletonService.class);
    ScopeTestSingletonService s2 = scope.resolve(ScopeTestSingletonService.class);

    ScopeTestTransientService t1 = scope.resolve(ScopeTestTransientService.class);
    ScopeTestTransientService t2 = scope.resolve(ScopeTestTransientService.class);

    assertEquals(s1, s2);
    assertNotEquals(t1, t2);

    scope.close();

    assertFalse(s1.isClosed());
    assertFalse(s2.isClosed());

    container.close();
  }

  @Test public void exception_resolving_scoped_service_from_root_scope() {
    Container container = new ContainerImpl(cfg -> cfg.apply(ScopeTestRegistry.class));

    assertThrows(JBasisException.class, () -> container.resolve(ScopedService.class));
    
    Optional<ScopedService> optSvc = container.tryResolve(ScopedService.class);
    assertTrue(!optSvc.isPresent());

    container.close();
  }

  @Test public void can_resolve_scoped_service_from_scope() {
    Container container = new ContainerImpl(cfg -> cfg.apply(ScopeTestRegistry.class));

    ServiceFactory scope = container.createScope();

    ScopedService svc1 = scope.resolve(ScopedService.class);
    ScopedService svc2 = scope.resolve(ScopedService.class);

    assertEquals(svc1, svc2);

    scope.close();

    assertTrue(svc1.isClosed());
    assertTrue(svc2.isClosed());

    container.close();
  }

  @Test public void can_resolve_scoped_service_with_scoped_dependency() {
    Container container = new ContainerImpl(cfg -> cfg.apply(ScopeTestRegistry.class));
  
    ServiceFactory scope = container.createScope();
  
    ScopedWithScopedDependency svc = scope.resolve(ScopedWithScopedDependency.class);
  
    assertNotNull(svc);
  
    assertNotNull(svc.getDep());

    scope.close();

    container.close();
  }
}

class ScopeTestRegistry extends Registry {
  public ScopeTestRegistry() {
    register(cfg -> cfg.addSingleton(ScopeTestSingletonService.class, x -> new SingletonImpl()));
    register(cfg -> cfg.addScoped(ScopedService.class, x -> new ScopedImpl()));
    register(cfg -> cfg.addTransient(ScopeTestTransientService.class, x -> new TransientImpl()));
    register(cfg -> cfg.addScoped(ScopedDependency.class, x -> new ScopedDependencyImpl()));
    register(cfg -> cfg.addScoped(ScopedWithScopedDependency.class, x -> new ScopedWithScopedDependencyImpl(x.resolve(ScopedDependency.class))));
  }
}

interface ScopeTestSingletonService extends Closeable {
  UUID getId();
  boolean isClosed();
}

interface ScopedService extends Closeable {
  UUID getId();
  boolean isClosed();
}

interface ScopeTestTransientService {
  UUID getId();
}

class SingletonImpl implements ScopeTestSingletonService {
  UUID _id;
  boolean _closed;

  SingletonImpl() {
    _id = UUID.randomUUID();
  }

  @Override
  public void close() throws IOException {
    _closed = true;
  }

  @Override
  public UUID getId() {
    return _id;
  }

  @Override
  public boolean isClosed() {
    return _closed;
  }
}

class ScopedImpl implements ScopedService {
  UUID _id;
  boolean _closed;

  ScopedImpl() {
    _id = UUID.randomUUID();
  }

  @Override
  public void close() throws IOException {
    _closed = true;
  }

  @Override
  public UUID getId() {
    return _id;
  }

  @Override
  public boolean isClosed() {
    return _closed;
  }
}

class TransientImpl implements ScopeTestTransientService {
  UUID _id;

  TransientImpl() {
    _id = UUID.randomUUID();
  }

  @Override
  public UUID getId() {
    return _id;
  }
}

interface ScopedWithScopedDependency {
  ScopedDependency getDep() ;
}

class ScopedWithScopedDependencyImpl implements ScopedWithScopedDependency {
  private ScopedDependency dep;

  ScopedWithScopedDependencyImpl(ScopedDependency dep) {
    this.dep = dep;
  }

  public ScopedDependency getDep() {
    return dep;
  }
}

interface ScopedDependency {

}

class ScopedDependencyImpl  implements ScopedDependency{

}