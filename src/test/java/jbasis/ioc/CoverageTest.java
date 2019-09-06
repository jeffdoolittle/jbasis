package jbasis.ioc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import jbasis.interception.EmptyInterceptor;
import jbasis.logging.Level;
import jbasis.logging.Log4JLogger;
import jbasis.logging.LoggerFactory;
import jbasis.util.JBasisException;

public class CoverageTest {

  @Test public void coverage() {
    LoggerFactory.registerFactory(cls -> new Log4JLogger(cls));

    JBasisException ex = new JBasisException("message", new Exception("cause"));
    assertEquals("cause", ex.getCause().getMessage());

    EmptyInterceptor ei = new EmptyInterceptor() {
    };
    assertFalse(ei.onError(null, null, null, null, null));

    ServiceCollectionImpl sci = new ServiceCollectionImpl();
    sci.forEach(x -> {});

    Level[] possibleValues = Level.DEBUG.getDeclaringClass().getEnumConstants();
    for (Level l : possibleValues) {
      assertNotNull(l.abbreviation());
    }
  }

  @Test public void unsupported_getting_logger_when_no_factory_is_registered() {
    LoggerFactory.registerFactory(null);
    assertThrows(UnsupportedOperationException.class, () -> LoggerFactory.get(getClass()));
  }
}