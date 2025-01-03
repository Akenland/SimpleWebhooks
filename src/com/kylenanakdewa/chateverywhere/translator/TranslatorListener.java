package com.kylenanakdewa.chateverywhere.translator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * Listens for chat messages, and translates them if needed.
 */
public class TranslatorListener implements Listener, TabExecutor {

    private TranslatorWebhook webhook;

    static final Map<UUID, String> playerLanguages = new HashMap<UUID, String>();

    private final Plugin plugin;

    public TranslatorListener(String apiKey, Plugin plugin) {
        try {
            webhook = new TranslatorWebhook(apiKey);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.plugin = plugin;
    }


    public String translate(String message, String targetLanguageCode) {
        if(message.isEmpty()) return null;
        webhook.replaceParamVar("{CHAT_MESSAGE}", ChatColor.stripColor(message));
        webhook.replaceParamVar("{TRANSLATOR_LANGUAGE_CODE}", targetLanguageCode);

        // Execute the webhook
        InputStream inputStream = webhook.execute();

        // Read response
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String response = "";
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                response += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Parse response
        if (response.contains("lang=\"en-en\""))
            return null;
        int startingIndex = response.indexOf("<text>");
        if (startingIndex < 0)
            return null;
        startingIndex += 6;
        int endingIndex = response.indexOf("</text>");

        String translation = response.substring(startingIndex, endingIndex);

        if(translation.equalsIgnoreCase(message)) return null;

        return translation;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        // If event is cancelled, do nothing
        if (event.isCancelled()) return;

        String message = ChatColor.stripColor(event.getMessage());
        // If message starts with / or ./, it's probably a command, ignore it
        if(message.startsWith("/") || message.startsWith("./")) return;

        // Get languages to translate into
        Set<String> targetLanguages = new HashSet<String>(playerLanguages.values());

        for (String languageCode : targetLanguages) {
            String translation = translate(message, languageCode);
            if(translation!=null){
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Delay message, so it shows after the chat message
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.sendMessage(ChatColor.DARK_GRAY + "[Translation] " + ChatColor.RESET + translation));
                }
            }
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String language = new TranslatorPlayerData(event.getPlayer(), plugin).getLanguageCode();
        playerLanguages.put(event.getPlayer().getUniqueId(), language);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerLanguages.remove(event.getPlayer().getUniqueId());
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length>1 && args[0].equalsIgnoreCase("-set")){
            String languageCode = args[1];

            Player targetPlayer;
            if(args.length==3){
                targetPlayer = Utils.getPlayer(args[2]);
                if(targetPlayer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
            } else targetPlayer = (Player) sender;

            new TranslatorPlayerData(targetPlayer, plugin).setLanguageCode(languageCode);
            playerLanguages.put(targetPlayer.getUniqueId(), languageCode);

            Utils.sendActionBar(sender, "Messages will be translated to "+languageCode.toUpperCase());
            Utils.sendActionBar(targetPlayer, "Messages will be translated to "+languageCode.toUpperCase());
            return true;
        }

        String message = String.join(" ", args);
        String translation = translate(message, playerLanguages.get(((Player)sender).getUniqueId()));
        if(translation==null){
            Utils.sendActionBar(sender, ChatColor.RED+"Unable to translate.");
            return false;
        }
        sender.sendMessage(ChatColor.DARK_GRAY + "[Translation] " + ChatColor.RESET + translation);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

}