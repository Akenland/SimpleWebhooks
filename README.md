# SimpleWebhooks
A Bukkit plugin for webhooks

> [!NOTE]
> The plugin has been completely rewritten in the 2.0 release and the information below has not yet been updated.

Download and instructions can be found [here](https://github.com/Akenland/SimpleWebhooks/releases). For help, please [create an issue](https://github.com/Akenland/SimpleWebhooks/issues/new) or send an email to kade@akenland.com

# About this plugin
This plugin will send webhooks to specified URLs when something happens on your server. You can use it to mirror chat messages to Discord servers, track player joins and quits, recieve notifications when something happens in-game, ping a web server (such as a dynamic DNS service), update a website, or integrate with anything that uses webhooks. 

The following triggers are available:
* Player join
* Player quit
* Player chat
* Dynmap chat
* Command (execute webhook on manual command, from server console, or from command block)

Webhooks can be simple GET requests, or you can include JSON data for POST requests. This data can include a variety of event-specific data, or in the case of commands, can be specified as arguments.

This plugin does not support receiving requests, only sending. Services that require authentication are not supported. 