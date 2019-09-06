package jbasis.interception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HandleError {
  /**
   * Provide the interceptor type to be used for error handling.
   * 
   * @return .
   */
  Class<? extends Interceptor> withInterceptor() default EmptyInterceptor.class;
}
