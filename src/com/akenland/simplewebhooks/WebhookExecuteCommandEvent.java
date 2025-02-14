package com.akenland.simplewebhooks;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is used as the triggering event when a webhook is executed via
 * commands.
 * 
 * It contains any arguments used in the command, the command sender, and if
 * executed by a command block, the nearest player.
 */
class WebhookExecuteCommandEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final CommandSender sender;
    private final List<String> params;

    WebhookExecuteCommandEvent(CommandSender sender, List<String> params) {
        this.sender = sender;
        this.params = params;
    }

    CommandSender getSender() {
        return sender;
    }

    List<String> getParams() {
        return params;
    }

    String getParam(int index) {
        return params.get(index);
    }

    int getParamCount() {
        return params.size();
    }

    String getAllParams() {
        return String.join(" ", params);
    }

}