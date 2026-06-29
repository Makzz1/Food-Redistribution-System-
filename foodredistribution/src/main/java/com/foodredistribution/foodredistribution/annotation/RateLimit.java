package com.foodredistribution.foodredistribution.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to apply rate limiting to controllers or specific endpoints.
 * If applied at the class level, all endpoints in the controller inherit the limit.
 * Method-level annotations override class-level annotations.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * The maximum number of allowed requests within the time window.
     */
    int requests();

    /**
     * The time window in seconds.
     */
    int window();

    /**
     * Optional key to group rate limits across different endpoints.
     */
    String key() default "";
}

