package com.kylenanakdewa.simplewebhooks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import com.kylenanakdewa.simplewebhooks.Webhook.RequestType;

/**
 * Main plugin class for SimpleWebhooks.
 */
public final class WebhooksPlugin extends JavaPlugin {

	static WebhooksPlugin plugin;

	/** All registered join webhooks. */
	static Map<String,Webhook> joinWebhooks;
	/** All registered quit webhooks. */
	static Map<String,Webhook> quitWebhooks;
	/** All registered chat webhooks. */
	static Map<String,Webhook> chatWebhooks;
	/** All registered other webhooks. */
	static Map<String,Webhook> otherWebhooks;


	@Override
	public void onEnable(){
		plugin = this;

		// Main command
		getCommand("webhooks").setExecutor(new WebhooksCommands());

		// Register event listener
		getServer().getPluginManager().registerEvents(new PlayerWebhookListener(), this);

		// Load config
		saveDefaultConfig();
		loadConfig();
	}


	/** Retrieve webhooks from config. */
	void loadConfig(){
		reloadConfig();
		joinWebhooks = getFromConfig(getConfig().getConfigurationSection("webhooks.join"));
		quitWebhooks = getFromConfig(getConfig().getConfigurationSection("webhooks.quit"));
		chatWebhooks = getFromConfig(getConfig().getConfigurationSection("webhooks.chat"));
		otherWebhooks = getFromConfig(getConfig().getConfigurationSection("webhooks.other"));
	}

	/** Converts webhooks from the config into a map. */
	private Map<String,Webhook> getFromConfig(ConfigurationSection config){
		Map<String,Webhook> webhooks = new HashMap<String,Webhook>();

		for(String webhookName : config.getKeys(false)){
			// URL
			URL url;
			String urlString = config.getString(webhookName+".url");
			if(urlString==null || urlString.isEmpty()){
				getLogger().severe("Missing URL for webhook "+webhookName);
				break;
			}
			try{
				url = new URL(urlString);
			} catch(MalformedURLException e){
				getLogger().severe("Invalid URL for webhook "+webhookName);
				getLogger().severe(e.getLocalizedMessage());
				break;
			}

			// Query params
			Map<String,String> queryParams = new HashMap<String,String>();
			for(String paramKey : config.getConfigurationSection(webhookName+".query").getKeys(false)){
				queryParams.put(paramKey, config.getString(webhookName+".query."+paramKey));
			}

			// Form params for POST
			Map<String,String> formParams = new HashMap<String,String>();
			for(String paramKey : config.getConfigurationSection(webhookName+".form").getKeys(false)){
				formParams.put(paramKey, config.getString(webhookName+".query."+paramKey));
			}
			RequestType requestType = formParams.isEmpty() ? RequestType.GET : RequestType.POST;

			webhooks.put(webhookName.toLowerCase(), new Webhook(url, queryParams, requestType, formParams));
		}

		return webhooks;
	}

}
