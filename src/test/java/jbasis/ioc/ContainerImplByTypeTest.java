package jbasis.ioc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jbasis.logging.Log4JLogger;
import jbasis.logging.LoggerFactory;

public class ContainerImplByTypeTest {

  @BeforeAll public static void beforeAll () {
    LoggerFactory.registerFactory(cls -> new Log4JLogger(cls));
  }

  @Test public void can_resolve_singleton_service_with_dependency() {
    Container c = new ContainerImpl(cfg -> cfg.apply(new ContainerImplByTypeTestRegistry()));

    Singleton s1 = c.resolve(Singleton.class);
    s1.exec();
    Singleton s2 = c.resolve(Singleton.class);
    s2.exec();

    assertEquals(s1, s2);

    Transient t1 = c.resolve(Transient.class);
    t1.exec();
    Transient t2 = c.resolve(Transient.class);
    t2.exec();

    assertNotEqual(t1, t2);
    
    Dependency d1 = c.resolve(Dependency.class);
    assertEquals(4, d1.callCount());

    Dependency d2 = c.resolve(Dependency.class);

    assertEquals(d1, d2);

    Dependency d3 = s1.getDependency();
    Dependency d4 = t1.getDependency();

    assertEquals(d3, d4);

    c.close();
  }
  
  private void assertNotEqual(Transient t1, Transient t2) {
}

    
public interface Dependency {
    void exec();
    int callCount();
  }

  public class DependencyImpl implements Dependency {
    private int callCount;

    @Override
    public void exec() {
      this.callCount++;
    }
    
    @Override
    public int callCount() {
      return this.callCount;
    }
  }

  public interface Singleton {
    void exec();
    Dependency getDependency();
  }

  public interface Scoped {
    void exec();
  }

  public interface Transient {
    void exec();
    Dependency getDependency();
  }

  public class SingletonImpl implements Singleton {

    private Dependency dependency;
    private String value;
    private Transient _transient;

    public SingletonImpl(Dependency dependency, String value, Transient _transient) {
      this.dependency = dependency;
      this.value = value;
      this._transient = _transient;

    }

    @Override
    public void exec() {
      dependency.exec();
    }
 
    public Dependency getDependency() {
      return dependency;      
    }
  }

  public class ScopedImpl implements Scoped {

    private Dependency dependency;

    public ScopedImpl(Dependency dependency) {
      this.dependency = dependency;

    }

    @Override
    public void exec() {
      dependency.exec();
    }
  }

  public class TransientImpl implements Transient {

    private Dependency dependency;

    public TransientImpl(Dependency dependency) {
      this.dependency = dependency;

    }

    @Override
    public void exec() {
      dependency.exec();
    }

    public Dependency getDependency() {
      return dependency;      
    }
  }

  class ContainerImplByTypeTestRegistry extends Registry {
    public ContainerImplByTypeTestRegistry() {
      register(cfg -> {
        cfg.addSingleton(Singleton.class, SingletonImpl.class);
        cfg.addSingleton(Dependency.class, DependencyImpl.class);
        cfg.addScoped(Scoped.class, ScopedImpl.class);
        cfg.addTransient(Transient.class, TransientImpl.class);
      });
    }
  }
}