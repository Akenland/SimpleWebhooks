package com.kylenanakdewa.simplewebhooks;

import org.bukkit.plugin.java.JavaPlugin;

public final class WebhooksPlugin extends JavaPlugin {

	public static JavaPlugin plugin;

	@Override
	public void onEnable(){
		plugin = this;

		// Main command
		this.getCommand("webhooks").setExecutor(new WebhooksCommands());
	}
}
