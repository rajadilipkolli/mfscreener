package com.learning.mfscreener.config.logging;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.boot.logging.LogLevel;

@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface Loggable {

    boolean params() default true;

    boolean result() default true;

    LogLevel value() default LogLevel.DEBUG;
}
