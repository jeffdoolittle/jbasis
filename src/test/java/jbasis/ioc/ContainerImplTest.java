package jbasis.ioc;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Closeable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jbasis.ioc.interception.EmptyInterceptor;
import jbasis.ioc.interception.HandleError;
import jbasis.ioc.interception.Profile;
import jbasis.ioc.interception.Retry;
import jbasis.ioc.interception.WithInterceptor;
import jbasis.logging.Log4JLogger;
import jbasis.logging.LoggerFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ContainerImplTest {

  static Container _container;

  @BeforeAll public static void before() {
    LoggerFactory.registerFactory(cls -> new Log4JLogger(cls));
    _container = new ContainerImpl(cfg -> cfg.apply(TestRegistry.class));
  }

  @Test public void can_configure_container_with_registry_instance() {
    Container container = new ContainerImpl(cfg -> cfg.apply(new TestRegistry()));
    Optional<String> value = container.tryResolve(String.class);
    assertEquals("default", value.get());
    container.close();
  }

  @Test public void can_handle_not_registered_service() {
    Optional<NotRegistered> onr = _container.tryResolve(NotRegistered.class);
    
    assertTrue(!onr.isPresent());

    Exception ex = null;
    try {
      _container.resolve(NotRegistered.class);
    } catch (Exception e) {
      ex = e;
    }

    assertNotNull(ex);
  }

  @Test public void can_profile_slow_service() {
    SlowService svc = _container.resolve(SlowService.class);
    svc.run();
    assertTrue(true); // need to add ability to test logging output
  }

  @Test public void can_resolve_singleton_service() {
    SingletonService instanceSS = _container.resolve(SingletonService.class);
    SingletonService otherSS = _container.resolve(SingletonService.class);
    instanceSS.exec();
    otherSS.exec();

    assertEquals(instanceSS, otherSS);
  }

  @Test public void can_resolve_transient_service() {
    TransientService instanceTS = _container.resolve(TransientService.class);
    TransientService otherTS = _container.resolve(TransientService.class);
    instanceTS.exec();
    otherTS.exec();

    assertNotEquals(instanceTS, otherTS);
  }
  
  @Test public void can_resolve_class_service() {
    ClassService svc = _container.resolve(ClassService.class);
    assertNotNull(svc);
  }

  @Test public void can_intercept_errors() {
    ErrorService svc = _container.resolve(ErrorService.class);

    svc.error(1);
    svc.error(2);
    svc.error(3);

    Exception e = null;
    try {
      svc.error(4);
    } catch (RuntimeException re) {
      e = re;
    }    
    assertNotNull(e);
  }

  @Test public void can_retry_on_errors() {
    RetryService svc = _container.resolve(RetryService.class);
    
    svc.succeedOnLastTry();

    Throwable lastThrowable = null;
    try {
      svc.failOnLastTry();
    } catch (Throwable t) {
      lastThrowable = t;
    }

    assertNotNull(lastThrowable);
  }

  @Test public void exception_resolving_annotation_with_constructorless_interceptor() {
    BadService svc = _container.resolve(BadService.class);
    assertThrows(JBasisException.class, () -> svc.bad());
  }

  @Test public void try_resolve_returns_empty_when_service_not_registered() {
    Optional<Float> f = _container.tryResolve(Float.class);
    assertEquals(Optional.empty(), f);
  }

  @Test public void try_resolve_returns_empty_when_service_resolution_throws() {
    Optional<Exception> f = _container.tryResolve(Exception.class);
    assertEquals(Optional.empty(), f);
  }

  @Test public void can_resolve_concrete_transient_service() {
    Date d = _container.resolve(Date.class);
    assertNotNull(d);
  }

  @Test public void exception_resolving_annotion_without_interceptor_type() {
    AnotherBadService svc = _container.resolve(AnotherBadService.class);
    assertThrows(JBasisException.class, () -> svc.exec());
  }
  
  @Test public void exception_when_registry_constructor_is_not_accessible() {
    assertThrows(JBasisException.class, () -> new ContainerImpl(cfg -> cfg.apply(BadRegistry.class)));
    
  }

  /**
   * javadoc.
   */
  @AfterAll public static void after() {
    AutoCloseableService acs = _container.resolve(AutoCloseableService.class);
    CloseableService cs = _container.resolve(CloseableService.class);

    _container.close();

    assertTrue(acs.isClosed());
    assertTrue(cs.isClosed());
    
    System.out.println("all good!");
    System.out.println();
  }
}

interface SlowService {
  void run();
}

class SlowServiceImpl implements SlowService {
  @Profile(thresholdMilliseconds = 5)
  public void run() {
    await().atLeast(25, TimeUnit.MILLISECONDS);
  }
}

class ClassService {}

interface NotRegistered {}


interface SingletonService {
  void exec();
}

class SingletonServiceImpl implements SingletonService {
  @Override
  @Profile(thresholdMilliseconds = 1)
  public void exec() {}
}

interface TransientService {
  void exec();
}

class TransientServiceImpl implements TransientService {
  @Override
  @Profile(thresholdMilliseconds = 1000)
  public void exec() {}
}

interface CloseableService extends Closeable {
  boolean isClosed();
}

class CloseableServiceImpl implements CloseableService {

  private boolean closed;

  @Override
  public void close() {
    this.closed = true;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }
}

interface AutoCloseableService extends AutoCloseable {
  boolean isClosed();
}

class AutoCloseableServiceImpl implements AutoCloseableService {

  private boolean closed;

  @Override
  public void close() throws Exception {
    this.closed = true;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }
}

interface ErrorService {
  void error(int value);
}

class ErrorServiceImpl implements ErrorService {

  @Override  
  @HandleError(withInterceptor = ErrorHandler.class)
  @Profile
  public void error(int value) {
    if (value % 2 == 0) {
      throw new RuntimeException("No even numbers!!!");
    }
  }
}

class ErrorHandler extends EmptyInterceptor {
  public ErrorHandler(HandleError annotation) {
  }

  @Override
  public boolean onError(Object proxy, Object target, Method method, Object[] args, Throwable throwable) {
    this.getContainer().resolve(String.class);
    if (args[0].equals(4))
    {
      return false;
    }
    return true;
  }
}

interface RetryService {
  void succeedOnLastTry();  
  void failOnLastTry();
}

class RetryServiceImpl implements RetryService {

  int succeedTries = 0;
  int failTries = 0;

  @Override
  @Profile
  @Retry(tries = 6, backOffIntervalMilliseconds = 2, exponentialBackOff = true)
  public void succeedOnLastTry() {
    succeedTries++;
    if (succeedTries < 6) {
      throw new RuntimeException("Try " + succeedTries);
    }
  }

  @Override
  @Profile
  @Retry(tries = 4, backOffIntervalMilliseconds = 4)
  public void failOnLastTry() {
    failTries++;
    throw new RuntimeException("Try " + failTries);
  }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithInterceptor(type = InterceptorWithoutConstructorReceivingAnnotation.class)
@interface AnnotationWithNoConstructorInterceptor {

}

class InterceptorWithoutConstructorReceivingAnnotation extends EmptyInterceptor {

}

interface BadService {
  void bad();
}

class BadServiceImpl implements BadService {

  @Override
  @AnnotationWithNoConstructorInterceptor
  public void bad() {

  }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface AnnotationWithoutInterceptorType {

}

interface AnotherBadService {
  void exec();
}

class AnotherBadServiceImpl implements AnotherBadService {

  @Override
  @AnnotationWithoutInterceptorType
  public void exec() {
  }
}

class BadRegistry extends Registry {
  private BadRegistry() {}
}