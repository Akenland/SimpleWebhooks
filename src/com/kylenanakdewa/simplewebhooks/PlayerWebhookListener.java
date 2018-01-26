package com.kylenanakdewa.simplewebhooks;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listeners to fire webhooks when a player event happens.
 */
final class PlayerWebhookListener implements Listener {

    /** Replace variables for player event webhooks. */
    private void replaceParamVars(Webhook webhook, PlayerEvent event){
        webhook.replaceParamVar("{PLAYER_USERNAME}", event.getPlayer().getName());
        webhook.replaceParamVar("{PLAYER_DISPLAYNAME}", ChatColor.stripColor(event.getPlayer().getDisplayName()));
        webhook.replaceParamVar("{PLAYER_ISNEW}", event.getPlayer().hasPlayedBefore()+"");
        webhook.replaceParamVar("{PLAYER_UUID}", event.getPlayer().getUniqueId().toString());
        webhook.replaceParamVar("{PLAYER_IP}", event.getPlayer().getAddress().getAddress().getHostAddress());
        webhook.replaceParamVar("{PLAYER_ENTITYID}", event.getPlayer().getEntityId()+"");
        webhook.replaceParamVar("{PLAYER_LOC_WORLD}", event.getPlayer().getWorld().getName());
        webhook.replaceParamVar("{PLAYER_LOC_X}", event.getPlayer().getLocation().getX()+"");
        webhook.replaceParamVar("{PLAYER_LOC_Y}", event.getPlayer().getLocation().getY()+"");
        webhook.replaceParamVar("{PLAYER_LOC_Z}", event.getPlayer().getLocation().getZ()+"");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event){
        // Get the registered join webhooks
        for(Webhook webhook : WebhooksPlugin.joinWebhooks.values()){
            // Replace vars
            replaceParamVars(webhook, event);
            webhook.replaceParamVar("{EVENT_MESSAGE}", ChatColor.stripColor(event.getJoinMessage()!=null ? event.getJoinMessage() : ""));
            // Execute the webhook
            webhook.execute();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event){
        // Get the registered join webhooks
        for(Webhook webhook : WebhooksPlugin.quitWebhooks.values()){
            // Replace vars
            replaceParamVars(webhook, event);
            webhook.replaceParamVar("{EVENT_MESSAGE}", ChatColor.stripColor(event.getQuitMessage()!=null ? event.getQuitMessage() : ""));
            // Execute the webhook
            webhook.execute();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event){
        // If event is cancelled, do nothing
        if(event.isCancelled()) return;

        // Get the registered join webhooks
        for(Webhook webhook : WebhooksPlugin.chatWebhooks.values()){
            // Replace vars
            replaceParamVars(webhook, event);
            webhook.replaceParamVar("{EVENT_MESSAGE}", ChatColor.stripColor(String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage())));
            webhook.replaceParamVar("{CHAT_MESSAGE}", ChatColor.stripColor(event.getMessage()));
            // Execute the webhook
            webhook.execute();
        }
    }
}