package me.bush.eventbus.bus;

import me.bush.eventbus.annotation.EventListener;
import me.bush.eventbus.event.Event;
import me.bush.eventbus.handler.Handler;
import me.bush.eventbus.handler.handlers.LambdaHandler;
import me.bush.eventbus.handler.handlers.ReflectHandler;
import me.bush.eventbus.util.Util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author bush
 * @since fall 2021
 */
public class EventBus {
    // To avoid confusion:
    // Listener = The method with the @EventListener annotation
    // Handler = The object that handles listener invocation
    // Subscriber = The object or class that a listener is in

    /**
     * A set for quickly checking if an object or class is already subscribed.
     */
    private final Set<Object> subscribers = Collections.synchronizedSet(new HashSet<>());

    /**
     * Maps handlers by event type, with one arraylist of handlers for each event type.
     */
    private Map<Class<?>, List<Handler>> handlerMap = new ConcurrentHashMap<>();

    /**
     * The current handler type to use when adding listeners from subscribers.
     */
    private Class<? extends Handler> handlerType;

    /**
     * The consumer to use for logging errors.
     */
    private final Consumer<String> errorLogger;

    /**
     * The consumer to use for logging debug info.
     */
    private final Consumer<String> infoLogger;

    /**
     * Creates an EventBus with the fastest handler type, and logs errors and info to console.
     */
    public EventBus() {
        this(LambdaHandler.class);
    }

    /**
     * Creates an EventBus with the specified handler type, and logs errors and info to console.
     *
     * @param handlerType The type of {@link Handler} to use.
     */
    public EventBus(Class<? extends Handler> handlerType) {
        this(handlerType, message -> System.out.println("[EVENTBUS]: " + message));
    }

    /**
     * Creates an EventBus with the fastest handler type, and logs errors and info to the specified consumer.
     *
     * @param messageLogger The consumer to use for errors and info messages.
     */
    public EventBus(Consumer<String> messageLogger) {
        this(LambdaHandler.class, messageLogger, messageLogger);
    }

    /**
     * Creates an EventBus with the fastest handler type, and logs errors and info to their respective consumers.
     *
     * @param errorLogger The consumer to use for error messages.
     * @param infoLogger  The consumer to use for info messages.
     */
    public EventBus(Consumer<String> errorLogger, Consumer<String> infoLogger) {
        this(LambdaHandler.class, errorLogger, infoLogger);
    }

    /**
     * Creates an EventBus with the specified listener type, and logs errors and info to the specified consumer.
     *
     * @param handlerType   The type of {@link Handler} to use.
     * @param messageLogger The consumer to use for errors and info messages.
     */
    public EventBus(Class<? extends Handler> handlerType, Consumer<String> messageLogger) {
        this(handlerType, messageLogger, messageLogger);
    }

    /**
     * Creates an EventBus with the specified handler type, and logs errors and info to their respective consumers.
     *
     * @param handlerType The type of {@link Handler} to use.
     * @param errorLogger The consumer to use for error messages.
     * @param infoLogger  The consumer to use for info messages.
     */
    public EventBus(Class<? extends Handler> handlerType, Consumer<String> errorLogger, Consumer<String> infoLogger) {
        this.handlerType = handlerType;
        this.errorLogger = errorLogger;
        this.infoLogger = infoLogger;
    }

    /**
     * Subsribes an object/class to the EventBus. Automatically finds listeners.<br>
     * - If you want static listeners to recieve events, you must subscribe the class the listener is in.<br>
     * - If you want non-static listeners to recieve events, you must subscribe the object the listener is in.
     *
     * @param subscriber An object or class to subscribe.
     */
    public void subscribe(Object subscriber) {
        if (subscriber == null || this.subscribers.contains(subscriber)) return;
        // Add subscriber to cache
        this.subscribers.add(subscriber);
        // Add handlers from subscriber
        this.addHandlers(subscriber);
    }

    /**
     * Posts an {@link Event} to the EventBus. Every listener with the given event
     * type is called, in order of {@link EventListener#priority}. If the event was cancelled by
     * a previous listener, only future listeners with {@link EventListener#recieveCancelled} will be invoked.
     *
     * @param event The {@link Event} to post.
     * @return True if the event was cancelled, false otherwise.
     */
    public boolean post(Event event) {
        if (event == null) return false;
        // Get list of handlers with this event type
        List<Handler> handlers = this.handlerMap.get(event.getClass());
        if (handlers == null) return false;
        // Invoke each handler (list is already sorted)
        for (Handler handler : handlers) {
            if (!event.isCancelled() || handler.shouldRecieveCancelled()) {
                handler.invoke(event);
            }
        }
        // Return true if the event was cancelled
        return event.isCancelled();
    }

    /**
     * Removes an object/class and it's listeners from the EventBus.
     *
     * @param subscriber The object/class to unsubscribe.
     */
    public void unsubscribe(Object subscriber) {
        if (subscriber == null || !this.subscribers.contains(subscriber)) return;
        // Remove from subscriber cache
        this.subscribers.remove(subscriber);
        // Get values from handlermap, remove handlers that are from this subscriber
        this.handlerMap.values().forEach(handlers -> handlers.removeIf(handler -> handler.isSubscriber(subscriber)));
        // Remove entry from handlermap if there are no handlers for an event type
        this.handlerMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Sends some basic info about the EventBus to the info logger.
     */
    public void getInfo() {
        // 25 spaces per entry, left aligned (-), format as string (s)
        String format = "%-25s%-25s";
        this.infoLogger.accept("============ EVENTBUS INFO ============");
        // Handler type
        this.infoLogger.accept(String.format(format, "Handler type", this.handlerType.getSimpleName()));
        // Subscriber count
        this.infoLogger.accept(String.format(format, "Subscriber count", this.subscribers.size()));
        // Get total listener count
        int total = this.handlerMap.values().stream().mapToInt(Collection::size).sum();
        // Log total
        this.infoLogger.accept(String.format(format, "Listener count", total));
        // For every key
        this.handlerMap.forEach((eventType, handlers) -> {
            // Get count of listeners
            int listenerCount = handlers.size();
            // Get name (Class#getSimpleName would just show "Post" instead of "SettingEvent$Post")
            String eventName = Util.formatClassName(eventType);
            // Log info
            this.infoLogger.accept(String.format(format, eventName, listenerCount));
        });
    }

    /**
     * Get the current {@link Handler} type being used.
     */
    public Class<? extends Handler> getHandlerType() {
        return this.handlerType;
    }

    /**
     * Changes the {@link Handler} type to the specified type.
     * Calling this will remake the handler map, and re-add every listener with the new type.
     */
    public void setHandlerType(Class<? extends Handler> handlerType) {
        if (this.handlerType == handlerType) return;
        this.handlerType = handlerType;
        // Reset handler map (.clear() doesn't remove entries, just sets them to null)
        this.handlerMap = new ConcurrentHashMap<>();
        // Re-add with new listener type (iterating over a hashset :\, but performance isn't important here)
        this.subscribers.forEach(this::addHandlers);
    }

    /**
     * Finds listener methods in an object or class, creates handlers from them, and adds them to the handler map.
     *
     * @param subscriber The object or class to search in.
     */
    private void addHandlers(Object subscriber) {
        // Check if an object or class is being subscribed
        boolean isClass = subscriber instanceof Class;
        // Get all public methods from object or class (including inherited methods)
        Arrays.stream((isClass ? (Class<?>) subscriber : subscriber.getClass()).getMethods())
                // Sort for only @EventListener methods
                .filter(method -> method.isAnnotationPresent(EventListener.class))
                // If the subscriber is a class object, only look for static methods, and vice versa
                .filter(method -> isClass == Modifier.isStatic(method.getModifiers()))
                .forEach(method -> {
                    // Get parameters 
                    Class<?>[] parameters = method.getParameterTypes();
                    // Check return type
                    if (method.getReturnType() != void.class) {
                        this.errorLogger.accept(method + " has an incorrect return type. Listeners must return void.");
                        return;
                    }
                    // Check parameter count and if the parameter is a subclass of event
                    if (parameters.length != 1 || !Event.class.isAssignableFrom(parameters[0])) {
                        this.errorLogger.accept(method + " has incorrect parameters. Listeners must have one parameter that is a subclass of Event.");
                        return;
                    }
                    // Get list of handlers for this event type. If it doesn't exist, make a new list
                    List<Handler> handlers = this.handlerMap.computeIfAbsent(parameters[0], v -> new CopyOnWriteArrayList<>());
                    // Add handler to the list
                    handlers.add(this.createHandler(method, subscriber));
                    // Sort the list
                    handlers.sort(Comparator.comparing(Handler::getPriority));
                });
    }

    /**
     * Creates a handler based on the current handler type. If an exception is caught, it defaults to {@link ReflectHandler}.
     *
     * @param method The listener method.
     * @param object The subscribing object or class.
     * @return A subclass of {@link Handler}.
     */
    private Handler createHandler(Method method, Object object) {
        try {
            // Create a new handler based on the current handler type
            return this.handlerType
                    .getDeclaredConstructor(Method.class, Object.class, Consumer.class)
                    .newInstance(method, object, this.errorLogger);
        } catch (Exception exception) {
            // Log exceptions that were thrown
            Util.logReflectionExceptions(exception, Util.formatClassName(this.handlerType), this.errorLogger);
            this.errorLogger.accept("Defaulting to ReflectHandler for listener method " + Util.formatMethodName(method) + ".");
            exception.printStackTrace();
            // Return most safe handler type
            return new ReflectHandler(method, object, this.errorLogger);
        }
    }
}
