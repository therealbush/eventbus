package me.bush.eventbus.event;

import me.bush.eventbus.annotation.EventListener;
import me.bush.eventbus.bus.EventBus;

/**
 * @author bush
 * @since fall 2021
 */
public abstract class Event {

    /**
     * Whether or not this event will be sent to listeners without {@link EventListener#recieveCancelled}.
     */
    private boolean cancelled;

    /**
     * Called in {@link EventBus#post} to determine if a listener method should be invoked,
     * based on {@link EventListener#recieveCancelled}.
     *
     * @return True if this event is cancelled.
     */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Sets this event to cancelled, so only {@link EventListener#recieveCancelled} future listeners
     * will be invoked. You can un-cancel an event, and all future listeners will be invoked.
     *
     * @param cancelled The cancelled state to set for the {@link Event}.
     */
    public void setCancelled(boolean cancelled) {
        if (this.isCancellable()) {
            this.cancelled = cancelled;
        }
    }
    
    /**
     * Sets this event to cancelled, so only {@link EventListener#recieveCancelled} future listeners
     * will be invoked.
     */
    public void cancel() {
        if (this.isCancellable()) {
            this.cancelled = true;
        }
    }

    /**
     * Implementation is required to determine cancellability.
     *
     * @return Cancellability of the {@link Event}.
     */
    protected abstract boolean isCancellable();
}
