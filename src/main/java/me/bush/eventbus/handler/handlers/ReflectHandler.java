package me.bush.eventbus.handler.handlers;

import me.bush.eventbus.annotation.EventListener;
import me.bush.eventbus.event.Event;
import me.bush.eventbus.handler.Handler;
import me.bush.eventbus.util.Util;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * @author bush
 * @since 11/23/2021
 */
public class ReflectHandler extends Handler {

    /**
     * The listener method to invoke.
     */
    private final Method listener;

    /**
     * Simplest invocation type, but not the fastest. Uses {@link Method#invoke} to invoke listeners.
     *
     * @param listener   A method with an {@link EventListener} annotation.
     * @param subscriber The object or class that the listener belongs to.
     * @param logger     The consumer to use for error messages.
     * @see Handler
     */
    public ReflectHandler(Method listener, Object subscriber, Consumer<String> logger) {
        super(listener, subscriber, logger);
        this.listener = listener;
    }

    @Override
    public void invoke(Event event) {
        try {
            // Invoke method
            this.listener.invoke(this.subscriber, event);
        } catch (Exception exception) {
            // Log errors
            Util.logReflectionExceptions(exception, Util.formatMethodName(this.listener), this.logger);
            exception.printStackTrace();
        }
    }
}
