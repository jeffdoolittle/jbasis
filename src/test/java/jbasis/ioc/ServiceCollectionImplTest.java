package jbasis.ioc;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    collection.add(String.class, ServiceLifetime.SINGLETON, x -> "hello, container!");

    ServiceDescriptor descriptor = (ServiceDescriptor)StreamSupport
        .stream(collection.spliterator(), false).toArray()[0];

    assertEquals(ServiceLifetime.SINGLETON, descriptor.getServiceLifetime());
    assertEquals(String.class, descriptor.getServiceType());
    assertEquals("hello, container!", descriptor.getFactory().apply(null));
  }

  @Test public void last_registered_wins_when_registering_the_same_service_twice() {
    ServiceCollection collection = new ServiceCollectionImpl();

    collection.add(String.class, ServiceLifetime.SINGLETON, x -> "one");
    collection.add(String.class, ServiceLifetime.SINGLETON, x -> "two");

    ServiceDescriptor sd = StreamSupport.stream(collection.spliterator(), false)
    .filter(x -> x.getServiceType() == String.class)
    .findFirst()
    .get();

    String value = (String)sd.getFactory().apply(null);

    assertEquals("two", value);
  }
  
  @Test public void last_registered_wins_when_registering_the_same_service_twice_by_type() {
    ServiceCollection collection = new ServiceCollectionImpl();

    collection.add(SomeService.class, ServiceLifetime.SINGLETON, SomeServiceOneImpl.class);
    collection.add(SomeService.class, ServiceLifetime.SINGLETON, SomeServiceTwoImpl.class);

    ServiceDescriptor sd = StreamSupport.stream(collection.spliterator(), false)
    .filter(x -> x.getServiceType() == SomeService.class)
    .findFirst()
    .get();

    assertEquals(sd.getImplementationType(), SomeServiceTwoImpl.class); 
  }

  public interface SomeService {}
  public class SomeServiceOneImpl implements SomeService {}
  public class SomeServiceTwoImpl implements SomeService {}
}