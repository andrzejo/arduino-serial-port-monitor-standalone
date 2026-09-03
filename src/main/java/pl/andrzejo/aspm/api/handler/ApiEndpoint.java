/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.api.handler;


import pl.andrzejo.aspm.api.server.SimpleHttpServer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ApiEndpoint {
    String path() default "";

    int order() default 0;

    SimpleHttpServer.Method method() default SimpleHttpServer.Method.Get;

    String description() default "";

    String bodyExample() default "";

    String queryParams() default "";
}
