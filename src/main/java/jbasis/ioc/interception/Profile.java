package jbasis.ioc.interception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import jbasis.logging.Level;
import jbasis.logging.Logger;
import jbasis.logging.LoggerFactory;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithInterceptor(type = ProfileInterceptor.class)
public @interface Profile {
  String loggerDefaultPriority() default "DEBUG";

  String loggerThresholdExceededPriority() default "WARN";

  long thresholdMilliseconds() default -1L;
}

class ProfileInterceptor extends EmptyInterceptor {
  private final Logger logger = LoggerFactory.get(getClass());
  private Profile profile;
  private long start;

  public ProfileInterceptor(Profile profile) {
    this.profile = profile;
  }

  @Override
  public void beforeInvoke(Object proxy, Object target, Method method, Object[] args) {
    String msg = String.format("Executing %s.%s", target, method.getName());
    Level level = Level.valueOf(profile.loggerDefaultPriority());
    logger.log(level, msg);
    start = System.nanoTime();
  }

  @Override
  public boolean onError(Object proxy, Object target, Method method, Object[] args, 
      Throwable throwable) {
    return false;
  }

  @Override
  public void afterInvoke(Object proxy, Object target, Method method, Object[] args, 
      Object result) {
    long elapsedMicros = (System.nanoTime() - start) / 1000;
    long elapsedMillis = elapsedMicros / 1000;
    long threshold = profile.thresholdMilliseconds();
    threshold = threshold < 0 ? 0 : threshold;
    String thresholdConfig = threshold > 0 ? String.format(" (threshold = %s ms)", threshold) : "";
    String msg = String.format("Executed %s.%s in %s μs%s", target,
        method.getName(), elapsedMicros, thresholdConfig);
    if (threshold > 0 && elapsedMillis > threshold) {
      Level level = Level.valueOf(profile.loggerThresholdExceededPriority());
      logger.log(level, msg);
    } else {
      Level level = Level.valueOf(profile.loggerDefaultPriority());
      logger.log(level, msg);
    }
  }
}
