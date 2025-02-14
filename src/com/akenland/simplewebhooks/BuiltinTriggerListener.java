package com.akenland.simplewebhooks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * Listens for various events and triggers webhooks.
 */
public final class BuiltinTriggerListener implements Listener {

    private final WebhooksPlugin PLUGIN;

    BuiltinTriggerListener(WebhooksPlugin plugin) {
        this.PLUGIN = plugin;
    }

    /**
     * Triggers webhooks when another plugin calls {@link TriggerWebhookEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginTrigger(TriggerWebhookEvent event) {
        PLUGIN.executeWebhooksByTrigger(event.getTrigger(), event.getTriggeringEvent());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerStart(ServerLoadEvent event) {
        PLUGIN.executeWebhooksByTrigger("SERVER_START", event);

        // TODO: hourly/daily triggers
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerCommand(ServerCommandEvent event) {
        PLUGIN.executeWebhooksByTrigger("SERVER_COMMAND", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        PLUGIN.executeWebhooksByTrigger("PLAYER_JOIN", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        PLUGIN.executeWebhooksByTrigger("PLAYER_QUIT", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PLUGIN.executeWebhooksByTrigger("PLAYER_CHAT", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        PLUGIN.executeWebhooksByTrigger("PLAYER_DEATH", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        PLUGIN.executeWebhooksByTrigger("PLAYER_ADVANCEMENT", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        PLUGIN.executeWebhooksByTrigger("PLAYER_COMMAND", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        PLUGIN.executeWebhooksByTrigger("PLAYER_CHANGEWORLD", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeGameMode(PlayerGameModeChangeEvent event) {
        PLUGIN.executeWebhooksByTrigger("PLAYER_CHANGEGAMEMODE", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        PLUGIN.executeWebhooksByTrigger("WORLD_LOAD", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        PLUGIN.executeWebhooksByTrigger("WORLD_UNLOAD", event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldSave(WorldSaveEvent event) {
        PLUGIN.executeWebhooksByTrigger("WORLD_SAVE", event);
    }

}