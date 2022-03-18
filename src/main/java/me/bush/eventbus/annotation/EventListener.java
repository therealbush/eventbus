package me.bush.eventbus.annotation;

import me.bush.eventbus.bus.EventBus;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>An annotation that tells the eventbus which methods are listeners. Can be used on static and instance methods.
 * The object the annotated method is in must be subscribed for events to be posted to this listener, by using
 * {@link EventBus#subscribe}.
 *
 * @author bush
 * @since fall 2021
 */
@Documented
@Target(value = METHOD)
@Retention(value = RUNTIME)
public @interface EventListener {

    /**
     * The priority that determines the order that listener methods are invoked.
     */
    ListenerPriority priority() default ListenerPriority.NORMAL;

    /**
     * Whether or not this listener will be invoked when an event was previously cancelled.
     */
    boolean recieveCancelled() default false;
}
