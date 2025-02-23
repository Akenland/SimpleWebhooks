package com.akenland.simplewebhooks;

import java.io.File;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import com.akenland.simplewebhooks.Webhook.RequestMethod;

/**
 * Main plugin class for SimpleWebhooks.
 */
@Plugin(name = "SimpleWebhooks", version = "2.0")
@Description(value = "Send webhooks to any URL when events happen on your server!")
@Author(value = "Kade")
@Website(value = "https://plugins.akenland.com/simplewebhooks")
@ApiVersion(value = ApiVersion.Target.v1_20)
public final class WebhooksPlugin extends JavaPlugin {

	/** The HTTP client to use for requests. */
	private HttpClient httpClient;

	/** The default user agent to use for requests. */
	private String userAgent;

	/** The email address to use in "From" header in requests. */
	private String serverContactEmail;

	/**
	 * The timeout to use for requests, in seconds. If nonpositive, requests will
	 * never timeout.
	 */
	private int timeout;

	/** All registered webhooks. */
	private Map<String, Webhook> webhooks;

	/** Triggers and their associated webhooks. */
	private Map<String, Set<Webhook>> triggers;

	@Override
	public void onEnable() {
		// Main command
		getCommand("webhooks").setExecutor(new WebhooksCommands(this));

		// Run all reload tasks
		reload();
	}

	@Override
	public void onDisable() {
		// Unregister event listeners
		HandlerList.unregisterAll(this);
	}

	/**
	 * Reloads the plugin.
	 */
	void reload() {
		// Disable/cleanup
		onDisable();

		// Load config
		saveDefaultConfig();
		loadConfig();

		// Create HTTP client
		var builder = HttpClient.newBuilder();
		if (getConfig().isString("http-client.follow-redirects")) {
			var policy = HttpClient.Redirect
					.valueOf(getConfig().getString("http-client.follow-redirects").toUpperCase());
			builder.followRedirects(policy);
		}
		this.httpClient = builder.build();

		// Register event listeners
		getServer().getPluginManager().registerEvents(new BuiltinVariableListener(this), this);
		getServer().getPluginManager().registerEvents(new BuiltinTriggerListener(this), this);
	}

	/** Load config options. */
	private void loadConfig() {
		reloadConfig();

		// User agent
		if (getConfig().isString("http-client.user-agent-override")) {
			var override = getConfig().getString("http-client.user-agent-override");
			userAgent = override.equals("NONE") ? null : override;

			// Warn if no user agent
			if (userAgent == null) {
				getLogger().warning("User agent disabled in " + getDataFolder().getPath() + File.pathSeparator
						+ "config.yml under http-client > user-agent-override. It is highly recommended to set a user agent, as receiving servers may block your webhooks in the event that you cannot be contacted.");
			}
		} else {
			var serverVersion = getServer().getName() + "/" + getServer().getBukkitVersion();
			var pluginVersion = getName() + "/" + getDescription().getVersion() + " +"
					+ getDescription().getWebsite();

			userAgent = serverVersion + " (" + pluginVersion + ")";

			var serverName = getConfig().getString("http-client.server-name");
			serverContactEmail = getConfig().getString("http-client.server-contact-email");

			// Warn if no contact info
			if (serverName == null) {
				getLogger().warning("Please set your server name in plugins/" + getDataFolder().getPath()
						+ File.pathSeparator
						+ "config.yml under http-client > server-name. This is highly recommended, as receiving servers may block your webhooks in the event that you cannot be contacted.");
			} else {
				if (serverContactEmail == null) {
					getLogger().warning("Please set your email address in plugins/" + getDataFolder().getPath()
							+ File.pathSeparator
							+ "config.yml under http-client > server-contact-email. This is highly recommended, as receiving servers may block your webhooks in the event that you cannot be contacted.");
				} else {
					userAgent += " " + serverName + " (" + serverContactEmail + ")";
				}
			}
		}

		timeout = getConfig().getInt("http-client.timeout");

		loadWebhooksFromConfig(getConfig().getConfigurationSection("webhooks"));
	}

	/** Load webhooks from the config. */
	private void loadWebhooksFromConfig(ConfigurationSection config) {
		webhooks = new HashMap<String, Webhook>();
		triggers = new HashMap<String, Set<Webhook>>();
		triggers.put("WEBHOOKS_INTERNAL_NONE", new HashSet<Webhook>());

		for (var webhookName : config.getKeys(false)) {
			// HTTP method
			var defaultMethod = config.isString(webhookName + ".body") ? "POST" : "GET";
			var method = config.getString(webhookName + ".method", defaultMethod).toUpperCase();
			RequestMethod requestMethod;
			try {
				requestMethod = RequestMethod.valueOf(method);
			} catch (IllegalArgumentException e) {
				getLogger().severe("Invalid method for webhook " + webhookName);
				getLogger().severe(e.getLocalizedMessage());
				break;
			}

			// URI
			var uri = config.getString(webhookName + ".url");
			if (uri == null || uri.isEmpty()) {
				getLogger().severe("Missing URL for webhook " + webhookName);
				break;
			}

			// Headers
			Map<String, String> headers = new HashMap<String, String>();
			if (config.contains(webhookName + ".headers")) {
				for (var header : config.getConfigurationSection(webhookName + ".headers").getKeys(false)) {
					headers.put(header, config.getString(webhookName + ".headers." + header));
				}
			}

			// Body
			var body = config.getString(webhookName + ".body");

			// Create webhook
			var webhook = new Webhook(requestMethod, uri, headers, body);
			webhooks.put(webhookName.toLowerCase(), webhook);

			// Triggers
			var triggerList = config.getStringList(webhookName + ".triggers");
			if (triggerList.isEmpty()) {
				triggers.get("WEBHOOKS_INTERNAL_NONE").add(webhook);
			} else {
				for (var trigger : config.getStringList(webhookName + ".triggers")) {
					trigger = trigger.toUpperCase();
					if (!triggers.containsKey(trigger)) {
						triggers.put(trigger, new HashSet<Webhook>());
					}
					triggers.get(trigger).add(webhook);
				}
			}
		}
	}

	/**
	 * Gets all webhooks.
	 * 
	 * @return A map of webhooks, with their names as keys.
	 */
	Map<String, Webhook> getWebhooks() {
		return webhooks;
	}

	/**
	 * Gets all webhooks that are triggered by a specific trigger.
	 * <p>
	 * If trigger is null, returns all webhooks have no triggers.
	 * 
	 * @param trigger The trigger to get webhooks for.
	 * @return A map of webhooks, with their names as keys.
	 */
	Map<String, Webhook> getWebhooksByTrigger(String trigger) {
		if (trigger == null) {
			trigger = "WEBHOOKS_INTERNAL_NONE";
		}
		var webhooks = triggers.getOrDefault(trigger.toUpperCase(), new HashSet<Webhook>());
		Map<String, Webhook> webhookMap = new HashMap<String, Webhook>();

		// Iterate over all webhooks and add the ones that are in the set
		for (var entry : getWebhooks().entrySet()) {
			if (webhooks.contains(entry.getValue())) {
				webhookMap.put(entry.getKey(), entry.getValue());
			}
		}

		return webhookMap;
	}

	/**
	 * Gets all triggers that have at least one webhook.
	 * 
	 * @return A set of all triggers.
	 */
	Set<String> getTriggers() {
		return triggers.keySet();
	}

	/**
	 * Executes a webhook.
	 * 
	 * @param webhook         The webhook to execute.
	 * @param triggeringEvent The event that triggered the webhook.
	 * @return True if the webhook was executed successfully, false otherwise.
	 */
	private boolean executeWebhook(Webhook webhook, Event triggeringEvent) {
		// Get the request
		HttpRequest request;
		try {
			request = webhook.getHttpRequest(triggeringEvent);
		} catch (URISyntaxException e) {
			getLogger().severe("Could not execute webhook, the URL was invalid. " + e.getMessage());
			return false;
		}

		// Add user agent and timeout
		var builder = HttpRequest.newBuilder(request, (n, v) -> true);
		builder.setHeader("User-Agent", userAgent);
		builder.setHeader("From", serverContactEmail);
		if (timeout > 0) {
			builder.timeout(Duration.ofSeconds(timeout));
		}
		request = builder.build();

		// Send the request
		httpClient.sendAsync(request, BodyHandlers.discarding());
		return true;
	}

	/**
	 * Executes a webhook.
	 * 
	 * @param name            The name of the webhook to execute.
	 * @param triggeringEvent The event that triggered the webhook.
	 * @return True if the webhook was executed successfully, false otherwise.
	 */
	boolean executeWebhook(String name, Event triggeringEvent) {
		name = name.toLowerCase();

		var webhook = webhooks.get(name);

		if (webhook == null) {
			getLogger().warning("Could not execute webhook " + name + ", the webhook was not found.");
			return false;
		} else {
			return executeWebhook(webhook, triggeringEvent);
		}
	}

	/**
	 * Executes all webhooks for a trigger.
	 * 
	 * @param trigger         The trigger
	 * @param triggeringEvent The event that triggered the webhooks.
	 * @return The number of webhooks that executed successfully.
	 */
	int executeWebhooksByTrigger(String trigger, Event triggeringEvent) {
		int successCount = 0;

		var webhooks = triggers.getOrDefault(trigger.toUpperCase(), new HashSet<Webhook>());

		for (var webhook : webhooks) {
			if (executeWebhook(webhook, triggeringEvent)) {
				successCount++;
			}
		}

		return successCount;
	}

}