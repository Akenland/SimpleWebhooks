package com.akenland.simplewebhooks;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

/**
 * A Webhook that can be executed to send a payload to a webserver.
 */
class Webhook {

    /** The available types of HTTP requests. */
    enum RequestMethod {
        GET, POST, PUT, DELETE, HEAD;
    }

    /** The type of HTTP request. */
    private final RequestMethod requestMethod;

    /** The URI of this webhook. */
    private final String uri;

    /** The headers to use for this request. */
    private final Map<String, String> headers;

    /** The body for POST or PUT requests. */
    private final String body;

    /** Creates a webhook. */
    Webhook(RequestMethod method, String uri, Map<String, String> headers, String body) {
        this.requestMethod = method;
        this.uri = uri;
        this.headers = headers;
        this.body = body;
    }

    /** Gets the URI for this webhook. */
    String getURI() {
        return uri;
    }

    /**
     * Gets a HttpRequest instance for this webhook.
     */
    HttpRequest getHttpRequest(Event triggeringEvent) throws URISyntaxException {
        // Replace variables
        var uriString = replaceVariables(this.uri, true, triggeringEvent);
        var headers = new HashMap<String, String>();
        for (var entry : this.headers.entrySet()) {
            headers.put(entry.getKey(), replaceVariables(entry.getValue(), true, triggeringEvent));
        }
        var body = replaceVariables(this.body, false, triggeringEvent);

        // Create the request
        var uri = new URI(uriString);
        var builder = HttpRequest.newBuilder(uri);
        var bodyPublisher = body != null && !body.isEmpty() ? HttpRequest.BodyPublishers.ofString(body)
                : HttpRequest.BodyPublishers.noBody();
        builder.method(requestMethod.name(), bodyPublisher);
        for (var entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    /**
     * Replaces variables in the text with their values.
     * 
     * @param text                 The text to replace variables in.
     * @param removeUnreplacedVars Whether to remove unreplaced variables from the
     *                             text. If false, the variable will remain as-is.
     * @param triggeringEvent      The event that triggered this webhook.
     * @return The text with variables replaced.
     */
    private static String replaceVariables(final String text, final boolean removeUnreplacedVars,
            final Event triggeringEvent) {
        // Holds the new string, so that the changing length of the string doesn't
        // affect indexes when searching
        String newText = text;

        // Cache variables so that we only call the event once per unique variable
        Set<String> replacedVariables = new HashSet<String>();

        // Search for variables in the text
        // All variables are in the format {{variable}}

        // Look for {{ starting from the beginning of the string
        // If found, look for the next }} and extract the variable name
        int start = text.indexOf("{{");
        while (start != -1) {
            int end = text.indexOf("}}", start);
            if (end == -1) {
                // No closing bracket found
                break;
            }
            var variableName = text.substring(start + 2, end);

            // If the variable has already been replaced, skip it
            if (!replacedVariables.contains(variableName)) {
                // Call the event to allow any listener to replace the variable
                var event = new WebhookVariableEvent(variableName, triggeringEvent, triggeringEvent.isAsynchronous());
                Bukkit.getPluginManager().callEvent(event);

                // If the event has a replacement, use it
                var value = event.getReplacement();
                if (value != null) {
                    // Replace all instances of the variable in the text
                    newText = newText.replace("{{" + variableName + "}}", value);
                } else if (removeUnreplacedVars) {
                    // Remove the variable if it wasn't replaced
                    newText = newText.replace("{{" + variableName + "}}", "");
                }

                // Prevent replacing the same variable multiple times
                replacedVariables.add(variableName);
            }

            // Look for the next variable
            start = text.indexOf("{{", end);
        }

        return newText;
    }

}