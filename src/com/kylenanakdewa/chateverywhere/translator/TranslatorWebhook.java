package com.kylenanakdewa.chateverywhere.translator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.kylenanakdewa.simplewebhooks.Webhook;

/**
 * The webhook used for translation.
 *
 * @author Kyle Nanakdewa
 */
class TranslatorWebhook extends Webhook {

    private static final Map<String,String> QUERY_PARAMS;
    static {
        Map<String,String> map = new HashMap<String,String>();
        map.put("key", "{TRANSLATOR_API_KEY}");
        map.put("text", "{CHAT_MESSAGE}");
        map.put("lang", "{TRANSLATOR_LANGUAGE_CODE}");
        QUERY_PARAMS = Collections.unmodifiableMap(map);
    }

    TranslatorWebhook(String key) throws MalformedURLException {
        super(new URL("https://translate.yandex.net/api/v1.5/tr/translate"), QUERY_PARAMS);
        replaceParamVar("{TRANSLATOR_API_KEY}", key);
    }
}