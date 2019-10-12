package com.kylenanakdewa.simplewebhooks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.world.WorldEvent;

/**
 * Listeners to fire webhooks when a server event happens.
 */
final class ServerWebhookListener implements Listener {

    /** Replace variables for server event webhooks. */
    private void replaceParamVars(Webhook webhook, ServerEvent event){
        
    }
    /** Replace variables for world event webhooks. */
    private void replaceParamVars(Webhook webhook, WorldEvent event){
        webhook.replaceParamVar("{WORLD_NAME}", event.getWorld().getName());
        webhook.replaceParamVar("{WORLD_ENVIRONMENT}", event.getWorld().getEnvironment().toString());
        webhook.replaceParamVar("{WORLD_SEED}", event.getWorld().getSeed()+"");
        webhook.replaceParamVar("{WORLD_CHUNK_COUNT}", event.getWorld().getLoadedChunks().length+"");
        webhook.replaceParamVar("{WORLD_PLAYER_COUNT}", event.getWorld().getPlayers().size()+"");
        webhook.replaceParamVar("{WORLD_ENTITY_COUNT}", event.getWorld().getEntities().size()+"");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerStart(PluginEnableEvent event){
        // Get the registered join webhooks
        for(Webhook webhook : WebhooksPlugin.joinWebhooks.values()){
            // Replace vars
            replaceParamVars(webhook, event);
            //webhook.replaceParamVar("{EVENT_MESSAGE}", ChatColor.stripColor(event.getJoinMessage()!=null ? event.getJoinMessage() : ""));
            // Execute the webhook
            webhook.execute();
        }
    }

}