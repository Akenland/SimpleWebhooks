package com.akenland.simplewebhooks;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Plugins may instantiate and call this event to trigger webhooks.
 * <p>
 * Most plugins will have no need to listen for this event, nor extend it.
 * <p>
 * Example usage:
 * 
 * <pre>
 * var event = new TriggerWebhookEvent("MY_TRIGGER", triggeringEvent);
 * getServer().getPluginManager().callEvent(event);
 * </pre>
 * <p>
 * This will trigger all webhooks registered to the trigger "MY_TRIGGER". You
 * can use any of the built-in triggers defined in config.yml, or any other
 * string.
 * For example, if your plugin sends custom chat messages, you may use
 * "PLAYER_CHAT" to trigger any existing chat webhooks that the server has
 * defined.
 * If this is an arbitrary/custom trigger, you should come up with a unique
 * trigger name to avoid conflicts with triggers from other plugins.
 */
public class TriggerWebhookEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final String trigger;
    private final Event triggeringEvent;

    /**
     * Creates a new TriggerWebhookEvent.
     * <p>
     * Use this constructor, and then call the event using
     * {@link org.bukkit.plugin.PluginManager#callEvent(Event)}. All user webhooks
     * with a matching trigger will be executed.
     * <p>
     * The trigger is not case-sensitive, but it is recommended to use
     * SCREAMING_SNAKE_CASE.
     * <p>
     * The triggering event is the event that caused this webhook to be triggered.
     * It may be null if you are not executing webhooks in response to a Bukkit
     * event.
     * For example, if you are triggering a webhook when a player joins, you would
     * pass the PlayerJoinEvent as the triggering event.
     * This will make relevant variables available to the webhook. For example, any
     * event that extends {@link org.bukkit.event.player.PlayerEvent} will have
     * player variables available.
     * <p>
     * Custom events are supported, as long as they extend
     * {@link org.bukkit.event.Event}.
     * If you would like to have custom variables available for your custom event,
     * you will need to listen for {@link WebhookVariableEvent}, and check if the
     * triggering event is an instance of your custom event, matching the one used
     * here. Then, you can set the replacement value for your custom variable.
     * 
     * @param trigger         the category of webhooks to trigger
     * @param triggeringEvent the event that triggered this webhook, may be null
     */
    public TriggerWebhookEvent(String trigger, Event triggeringEvent) {
        this.trigger = trigger;
        this.triggeringEvent = triggeringEvent;
    }

    String getTrigger() {
        return trigger;
    }

    Event getTriggeringEvent() {
        return triggeringEvent;
    }

}