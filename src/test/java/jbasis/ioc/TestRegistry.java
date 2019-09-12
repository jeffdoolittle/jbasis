package jbasis.ioc;

import java.time.Instant;
import java.util.Date;

import jbasis.util.JBasisException;

class TestRegistry extends Registry {
  public TestRegistry() {
    register(cfg -> {
      cfg.addSingleton(String.class, x -> "default");
      cfg.addSingleton(SlowService.class, x -> new SlowServiceImpl());
      cfg.addSingleton(SingletonService.class, x -> new SingletonServiceImpl());
      cfg.addTransient(TransientService.class, x -> new TransientServiceImpl());
      cfg.addTransient(Date.class, x -> Date.from(Instant.now()));
      cfg.addSingleton(CloseableService.class, x -> new CloseableServiceImpl());
      cfg.addSingleton(AutoCloseableService.class, x -> new AutoCloseableServiceImpl());
      cfg.addSingleton(ClassService.class, x -> new ClassService());
      cfg.addSingleton(ErrorService.class, x -> new ErrorServiceImpl());
      cfg.addSingleton(RetryService.class, x -> new RetryServiceImpl());
      cfg.addSingleton(BadService.class, x -> new BadServiceImpl());
      cfg.addSingleton(AnotherBadService.class, x -> new AnotherBadServiceImpl());
      cfg.addSingleton(Exception.class, x -> {
        throw new JBasisException("simulate service resolution failure");
      });
      cfg.addSingleton(ServiceToShortCircuit.class, ServiceToShortCircuitImpl.class);
    });
  }
}