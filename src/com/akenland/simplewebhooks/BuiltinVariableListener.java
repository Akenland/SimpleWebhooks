package com.akenland.simplewebhooks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.WorldEvent;

/**
 * Performs variable replacements for common use cases, such as player events,
 * server events, and server constants.
 * 
 * Will run with lowest priority, so other plugins may override these values.
 */
public final class BuiltinVariableListener implements Listener {

    private final WebhooksPlugin PLUGIN;

    BuiltinVariableListener(WebhooksPlugin plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVariableEvent(WebhookVariableEvent event) {

        // Server variables/constants
        switch (event.getVariable()) {
            case "PLAYER_COUNT":
                event.setReplacement(Bukkit.getOnlinePlayers().size() + "");
                return;
            case "MAX_PLAYER_COUNT":
                event.setReplacement(Bukkit.getMaxPlayers() + "");
                return;
            case "SERVER_VERSION":
                event.setReplacement(Bukkit.getBukkitVersion());
                return;
            case "SERVER_MOTD":
                event.setReplacement(Bukkit.getMotd());
                return;
            case "WORLD_NAME":
                event.setReplacement(Bukkit.getWorlds().get(0).getName());
                return;
            case "WEBHOOKS_PLUGIN_VERSION":
                event.setReplacement(PLUGIN.getDescription().getFullName());
                return;
        }

        // Block events
        if (event.getTriggeringEvent() instanceof BlockEvent) {
            var blockEvent = (BlockEvent) event.getTriggeringEvent();
            var block = blockEvent.getBlock();
            var value = getBlockVariable(event.getVariable(), block);
            if (value != null) {
                event.setReplacement(value);
            }
        }

        // Player events
        if (event.getTriggeringEvent() instanceof PlayerEvent
                || event.getTriggeringEvent() instanceof PlayerDeathEvent) {
            Player player = null;
            if (event.getTriggeringEvent() instanceof PlayerEvent) {
                player = ((PlayerEvent) event.getTriggeringEvent()).getPlayer();
            }

            // Player join event
            if (event.getTriggeringEvent() instanceof PlayerJoinEvent && event.getVariable().equals("EVENT_MESSAGE")) {
                var joinEvent = (PlayerJoinEvent) event.getTriggeringEvent();
                if (joinEvent.getJoinMessage() != null) {
                    event.setReplacement(ChatColor.stripColor(joinEvent.getJoinMessage()));
                }
                return;
            }

            // Player quit event
            if (event.getTriggeringEvent() instanceof PlayerQuitEvent && event.getVariable().equals("EVENT_MESSAGE")) {
                var quitEvent = (PlayerQuitEvent) event.getTriggeringEvent();
                if (quitEvent.getQuitMessage() != null) {
                    event.setReplacement(ChatColor.stripColor(quitEvent.getQuitMessage()));
                }
                return;
            }

            // Player chat event
            if (event.getTriggeringEvent() instanceof AsyncPlayerChatEvent) {
                var chatEvent = (AsyncPlayerChatEvent) event.getTriggeringEvent();
                switch (event.getVariable()) {
                    case "CHAT_MESSAGE":
                        event.setReplacement(ChatColor.stripColor(chatEvent.getMessage()));
                        return;
                    case "EVENT_MESSAGE":
                        event.setReplacement(ChatColor.stripColor(String.format(chatEvent.getFormat(),
                                chatEvent.getPlayer().getDisplayName(), chatEvent.getMessage())));
                        return;
                }
            }

            // Player advancement event
            if (event.getTriggeringEvent() instanceof PlayerAdvancementDoneEvent) {
                var advancementEvent = (PlayerAdvancementDoneEvent) event.getTriggeringEvent();
                var advancement = advancementEvent.getAdvancement();
                var advancementDisplay = advancement.getDisplay();

                if (event.getVariable().equals("ADVANCEMENT_KEY")) {
                    event.setReplacement(advancement.getKey().getKey());
                    return;
                }

                if (advancementDisplay != null) {
                    switch (event.getVariable()) {
                        case "ADVANCEMENT_NAME":
                            event.setReplacement(advancement.getDisplay().getTitle());
                            return;
                        case "ADVANCEMENT_DESCRIPTION":
                            event.setReplacement(advancement.getDisplay().getDescription());
                            return;
                        case "ADVANCEMENT_TYPE":
                            event.setReplacement(advancement.getDisplay().getType().toString());
                            return;
                        case "EVENT_MESSAGE":
                            String playerName = ChatColor.stripColor(advancementEvent.getPlayer().getDisplayName());
                            String message;
                            switch (advancement.getDisplay().getType()) {
                                case TASK:
                                    message = " has made the advancement ";
                                    break;
                                case GOAL:
                                    message = " has reached the goal ";
                                    break;
                                case CHALLENGE:
                                    message = " has completed the challenge ";
                                    break;
                                default:
                                    message = " has made the advancement ";
                                    break;
                            }
                            String advancementName = "[" + advancement.getDisplay().getTitle() + "]";
                            event.setReplacement(playerName + message + advancementName);
                            return;
                    }
                }
            }

            // Player command event
            if (event.getTriggeringEvent() instanceof PlayerCommandPreprocessEvent) {
                var commandEvent = (PlayerCommandPreprocessEvent) event.getTriggeringEvent();
                switch (event.getVariable()) {
                    case "COMMAND_LINE":
                        event.setReplacement(commandEvent.getMessage());
                        return;
                    case "COMMAND_SENDER_NAME":
                        event.setReplacement(commandEvent.getPlayer().getName());
                        return;
                }
            }

            // Player death event
            if (event.getTriggeringEvent() instanceof PlayerDeathEvent) {
                var deathEvent = (PlayerDeathEvent) event.getTriggeringEvent();
                player = deathEvent.getEntity();

                switch (event.getVariable()) {
                    case "EVENT_MESSAGE":
                        event.setReplacement(ChatColor.stripColor(deathEvent.getDeathMessage()));
                        return;
                    case "PLAYER_KEEPINVENTORY":
                        event.setReplacement(deathEvent.getKeepInventory() + "");
                        return;
                    case "PLAYER_DROPS_COUNT":
                        event.setReplacement(deathEvent.getDrops().size() + "");
                        return;
                    case "DAMAGE_TYPE":
                        var damageType = deathEvent.getDamageSource().getDamageType().getKeyOrNull();
                        event.setReplacement(damageType != null ? damageType.getKey() : "UNKNOWN");
                        return;
                }
                if (deathEvent.getDamageSource().getCausingEntity() != null) {
                    var entity = deathEvent.getDamageSource().getCausingEntity();
                    switch (event.getVariable()) {
                        case "DAMAGE_CAUSER_NAME":
                            event.setReplacement(entity.getName());
                            return;
                        case "DAMAGE_CAUSER_DISPLAYNAME":
                            if (entity instanceof Player) {
                                event.setReplacement(ChatColor.stripColor(((Player) entity).getDisplayName()));
                            } else if (entity.getCustomName() != null) {
                                event.setReplacement(ChatColor.stripColor(entity.getCustomName()));
                            } else {
                                event.setReplacement(entity.getName());
                            }
                            return;
                        case "DAMAGE_CAUSER_UUID":
                            event.setReplacement(entity.getUniqueId().toString());
                            return;
                        case "DAMAGE_CAUSER_TYPE":
                            var type = entity.getType().getKeyOrNull();
                            event.setReplacement(type != null ? type.getKey() : "UNKNOWN");
                            return;
                    }
                }
            }

            // This should never happen
            if (player == null) {
                PLUGIN.getLogger().severe(
                        "Player event triggered without a player, unable to replace variables. Please report this to the plugin author!");
                return;
            }

            // General player variables
            var value = getPlayerVariable(event.getVariable(), player);
            if (value != null) {
                event.setReplacement(value);
            }

            // Player gamemode event - override the variable as it will be the previous mode
            if (event.getTriggeringEvent() instanceof PlayerGameModeChangeEvent) {
                var gameModeEvent = (PlayerGameModeChangeEvent) event.getTriggeringEvent();
                if (event.getVariable().equals("PLAYER_GAMEMODE")) {
                    event.setReplacement(gameModeEvent.getNewGameMode().toString());
                }
            }
        }

        // Server events
        if (event.getTriggeringEvent() instanceof ServerCommandEvent) {
            var commandEvent = (ServerCommandEvent) event.getTriggeringEvent();
            switch (event.getVariable()) {
                case "COMMAND_LINE":
                    event.setReplacement(commandEvent.getCommand());
                    return;
                case "COMMAND_SENDER_NAME":
                    event.setReplacement(commandEvent.getSender().getName());
                    return;
            }

            // Additional variables for command blocks
            if (commandEvent.getSender() instanceof BlockCommandSender) {
                var blockSender = (BlockCommandSender) commandEvent.getSender();
                var block = blockSender.getBlock();

                // Block variables, for the command block itself
                var blockValue = getBlockVariable(event.getVariable(), block);
                if (blockValue != null) {
                    event.setReplacement(blockValue);
                }

                // World variables, for the world the command block is in
                var world = block.getWorld();
                var worldValue = getWorldVariable(event.getVariable(), world);
                if (worldValue != null) {
                    event.setReplacement(worldValue);
                }

                // Player variables, for the nearest player to the command block
                var playerValue = getPlayerVariableForNearest(event.getVariable(), blockSender);
                if (playerValue != null) {
                    event.setReplacement(playerValue);
                }
            }

            // Additional variables for command minecarts
            if (commandEvent.getSender() instanceof CommandMinecart) {
                var minecartSender = (CommandMinecart) commandEvent.getSender();

                // World variables, for the world the command minecart is in
                var world = minecartSender.getWorld();
                var worldValue = getWorldVariable(event.getVariable(), world);
                if (worldValue != null) {
                    event.setReplacement(worldValue);
                }

                // Player variables, for the nearest player to the command minecart
                var playerValue = getPlayerVariableForNearest(event.getVariable(), minecartSender);
                if (playerValue != null) {
                    event.setReplacement(playerValue);
                }
            }
        }

        // World events
        if (event.getTriggeringEvent() instanceof WorldEvent) {
            var worldEvent = (WorldEvent) event.getTriggeringEvent();
            var world = worldEvent.getWorld();
            var value = getWorldVariable(event.getVariable(), world);
            if (value != null) {
                event.setReplacement(value);
            }
        }

        // Webhook execute command event
        if (event.getTriggeringEvent() instanceof WebhookExecuteCommandEvent) {
            var executeEvent = (WebhookExecuteCommandEvent) event.getTriggeringEvent();
            switch (event.getVariable()) {
                case "COMMAND_PARAM_ALL":
                    event.setReplacement(executeEvent.getAllParams());
                    return;
                case "COMMAND_PARAM_COUNT":
                    event.setReplacement(executeEvent.getParamCount() + "");
                    return;
                case "COMMAND_SENDER_NAME":
                    event.setReplacement(executeEvent.getSender().getName());
                    return;
            }

            // Individual command parameters
            if (event.getVariable().startsWith("COMMAND_PARAM_")) {
                try {
                    int index = Integer.parseInt(event.getVariable().substring("COMMAND_PARAM_".length())) - 1;
                    if (index >= 0 && index < executeEvent.getParamCount()) {
                        event.setReplacement(executeEvent.getParam(index));
                    }
                } catch (NumberFormatException e) {
                    // Ignore, this is not a valid number
                    PLUGIN.getLogger().warning("Invalid command parameter variable: " + event.getVariable());
                }
            }

            // Additional variables for command blocks
            if (executeEvent.getSender() instanceof BlockCommandSender) {
                var blockSender = (BlockCommandSender) executeEvent.getSender();
                var block = blockSender.getBlock();

                // Block variables, for the command block itself
                var blockValue = getBlockVariable(event.getVariable(), block);
                if (blockValue != null) {
                    event.setReplacement(blockValue);
                }

                // World variables, for the world the command block is in
                var world = block.getWorld();
                var worldValue = getWorldVariable(event.getVariable(), world);
                if (worldValue != null) {
                    event.setReplacement(worldValue);
                }

                // Player variables, for the nearest player to the command block
                var playerValue = getPlayerVariableForNearest(event.getVariable(), blockSender);
                if (playerValue != null) {
                    event.setReplacement(playerValue);
                }
            }

            // Additional variables for command minecarts
            if (executeEvent.getSender() instanceof CommandMinecart) {
                var minecartSender = (CommandMinecart) executeEvent.getSender();

                // World variables, for the world the command minecart is in
                var world = minecartSender.getWorld();
                var worldValue = getWorldVariable(event.getVariable(), world);
                if (worldValue != null) {
                    event.setReplacement(worldValue);
                }

                // Player variables, for the nearest player to the command minecart
                var playerValue = getPlayerVariableForNearest(event.getVariable(), minecartSender);
                if (playerValue != null) {
                    event.setReplacement(playerValue);
                }
            }
        }

    }

    /**
     * Given a variable relating to a block, get the value of that variable, for the
     * specified block.
     * <p>
     * Other plugins may use this method to resolve variables relating to blocks,
     * such as in triggering events that do not extend
     * {@link org.bukkit.event.block.BlockEvent} (such events already utilize this
     * method to auto-generate variables).
     * Example usage:
     * 
     * <pre>
     * MyCustomEvent triggeringEvent = event.getTriggeringEvent();
     * Block block = triggeringEvent.getBlock();
     * String value = BuiltinVariableListener.getBlockVariable(event.getVariable(), block);
     * if (value != null) {
     *     event.setReplacement(value);
     * }
     * </pre>
     * 
     * @param variable The variable to get the value of.
     * @param block    The block to get the value for.
     * @return The value of the variable, or null if the variable was not recognized
     *         or is not relating to a block.
     */
    public static String getBlockVariable(String variable, Block block) {
        switch (variable) {
            case "BLOCK_LOC_WORLD":
                return block.getWorld().getName();
            case "BLOCK_LOC_X":
                return block.getX() + "";
            case "BLOCK_LOC_Y":
                return block.getY() + "";
            case "BLOCK_LOC_Z":
                return block.getZ() + "";
            case "BLOCK_TYPE":
                var type = block.getType().getKeyOrNull();
                return type != null ? type.getKey() : "UNKNOWN";
        }

        return null;
    }

    /**
     * Given a variable relating to a player, get the value of that variable, for
     * the specified player.
     * <p>
     * Other plugins may use this method to resolve variables relating to players,
     * such as in triggering events that do not extend
     * {@link org.bukkit.event.player.PlayerEvent} (such events already utilize this
     * method to auto-generate variables).
     * Example usage:
     * 
     * <pre>
     * MyCustomEvent triggeringEvent = event.getTriggeringEvent();
     * Player player = triggeringEvent.getPlayer();
     * String value = BuiltinVariableListener.getPlayerVariable(event.getVariable(), player);
     * if (value != null) {
     *     event.setReplacement(value);
     * }
     * </pre>
     * 
     * @param variable The variable to get the value of.
     * @param player   The player to get the value for.
     * @return The value of the variable, or null if the variable was not recognized
     *         or is not relating to a player.
     */
    public static String getPlayerVariable(String variable, Player player) {
        switch (variable) {
            case "PLAYER_USERNAME":
                return player.getName();
            case "PLAYER_DISPLAYNAME":
                return ChatColor.stripColor(player.getDisplayName());
            case "PLAYER_ISNEW":
                return player.hasPlayedBefore() + "";
            case "PLAYER_UUID":
                return player.getUniqueId().toString();
            case "PLAYER_IP":
                return player.getAddress().getAddress().getHostAddress();
            case "PLAYER_ENTITYID":
                return player.getEntityId() + "";
            case "PLAYER_LOC_WORLD":
                return player.getWorld().getName();
            case "PLAYER_LOC_X":
                return player.getLocation().getX() + "";
            case "PLAYER_LOC_Y":
                return player.getLocation().getY() + "";
            case "PLAYER_LOC_Z":
                return player.getLocation().getZ() + "";
            case "PLAYER_HEALTH":
                return player.getHealth() + "";
            case "PLAYER_LOCALE":
                return player.getLocale();
            case "PLAYER_GAMEMODE":
                return player.getGameMode().toString();
            case "PLAYER_PING":
                return player.getPing() + "";
            case "PLAYER_ISOP":
                return player.isOp() + "";
        }

        if (variable.startsWith("PLAYER_PERMISSION_")) {
            var permission = variable.substring("PLAYER_PERMISSION_".length());
            return player.hasPermission(permission) + "";
        }

        return null;
    }

    /**
     * Given a variable relating to a player, get the value of that variable, for
     * the nearest player to the specified command sender.
     * There is no distance limit. This works identically to the "@p" target
     * selector.
     * If no player is found, this will return null.
     * <p>
     * See {@link #getPlayerVariable(String, Player)} for more information. This
     * method is simply a convenience for when you do not have a player object, but
     * have a command sender object.
     * 
     * @param variable The variable to get the value of.
     * @param sender   The command sender to get the nearest player for.
     * @return The value of the variable, or null if the variable was not
     *         recognized, is not relating to a player, or no player was found.
     */
    public static String getPlayerVariableForNearest(String variable, CommandSender sender) {
        var entities = Bukkit.selectEntities(sender, "@p");
        if (!entities.isEmpty() && entities.getFirst() instanceof Player) {
            var player = (Player) entities.getFirst();
            return getPlayerVariable(variable, player);
        }

        return null;
    }

    /**
     * Given a variable relating to a world, get the value of that variable, for the
     * specified world.
     * <p>
     * Other plugins may use this method to resolve variables relating to worlds,
     * such as in triggering events that do not extend
     * {@link org.bukkit.event.world.WorldEvent} (such events already utilize this
     * method to auto-generate variables).
     * Example usage:
     * 
     * <pre>
     * MyCustomEvent triggeringEvent = event.getTriggeringEvent();
     * World world = triggeringEvent.getWorld();
     * String value = BuiltinVariableListener.getWorldVariable(event.getVariable(), world);
     * if (value != null) {
     *     event.setReplacement(value);
     * }
     * </pre>
     * 
     * @param variable The variable to get the value of.
     * @param world    The world to get the value for.
     * @return The value of the variable, or null if the variable was not recognized
     *         or is not relating to a world.
     */
    public static String getWorldVariable(String variable, World world) {
        switch (variable) {
            case "WORLD_NAME":
                return world.getName();
            case "WORLD_ENVIRONMENT":
                return world.getEnvironment().toString();
            case "WORLD_SEED":
                return world.getSeed() + "";
            case "WORLD_CHUNK_COUNT":
                return world.getLoadedChunks().length + "";
            case "WORLD_PLAYER_COUNT":
                return world.getPlayers().size() + "";
            case "WORLD_ENTITY_COUNT":
                return world.getEntities().size() + "";
            case "WORLD_DIFFICULTY":
                return world.getDifficulty().toString();
            case "WORLD_GENERATOR":
                var generator = world.getGenerator();
                return generator != null ? generator.getClass().getName() : "DEFAULT";
            case "WORLD_BIOMEPROVIDER":
                var biomeProvider = world.getBiomeProvider();
                return biomeProvider != null ? biomeProvider.getClass().getName() : "DEFAULT";
            case "WORLD_FEATUREFLAGS":
                var flags = world.getFeatureFlags();
                if (flags.isEmpty()) {
                    return "NONE";
                } else {
                    String flagString = "";
                    for (var flag : flags) {
                        flagString += flag.getKey().toString() + ", ";
                    }
                    return flagString.substring(0, flagString.length() - 2);
                }
            case "WORLD_TIME":
                return world.getTime() + "";
            case "WORLD_WEATHER":
                return world.isThundering() ? "THUNDER" : world.hasStorm() ? "RAIN" : "CLEAR";
            case "WORLD_WEATHER_DURATION":
                return world.getWeatherDuration() + "";
            case "WORLD_SPAWN_LOC_X":
                return world.getSpawnLocation().getX() + "";
            case "WORLD_SPAWN_LOC_Y":
                return world.getSpawnLocation().getY() + "";
            case "WORLD_SPAWN_LOC_Z":
                return world.getSpawnLocation().getZ() + "";
        }
        return null;
    }

}