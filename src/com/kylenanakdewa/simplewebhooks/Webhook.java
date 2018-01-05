package com.kylenanakdewa.simplewebhooks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

/**
 * A Webhook that can be executed to send a payload to a webserver.
 */
public class Webhook {
    
    /** The URL of this webhook. */
    private final URL url;
    /** The charset to use. */
    private final String charset;
    /** The URL query parameters. */
    private final Map<String,String> queryParams;

    /** The type of HTTP request. */
    private RequestType requestType;
    /** The available types of HTTP requests. */
    enum RequestType {GET, POST;}

    /** Form content for POST requests. */
    private Map<String,String> formParams;


    /** Variables in params that should be replaced on execution. */
    private Map<String,String> paramVars;


    /** Creates a webhook. */
    Webhook(URL url, Map<String,String> queryParams, RequestType requestType, Map<String,String> formParams){
        this.url = url;
        this.charset = java.nio.charset.StandardCharsets.UTF_8.name();
        this.queryParams = queryParams;
        this.requestType = requestType;
        this.formParams = formParams;

        // Param variables
        paramVars = new HashMap<String,String>();
        replaceParamVars();
    }
    /** Creates a simple GET webhook. */
    Webhook(URL url, Map<String,String> queryParams){
        this(url, queryParams, RequestType.GET, null);
    }


    /** Executes the webhook, sending the data to the server. */
    void execute(){
        try{
            // Encode query params
            String encodedQuery = getEncodedParams(queryParams);

            // Open the connection
            URLConnection connection = new URL(url+encodedQuery).openConnection();
            connection.setRequestProperty("Accept-Charset", charset);

            // If POST, send output data
            if(requestType.equals(RequestType.POST)){
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset="+charset);

                // Encode form params
                String encodedOutput = getEncodedParams(formParams);

                connection.getOutputStream().write(encodedOutput.getBytes());
            }

            // Complete the HTTP request
            connection.connect();

        } catch(IOException e){
			WebhooksPlugin.plugin.getLogger().severe(e.getLocalizedMessage());
		}
    }

    /** Encodes parameters. Also replaces variables. */
    private String getEncodedParams(Map<String,String> params){
        // Convert variables
        for(Map.Entry<String,String> var : paramVars.entrySet())
            params.values().forEach(param -> param = param.replace(var.getKey(), var.getValue()));

        try{
            StringBuilder sb = new StringBuilder();
            for(Map.Entry<String,String> param : params.entrySet())
                sb.append(URLEncoder.encode(param.getKey(), charset) + "=" + URLEncoder.encode(param.getValue(), charset) + "&");
            return "?"+sb.toString();
        } catch(UnsupportedEncodingException e){
            WebhooksPlugin.plugin.getLogger().severe(e.getLocalizedMessage());
            return null;
        }
    }

    /** Parses variables in the parameters. */
    private void replaceParamVars(){
        replaceParamVar("{PLAYER_COUNT}", Bukkit.getOnlinePlayers().size()+"");
        replaceParamVar("{MAX_PLAYER_COUNT}", Bukkit.getMaxPlayers()+"");
        replaceParamVar("{SERVER_VERSION}", Bukkit.getVersion());
        replaceParamVar("{SERVER_MOTD}", Bukkit.getMotd());
        replaceParamVar("{WORLD_NAME}", Bukkit.getWorlds().get(0).getName());
    }

    /** Replace a variable in all parameters. */
    void replaceParamVar(String target, String replacement){
        paramVars.put(target, replacement);
    }
}