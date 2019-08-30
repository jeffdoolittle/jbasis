package jbasis.ioc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jbasis.logging.Log4JLogger;
import jbasis.logging.LoggerFactory;

public class RegistryTest {

  @BeforeAll public static void beforeAll() {
    LoggerFactory.registerFactory(cls -> new Log4JLogger(cls));
  }

  @Test public void can_create_composite_registry() {
    Container c = new ContainerImpl(cfg -> cfg.apply(CompositeRegistry.class));
    assertEquals("value", c.resolve(String.class));
    assertEquals(1, (int)c.resolve(Integer.class));
    c.close();
  }
}

class CompositeRegistry extends Registry {
  public CompositeRegistry() {
    includeRegistry(new StringRegistry());
    includeRegistry(new IntRegistry());
  }
}

class StringRegistry extends Registry {
  public StringRegistry() {
    register(cfg -> cfg.addSingleton(String.class, c -> "value"));
  }
}

class IntRegistry extends Registry {
  public IntRegistry() {
    register(cfg -> cfg.addSingleton(Integer.class, c -> 1));
  }
}