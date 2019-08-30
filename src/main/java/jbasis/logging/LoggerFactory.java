package jbasis.logging;

import java.util.function.Function;

/**
 * Before using jbasis, a logger factory must be registered by calling
 * `registerFactory` on the `LoggerFactory`
 */
public class LoggerFactory {
  private LoggerFactory() {}

  static Function<Class<?>, Logger> loggerFactoryFunction;

  /**
   * Registers a factory that will be used to resolve loggers by
   * class type
   * 
   * @param factory the factory to register
   */
  public static void registerFactory(Function<Class<?>, Logger> factory) {
    loggerFactoryFunction = factory;
  }

  /**
   * Gets a logger instance for the specified class
   * 
   * @param cls the class type
   * @return a logger instance for the specified class
   */
  public static Logger get(Class<?> cls) {
    if (loggerFactoryFunction == null) {
      throw new UnsupportedOperationException("You must configure a logger factory with `registryFactory`");
    }
    return loggerFactoryFunction.apply(cls);
  }
}