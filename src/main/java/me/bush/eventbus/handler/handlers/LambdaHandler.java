package me.bush.eventbus.handler.handlers;

import me.bush.eventbus.annotation.EventListener;
import me.bush.eventbus.event.Event;
import me.bush.eventbus.handler.DynamicHandler;
import me.bush.eventbus.handler.Handler;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author bush
 * @since fall 2021
 */
public class LambdaHandler extends Handler {

    /**
     * Caches dynamic handlers to avoid recreation.
     */
    private static final ConcurrentHashMap<Method, DynamicHandler> handlerCache = new ConcurrentHashMap<>();

    /**
     * The dynamically generated lambda object that invokes this handler's listener.
     */
    private final DynamicHandler dynamicHandler;

    /**
     * Very fast invocation style. Uses {@link LambdaMetafactory} to create lambda objects that implement {@link DynamicHandler}.
     *
     * @param listener   A method with an {@link EventListener} annotation.
     * @param subscriber The object or class that the listener belongs to.
     * @param logger     The consumer to use for error messages.
     * @throws Throwable If there was an exception in the target method, or an error creating the lambda object.
     * @see Handler
     */
    public LambdaHandler(Method listener, Object subscriber, Consumer<String> logger) throws Throwable {
        super(listener, subscriber, logger);
        // Make sure cache doesn't already have a handler for this listener
        if (handlerCache.containsKey(listener)) this.dynamicHandler = handlerCache.get(listener);
        else {
            // Get lookup instance
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            // Check method modifiers for static
            boolean isStatic = Modifier.isStatic(listener.getModifiers());
            // Create methodtype for invoking the methodhandle
            MethodType targetSignature = MethodType.methodType(DynamicHandler.class);
            // Generate callsite
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup, // The lookup instance to use
                    "invoke", // The name of the method to implement
                    isStatic ? targetSignature : targetSignature.appendParameterTypes(subscriber.getClass()), // The signature for .invoke()
                    MethodType.methodType(void.class, Event.class), // The method signature to implement
                    lookup.unreflect(listener), // Method to invoke when called
                    MethodType.methodType(void.class, listener.getParameterTypes()[0]) // Signature that is enforced at runtime
            );
            // Get target to invoke
            MethodHandle target = callSite.getTarget();
            // Invoke on the object if not static
            this.dynamicHandler = (DynamicHandler) (isStatic ? target.invoke() : target.invoke(subscriber));
            // Cache this dynamic handler
            handlerCache.put(listener, this.dynamicHandler);
        }
    }

    @Override
    public void invoke(Event event) {
        // Invoke lambda through dynamic handler interface
        this.dynamicHandler.invoke(event);
    }
}
