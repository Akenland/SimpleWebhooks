# SimpleWebhooks
A Bukkit plugin for webhooks

![Image](https://kylenanakdewa.visualstudio.com/_apis/public/build/definitions/d9964615-0dc2-48a8-b08d-00fc606158c0/8/badge)

Documentation and download links will be posted soon. If you want it for your Spigot server sooner, please send an email to Kyle@Akenland.com

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
