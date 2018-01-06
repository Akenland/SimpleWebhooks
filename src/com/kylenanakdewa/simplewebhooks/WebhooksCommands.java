package com.kylenanakdewa.simplewebhooks;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

/**
 * Commands for the SimpleWebhooks plugin.
 */
public class WebhooksCommands implements TabExecutor {

    @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Version command
		if(args.length==0 || args[0].equalsIgnoreCase("version")){
			sender.sendMessage("SimpleWebhooks "+WebhooksPlugin.plugin.getDescription().getVersion()+" by Kyle Nanakdewa");
            sender.sendMessage("- A simple plugin for sending data to webhooks.");
            sender.sendMessage("- Website: http://Akenland.com/plugins");
			return true;
        }

        // List command
        if(args.length==1 && args[0].equalsIgnoreCase("list")){
            sender.sendMessage("--- Join Webhooks ---");
            for(Map.Entry<String,Webhook> entry : WebhooksPlugin.joinWebhooks.entrySet()){
                sender.sendMessage("- "+entry.getKey()+" - "+entry.getValue().getURL());
            }

            sender.sendMessage("--- Quit Webhooks ---");
            for(Map.Entry<String,Webhook> entry : WebhooksPlugin.quitWebhooks.entrySet()){
                sender.sendMessage("- "+entry.getKey()+" - "+entry.getValue().getURL());
            }

            sender.sendMessage("--- Chat Webhooks ---");
            for(Map.Entry<String,Webhook> entry : WebhooksPlugin.chatWebhooks.entrySet()){
                sender.sendMessage("- "+entry.getKey()+" - "+entry.getValue().getURL());
            }

            sender.sendMessage("--- Other Webhooks ---");
            for(Map.Entry<String,Webhook> entry : WebhooksPlugin.otherWebhooks.entrySet()){
                sender.sendMessage("- "+entry.getKey()+" - "+entry.getValue().getURL());
            }

            return true;
        }

		// Reload command
		if(args.length==1 && args[0].equalsIgnoreCase("reload")){
			WebhooksPlugin.plugin.loadConfig();
            sender.sendMessage("Webhooks reloaded.");
            return true;
		}

		// Execute command
		if(args.length>=2 && args[0].equalsIgnoreCase("execute")){
            // Find the webhook
            String name = args[1].toLowerCase();
			Webhook webhook;
            webhook = WebhooksPlugin.otherWebhooks.get(name);
            // Check the other types, if not found
            if(webhook==null) webhook = WebhooksPlugin.joinWebhooks.get(name);
            if(webhook==null) webhook = WebhooksPlugin.quitWebhooks.get(name);
            if(webhook==null) webhook = WebhooksPlugin.chatWebhooks.get(name);
            // If still not found, return error
            if(webhook==null){
                sender.sendMessage("Webhook "+name+" was not found.");
                return false;
            }

            // Replace params
            if(args.length>2){
                List<String> paramArgs = new ArrayList<String>(Arrays.asList(args));
                paramArgs.remove(0); paramArgs.remove(0);
                for(String arg : paramArgs){
                    webhook.replaceParamVar("{COMMAND_PARAM_"+(paramArgs.indexOf(arg)+1)+"}", arg);
                }
            }

            // Execute the webhook
            webhook.execute();
            sender.sendMessage("Webhook "+name+" executed.");
            return true;
        }

        if(args.length>=2 && args[0].equalsIgnoreCase("executecustom")){
            try{
                URL url = new URL(args[1]);
            
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("Accept-Charset", java.nio.charset.StandardCharsets.UTF_8.name());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

                if(args.length==3){
                    connection.setDoOutput(true);

                    String output = args[2];

                    connection.getOutputStream().write(output.getBytes(java.nio.charset.StandardCharsets.UTF_8.name()));
                }                

                connection.connect();
                sender.sendMessage(("Webhook executed. "+((HttpURLConnection)connection).getResponseCode()+": "+((HttpURLConnection)connection).getResponseMessage()));

                return true;
        
            } catch(IOException e){
                sender.sendMessage("Invalid webhook.");
                return false;
            }
        }
        
        // Invalid command
        sender.sendMessage("Invalid arguments.");
        return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Main command - return each sub-command
        if(args.length<=1) return Arrays.asList("version", "list", "reload", "execute");
        // Execute command - return list of webhooks
        if(args.length>=1 && args[0].equalsIgnoreCase("execute") && args.length<=2) return new ArrayList<String>(WebhooksPlugin.otherWebhooks.keySet());
        // Otherwise return nothing
        return Arrays.asList("");
    }

}