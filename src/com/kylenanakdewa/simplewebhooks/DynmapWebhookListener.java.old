package com.kylenanakdewa.simplewebhooks;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.dynmap.DynmapWebChatEvent;

/**
 * Listener to fire webhooks when a chat message is sent from Dynmap.
 */
final class DynmapWebhookListener implements Listener {

    /** Replace variables for Dynmap event webhooks. */
    private void replaceParamVars(Webhook webhook, DynmapWebChatEvent event){
        webhook.replaceParamVar("{PLAYER_USERNAME}", event.getName());
        webhook.replaceParamVar("{PLAYER_DISPLAYNAME}", event.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(DynmapWebChatEvent event){
        // If event is cancelled, do nothing
        if(event.isCancelled()) return;

        // Get the registered chat webhooks
        for(Webhook webhook : WebhooksPlugin.chatWebhooks.values()){
            // Replace vars
            replaceParamVars(webhook, event);
            webhook.replaceParamVar("{EVENT_MESSAGE}", ChatColor.stripColor("<"+event.getName()+"> " + event.getMessage()));
            webhook.replaceParamVar("{CHAT_MESSAGE}", ChatColor.stripColor(event.getMessage()));
            // Execute the webhook
            webhook.execute();
        }
    }
}