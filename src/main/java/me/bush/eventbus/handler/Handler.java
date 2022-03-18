package me.bush.eventbus.handler;

import me.bush.eventbus.annotation.EventListener;
import me.bush.eventbus.annotation.ListenerPriority;
import me.bush.eventbus.bus.EventBus;
import me.bush.eventbus.event.Event;
import me.bush.eventbus.handler.handlers.ASMHandler;
import me.bush.eventbus.handler.handlers.LambdaHandler;
import me.bush.eventbus.handler.handlers.ReflectHandler;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * @author bush
 * @since 11/25/2021
 */
public abstract class Handler {

    /**
     * The priority of this handler's listener.
     */
    private final ListenerPriority priority;

    /**
     * If this handler should recieve cancelled events.
     */
    private final boolean receiveCancelled;

    /**
     * The object or class this handler's listener is in.
     */
    protected final Object subscriber;

    /**
     * The consumer to send errors to.
     */
    protected final Consumer<String> logger;

    /**
     * Base class for handling invocation of event listeners.
     * Contains basic methods for sorting, unsubscribing, etc.
     *
     * @param listener   A method with an {@link EventListener} annotation.
     * @param subscriber The object or class that the listener belongs to.
     * @param logger     The logger to use for error messages.
     * @see ASMHandler
     * @see LambdaHandler
     * @see ReflectHandler
     */
    public Handler(Method listener, Object subscriber, Consumer<String> logger) {
        // Lets java ignore some security checks (listeners should always be public)
        listener.setAccessible(true);
        // Get info
        EventListener annotation = listener.getAnnotation(EventListener.class);
        this.priority = annotation.priority();
        this.receiveCancelled = annotation.recieveCancelled();
        this.subscriber = subscriber;
        this.logger = logger;
    }

    /**
     * Called in {@link EventBus#post}.
     *
     * @param event The {@link Event} to send to this handler's listener.
     */
    public abstract void invoke(Event event);

    /**
     * Called in {@link EventBus#subscribe} to sort handlers.
     *
     * @return The priority specified in {@link EventListener#priority}.
     */
    public ListenerPriority getPriority() {
        return this.priority;
    }

    /**
     * Called in {@link EventBus#post} to check if a handler should recieve an event if it was cancelled.
     *
     * @return The recieveability specified in {@link EventListener#recieveCancelled}.
     */
    public boolean shouldRecieveCancelled() {
        return this.receiveCancelled;
    }

    /**
     * Called in {@link EventBus#unsubscribe} to find handlers with a matching subscriber.
     *
     * @return True if the given subscriber matches this handler's subscriber.
     */
    public boolean isSubscriber(Object object) {
        return this.subscriber.equals(object);
    }
}
