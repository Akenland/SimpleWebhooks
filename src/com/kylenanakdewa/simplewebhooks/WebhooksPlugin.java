package com.kylenanakdewa.simplewebhooks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import com.kylenanakdewa.chateverywhere.DiscordListener;
import com.kylenanakdewa.chateverywhere.translator.TranslatorListener;
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

	/** Discord token. */
	private static String discordToken;
	/** Translator API key. */
	private static String translatorApiKey;


	@Override
	public void onEnable(){
		plugin = this;

		// Main command
		getCommand("webhooks").setExecutor(new WebhooksCommands());

		// Register event listener
		getServer().getPluginManager().registerEvents(new PlayerWebhookListener(), this);

		// Register Dynmap listener
		if(getServer().getPluginManager().isPluginEnabled("dynmap")){
			//getServer().getPluginManager().registerEvents(new DynmapWebhookListener(), this);
		}

		// Load config
		saveDefaultConfig();
		loadConfig();

		// Discord sync
		if(discordToken!=null){
			new DiscordListener().register(discordToken);
			getLogger().info("Logged in to Discord!");
		}

		// Translator
		if(translatorApiKey!=null){
			TranslatorListener listener = new TranslatorListener(translatorApiKey,this);
			getServer().getPluginManager().registerEvents(listener, this);
			getCommand("translate").setExecutor(listener);
		}
	}


	/** Retrieve webhooks from config. */
	void loadConfig(){
		reloadConfig();
		joinWebhooks = getFromConfig(getConfig().getConfigurationSection("webhooks.join"));
		quitWebhooks = getFromConfig(getConfig().getConfigurationSection("webhooks.quit"));
		chatWebhooks = getFromConfig(getConfig().getConfigurationSection("webhooks.chat"));
		otherWebhooks = getFromConfig(getConfig().getConfigurationSection("webhooks.other"));
		discordToken = getConfig().getString("discord");
		translatorApiKey = getConfig().getString("translator");
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
			if(config.contains(webhookName+".query")){
				for(String paramKey : config.getConfigurationSection(webhookName+".query").getKeys(false))
					queryParams.put(paramKey, config.getString(webhookName+".query."+paramKey));
			}

			// JSON params for POST
			Map<String,String> jsonParams = new HashMap<String,String>();
			if(config.contains(webhookName+".json")){
				for(String paramKey : config.getConfigurationSection(webhookName+".json").getKeys(false))
					jsonParams.put(paramKey, config.getString(webhookName+".json."+paramKey));
			}
			RequestType requestType = jsonParams.isEmpty() ? RequestType.GET : RequestType.POST;

			webhooks.put(webhookName.toLowerCase(), new Webhook(url, queryParams, requestType, jsonParams));
		}

		return webhooks;
	}

}
