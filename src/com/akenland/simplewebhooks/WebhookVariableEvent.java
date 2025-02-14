package com.akenland.simplewebhooks;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a variable needs to be replaced in a webhook.
 * <p>
 * Plugins may listen for this event to replace variables in a webhook with
 * their own content.
 * <p>
 * This event is called individually for each unique variable in a webhook. For
 * example, a single webhook with two unique variables will trigger this event
 * twice. This event will not be called multiple times if the same variable is
 * used multiple times in the same string.
 */
public class WebhookVariableEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final String variable;
    private final Event triggeringEvent;
    private String replacement;

    /**
     * Creates a new WebhookVariableEvent.
     * 
     * @param variable        the name of the variable to replace
     * @param triggeringEvent the event that triggered this webhook, may be null
     * @param isAsync         set to true if the event should be called
     *                        asynchronously, this may be needed if the trigger was
     *                        fired from an async event
     */
    WebhookVariableEvent(String variable, Event triggeringEvent, boolean isAsync) {
        super(isAsync);
        this.variable = variable;
        this.triggeringEvent = triggeringEvent;
    }

    /**
     * Gets the name of the variable to be replaced.
     * <p>
     * Your plugin should use this to determine if it needs to replace the variable.
     * <p>
     * Will not include the brackets {{ }}.
     * 
     * @return the name of the variable
     */
    public String getVariable() {
        return variable;
    }

    /**
     * Gets the event that triggered this webhook.
     * <p>
     * This can be used to get the context of what triggered the webhook. For
     * example, if this webhook was triggered by a chat message from a player, this
     * may be an AyncPlayerChatEvent, allowing you to get the player who sent the
     * message, and the message itself.
     * <p>
     * The event type is not guaranteed to be known, so you should check the type of
     * the event before using it.
     * <p>
     * You should not modify the event in any way. The event will typically be
     * retrieved using MONITOR priority, so other plugins may have already modified
     * it. If you must modify the event, you should listen for the event directly
     * and using an appropriate priority.
     * <p>
     * If this webhook was not triggered by an event, this will be null.
     * 
     * @return the event that triggered this webhook, may be null
     */
    public Event getTriggeringEvent() {
        return triggeringEvent;
    }

    /**
     * Sets the replacement for the variable.
     * <p>
     * If your plugin is replacing the variable, you should set the replacement
     * here.
     * <p>
     * If multiple listeners call this method, the last one to set the replacement
     * will be used. Use event handler priorities as needed.
     * 
     * @param replacement the replacement for the variable
     */
    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    /**
     * Returns true if the variable has been replaced by a plugin.
     * <p>
     * This will also return false if a listener has set the replacement to null.
     */
    boolean hasBeenReplaced() {
        return replacement != null;
    }

    /**
     * Gets the replacement for the variable.
     * <p>
     * If the variable has not been replaced, this will return null.
     * 
     * @return the replacement for the variable, or null if it has not been replaced
     */
    String getReplacement() {
        return replacement;
    }

}