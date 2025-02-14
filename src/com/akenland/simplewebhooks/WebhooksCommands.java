package com.akenland.simplewebhooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.annotation.command.Commands;

/**
 * Commands for the SimpleWebhooks plugin.
 */
@Commands(@org.bukkit.plugin.java.annotation.command.Command(name = "webhooks", desc = "Manage and execute webhooks.", usage = "/webhooks [version|list|reload|execute|trigger] [webhook] [params...]", permission = "webhooks.admin"))
final class WebhooksCommands implements TabExecutor {

    private final WebhooksPlugin PLUGIN;

    private final ChatColor COLORB = ChatColor.AQUA;
    private final ChatColor COLORC = ChatColor.RED;
    private final ChatColor COLOR7 = ChatColor.GRAY;
    private final ChatColor COLOR8 = ChatColor.DARK_GRAY;
    private final ChatColor COLORR = ChatColor.RESET;

    WebhooksCommands(WebhooksPlugin plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Version command
        if (args.length == 0 || args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(COLORB + PLUGIN.getName() + " " + PLUGIN.getDescription().getVersion() + COLORR + " by "
                    + PLUGIN.getDescription().getAuthors().get(0));
            sender.sendMessage(COLOR8 + "- " + COLOR7 + PLUGIN.getDescription().getDescription());
            sender.sendMessage(COLOR8 + "- Website: " + COLOR7 + PLUGIN.getDescription().getWebsite());
            return true;
        }

        // List command
        if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
            String header;
            Map<String, Webhook> webhooks;

            // Second arg, if present, filters webhooks by trigger
            if (args.length == 2) {
                String trigger = args[1].toUpperCase();
                header = "Webhooks for trigger " + trigger;
                webhooks = PLUGIN.getWebhooksByTrigger(trigger);
            } else {
                header = "All webhooks";
                webhooks = PLUGIN.getWebhooks();
            }

            String count = COLOR8 + " (" + COLOR7 + webhooks.size() + COLOR8 + ")";

            sender.sendMessage(COLOR8 + "--- " + COLORB + header + count + " ---");
            for (var entry : webhooks.entrySet()) {
                sender.sendMessage(COLOR8 + "- " + COLORB + entry.getKey());
                sender.sendMessage(COLOR8 + " - URL: " + COLOR7 + entry.getValue().getURI());
            }

            return true;
        }

        // Reload command
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            PLUGIN.reload();
            sender.sendMessage(COLOR7 + "Webhooks reloaded.");
            return true;
        }

        // Execute command
        if (args.length >= 2 && args[0].equalsIgnoreCase("execute")) {
            String name = args[1].toLowerCase();

            // Prepare event data
            List<String> params = new ArrayList<String>();
            if (args.length > 2) {
                params.addAll(Arrays.asList(args));
                // Remove the first two args (execute subcommand, and webhook name)
                params.remove(0);
                params.remove(0);
            }
            var triggeringEvent = new WebhookExecuteCommandEvent(sender, params);

            // Execute the webhook
            var success = PLUGIN.executeWebhook(name, triggeringEvent);

            // Show message depending on success or failure
            if (success) {
                sender.sendMessage(COLORB + "Webhook " + name + " executed.");
            } else {
                sender.sendMessage(COLORC
                        + "An error occurred while executing the webhook. Make sure you are using the correct name, or check the server log for more information.");
            }
            return success;
        }

        // Trigger command
        if (args.length >= 2 && args[0].equalsIgnoreCase("trigger")) {
            String trigger = args[1].toUpperCase();

            // Prepare event data
            List<String> params = new ArrayList<String>();
            if (args.length > 2) {
                params.addAll(Arrays.asList(args));
                // Remove the first two args (trigger subcommand, and trigger name)
                params.remove(0);
                params.remove(0);
            }
            var triggeringEvent = new WebhookExecuteCommandEvent(sender, params);

            // Execute all webhooks with this trigger
            int successCount = PLUGIN.executeWebhooksByTrigger(trigger, triggeringEvent);

            // Show message depending on success or failure
            if (successCount > 0) {
                sender.sendMessage(
                        COLORB + "" + successCount + " webhooks sucessfully executed for trigger " + trigger + ".");
                return true;
            } else {
                sender.sendMessage(COLORC
                        + "No webhooks executed successfully. Make sure you are using the correct trigger, or check the server log for more information.");
                return false;
            }
        }

        // Invalid command
        sender.sendMessage(COLORC + "Invalid arguments.");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Main command - return each sub-command
        if (args.length == 1) {
            return Arrays.asList("version", "list", "reload", "execute", "trigger");
        }

        // Execute command - return list of webhooks
        if (args.length == 2 && args[0].equalsIgnoreCase("execute")) {
            return new ArrayList<String>(PLUGIN.getWebhooks().keySet());
        }

        // Trigger command - return list of triggers
        if (args.length == 2 && args[0].equalsIgnoreCase("trigger")) {
            return new ArrayList<String>(PLUGIN.getTriggers());
        }

        // Otherwise return nothing
        return Arrays.asList("");
    }

}