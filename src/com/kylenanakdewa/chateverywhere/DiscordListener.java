package com.kylenanakdewa.chateverywhere;

import java.awt.Color;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.dynmap.DynmapAPI;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * DiscordListener
 */
public final class DiscordListener {

    public void register(String token){
        ClientBuilder clientBuilder = new ClientBuilder();
        clientBuilder.withToken(token);
        IDiscordClient client = clientBuilder.login();
        client.getDispatcher().registerListener(this);
    }

    @EventSubscriber
    public void onChat(MessageReceivedEvent event){
        // Ignore if it's a webhook message
        if(event.getMessage().getWebhookLongID()==0){
            ChatColor realmColor = colorToChatColor(event.getAuthor().getColorForGuild(event.getGuild()));
            if(realmColor.equals(ChatColor.BLACK)) realmColor = ChatColor.GRAY;
            String sender = event.getAuthor().getDisplayName(event.getGuild());
            String message = event.getMessage().getFormattedContent();

            // Send it to the server
            Bukkit.broadcastMessage(realmColor+"<"+sender+"> "+ChatColor.RESET + message);

            // Send it to dynmap
            ((DynmapAPI)Bukkit.getPluginManager().getPlugin("dynmap")).sendBroadcastToWeb(null, "<"+sender+"> "+message);
        }
    }

    /**
     * Converts a {@link Color} to a Bukkit ChatColor.
     * The color must match the Minecraft colors exactly. If an exact match is not found, this method will return null.
     * @param color the color to convert
     * @return the matching ChatColor, or null if an exact match does not exist
     */
    private static ChatColor colorToChatColor(Color color){
        if(color.getRed()==0 && color.getGreen()==0 && color.getBlue()==0)
            return ChatColor.BLACK;
        if(color.getRed()==0 && color.getGreen()==0 && color.getBlue()==170)
            return ChatColor.DARK_BLUE;
        if(color.getRed()==0 && color.getGreen()==170 && color.getBlue()==0)
            return ChatColor.DARK_GREEN;
        if(color.getRed()==0 && color.getGreen()==170 && color.getBlue()==170)
            return ChatColor.DARK_AQUA;
        if(color.getRed()==170 && color.getGreen()==0 && color.getBlue()==0)
            return ChatColor.DARK_RED;
        if(color.getRed()==170 && color.getGreen()==0 && color.getBlue()==170)
            return ChatColor.DARK_PURPLE;
        if(color.getRed()==255 && color.getGreen()==170 && color.getBlue()==0)
            return ChatColor.GOLD;
        if(color.getRed()==170 && color.getGreen()==170 && color.getBlue()==170)
            return ChatColor.GRAY;
        if(color.getRed()==85 && color.getGreen()==85 && color.getBlue()==85)
            return ChatColor.DARK_GRAY;
        if(color.getRed()==85 && color.getGreen()==85 && color.getBlue()==255)
            return ChatColor.BLUE;

        if(color.getRed()==85 && color.getGreen()==255 && color.getBlue()==85)
            return ChatColor.GREEN;
        if(color.getRed()==85 && color.getGreen()==255 && color.getBlue()==255)
            return ChatColor.AQUA;
        if(color.getRed()==255 && color.getGreen()==85 && color.getBlue()==85)
            return ChatColor.RED;
        if(color.getRed()==255 && color.getGreen()==85 && color.getBlue()==255)
            return ChatColor.LIGHT_PURPLE;
        if(color.getRed()==255 && color.getGreen()==255 && color.getBlue()==85)
            return ChatColor.YELLOW;
        if(color.getRed()==255 && color.getGreen()==255 && color.getBlue()==255)
            return ChatColor.WHITE;

        // No match, return null
        return null;
    }
}