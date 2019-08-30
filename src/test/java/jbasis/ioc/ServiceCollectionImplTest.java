package jbasis.ioc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.StreamSupport;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jbasis.logging.Log4JLogger;
import jbasis.logging.LoggerFactory;

public class ServiceCollectionImplTest {

  @BeforeAll public static void beforeAll () {
    LoggerFactory.registerFactory(cls -> new Log4JLogger(cls));
  }

  @Test public void can_register_and_retrieve_service() {
    
    ServiceCollection collection = new ServiceCollectionImpl();

    collection.addSingleton(String.class, x -> "hello, container!");

    ServiceDescriptor descriptor = (ServiceDescriptor)StreamSupport
        .stream(collection.spliterator(), false).toArray()[0];

    assertEquals(ServiceLifetime.SINGLETON, descriptor.getServiceLifetime());
    assertEquals(String.class, descriptor.getServiceType());
    assertEquals("hello, container!", descriptor.getFactory().apply(null));
  }

  @Test public void last_registered_wins_when_registering_the_same_service_twice() {
    ServiceCollection collection = new ServiceCollectionImpl();

    collection.addSingleton(String.class, x -> "one");
    collection.addSingleton(String.class, x -> "two");

    ServiceDescriptor sd = StreamSupport.stream(collection.spliterator(), false)
    .filter(x -> x.getServiceType() == String.class)
    .findFirst()
    .get();

    String value = (String)sd.getFactory().apply(null);

    assertEquals("two", value);
 }
}