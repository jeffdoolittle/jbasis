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
    Registry r = new Registry(){};
    r.register(cfg -> {
      cfg.addSingleton(Singleton.class, SingletonImpl.class);
      cfg.addSingleton(Dependency.class, DependencyImpl.class);
      cfg.addScoped(Scoped.class, ScopedImpl.class);
      cfg.addTransient(Transient.class, TransientImpl.class);
    });

    Container c = new ContainerImpl(cfg -> cfg.apply(r));

    Singleton s1 = c.resolve(Singleton.class);
    s1.exec();
    Singleton s2 = c.resolve(Singleton.class);
    s2.exec();

    assertEquals(s1, s2);

    Dependency d1 = c.resolve(Dependency.class);
    assertEquals(2, d1.callCount());

    Dependency d2 = c.resolve(Dependency.class);

    assertEquals(d1, d2);

    c.close();
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
  }

  public interface Scoped {
    void exec();
  }

  public interface Transient {
    void exec();
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
  }
}