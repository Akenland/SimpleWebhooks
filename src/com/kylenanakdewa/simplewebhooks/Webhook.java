package com.kylenanakdewa.simplewebhooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
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

    /** JSON content for POST requests. */
    private Map<String,String> jsonParams;


    /** Variables in params that should be replaced on execution. */
    private Map<String,String> paramVars;


    /** Creates a webhook. */
    public Webhook(URL url, Map<String,String> queryParams, RequestType requestType, Map<String,String> jsonParams){
        this.url = url;
        this.charset = java.nio.charset.StandardCharsets.UTF_8.name();
        this.queryParams = queryParams;
        this.requestType = requestType;
        this.jsonParams = jsonParams;

        // Param variables
        paramVars = new HashMap<String,String>();
        replaceParamVars();
    }
    /** Creates a simple GET webhook. */
    public Webhook(URL url, Map<String,String> queryParams){
        this(url, queryParams, RequestType.GET, null);
    }


    /** Gets the URL for this webhook. */
    public URL getURL(){
        return url;
    }


    /** Executes the webhook, sending the data to the server. */
    public InputStream execute(){
        //WebhooksPlugin.plugin.getLogger().info("Executing "+requestType+" webhook for URL "+url);
        try{
            // Encode query params
            String encodedQuery = getEncodedParams(queryParams);
            if(!encodedQuery.isEmpty()) encodedQuery = "?"+encodedQuery;

            // Open the connection
            URLConnection connection = new URL(url+encodedQuery).openConnection();
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

            // If POST, send output data
            if(requestType.equals(RequestType.POST)){
                connection.setDoOutput(true);

                // Encode JSON params
                String jsonOutput = getJsonParams(jsonParams);

                if(jsonOutput.length()>2) connection.getOutputStream().write(jsonOutput.getBytes(charset));
            }

            // Complete the HTTP request
            connection.connect();
            int responseCode = ((HttpURLConnection)connection).getResponseCode();
            if(responseCode!=200 && responseCode!=201 && responseCode!=204 && responseCode!=304)
                WebhooksPlugin.plugin.getLogger().warning(requestType+" webhook for URL "+url+" returned error code "+responseCode+": "+((HttpURLConnection)connection).getResponseMessage());
            //WebhooksPlugin.plugin.getLogger().info("Webhook executed. "+((HttpURLConnection)connection).getResponseCode()+": "+((HttpURLConnection)connection).getResponseMessage());

            return connection.getInputStream();

        } catch(IOException e){
            WebhooksPlugin.plugin.getLogger().severe(e.getLocalizedMessage());
            return null;
		}
    }

    /** Encodes parameters. Also replaces variables. */
    private String getEncodedParams(Map<String,String> params){
        if(params.isEmpty()) return "";

        // Convert variables
        Map<String,String> convertedParams = new HashMap<String,String>(params);
        for(Map.Entry<String,String> var : paramVars.entrySet()){
            for(Map.Entry<String,String> param : convertedParams.entrySet()){
                convertedParams.replace(param.getKey(), param.getValue().replace(var.getKey(), var.getValue()));
            }
        }

        try{
            StringBuilder sb = new StringBuilder();
            for(Map.Entry<String,String> param : convertedParams.entrySet())
                sb.append(URLEncoder.encode(param.getKey(), charset) + "=" + URLEncoder.encode(param.getValue(), charset) + "&");
            sb.deleteCharAt(sb.length()-1);
            return sb.toString();
        } catch(UnsupportedEncodingException e){
            WebhooksPlugin.plugin.getLogger().severe(e.getLocalizedMessage());
            return "";
        }
    }
    /** Encodes parameters. Also replaces variables. */
    private String getJsonParams(Map<String,String> params){
        if(params.isEmpty()) return "";

        // Convert variables
        Map<String,String> convertedParams = new HashMap<String,String>(params);
        for(Map.Entry<String,String> var : paramVars.entrySet()){
            for(Map.Entry<String,String> param : convertedParams.entrySet()){
                convertedParams.replace(param.getKey(), param.getValue().replace(var.getKey(), var.getValue()));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for(Map.Entry<String,String> param : convertedParams.entrySet())
            if(param.getKey()!=null && !param.getKey().isEmpty() && param.getValue()!=null && !param.getValue().isEmpty())
                sb.append("\""+param.getKey() + "\":\"" + param.getValue() + "\",");
        if(sb.toString().endsWith(",")) sb.deleteCharAt(sb.length()-1);
        sb.append('}');

        //WebhooksPlugin.plugin.getLogger().info("JSON formatted as: "+sb);
        return sb.toString();
    }

    /** Parses variables in the parameters. */
    private void replaceParamVars(){
        replaceParamVar("{PLAYER_COUNT}", Bukkit.getOnlinePlayers().size()+"");
        replaceParamVar("{MAX_PLAYER_COUNT}", Bukkit.getMaxPlayers()+"");
        replaceParamVar("{SERVER_VERSION}", Bukkit.getVersion());
        replaceParamVar("{SERVER_MOTD}", Bukkit.getMotd());
        replaceParamVar("{WORLD_NAME}", Bukkit.getWorlds().get(0).getName());
        // Escape " and \
        replaceParamVar("\"", "%22");
        replaceParamVar("\\", "%5C");
    }

    /** Replace a variable in all parameters. */
    public void replaceParamVar(String target, String replacement){
        paramVars.put(target, replacement);
    }
}