package jbasis.interception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import jbasis.logging.Logger;
import jbasis.logging.LoggerFactory;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithInterceptor(type = RetryInterceptor.class)
public @interface Retry {

  /**
   * Sets the number of tries for the annotated method.
   * @return the number of tries
   */
  int tries() default 1;

  /**
   * Sets the number of milliseconds to back off.
   * @return the number of backoff milliseconds
   */
  long backOffIntervalMilliseconds() default 1000;

  /**
   * Sets whether to increase the backoff exponentially.
   * @return true or false
   */
  boolean exponentialBackOff() default false;
}

class RetryInterceptor extends EmptyInterceptor {
  private final Logger logger = LoggerFactory.get(getClass());
  private int tries = 1;
  private Retry retry;

  public RetryInterceptor(Retry retry) {
    this.retry = retry;
  }

  @Override
  public boolean onError(Object proxy, Object target, Method method, Object[] args, 
      Throwable throwable) {
    Exception lastThrowable = new Exception(new Exception("default"));
    logger.warn("Failed on first attempt. Will try a total of {} times.", retry.tries());

    logger.debug("Initiating {} backoff strategy", retry.exponentialBackOff() 
        ? "exponential" : "linear");

    while (true) {
      if (tries == retry.tries()) {
        String msg = String.format("Failed on try %s. %s", tries, 
            lastThrowable.getCause().getMessage());
        logger.error(msg, lastThrowable.getCause());
        return false;
      }

      backoff();
  
      try {
        method.invoke(target, args);
        logger.info("Succeeded on try {}", tries + 1);
        return true;
      } catch (Exception t) {
        lastThrowable = t;
      }

      tries++;
    }
  }

  private void backoff() {
    try {
      long backoff = retry.backOffIntervalMilliseconds();
      if (retry.exponentialBackOff()) {
        backoff = backoff * (int)Math.pow(2, tries - (double)1);
      } else {
        backoff = backoff * tries;
      }
      logger.debug("Backing off by {} ms", backoff);
      TimeUnit.MILLISECONDS.sleep(backoff);
    } catch (InterruptedException e) {
      logger.warn("thread sleep was interrupted");
      Thread.currentThread().interrupt();
    }  
  }
}