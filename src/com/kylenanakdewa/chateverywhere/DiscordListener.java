package com.kylenanakdewa.chateverywhere;

import org.bukkit.Bukkit;
import org.dynmap.DynmapAPI;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * DiscordListener
 */
public final class DiscordListener {

    public void register(String token){
        new ClientBuilder().withToken(token).login().getDispatcher().registerListener(this);
    }

    @EventSubscriber
    public void onChat(MessageReceivedEvent event){
        Bukkit.getLogger().info("Discord message received.");
        // Ignore if it's a webhook message
        if(event.getMessage().getWebhookLongID()==0){
            String message = "<"+event.getAuthor().getDisplayName(event.getGuild())+">" + event.getMessage().getFormattedContent();

            // Send it to the server
            Bukkit.broadcastMessage(message);

            // Send it to dynmap
            ((DynmapAPI)Bukkit.getPluginManager().getPlugin("dynmap")).sendBroadcastToWeb(null, message);
        }
    }
}