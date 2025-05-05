package com.copestudios.csspvp.utils;

import com.copestudios.csspvp.CSSPVP;

import java.util.HashMap;
import java.util.Map;

public class MessageId {

    /**
     * Get a message from the message config
     *
     * @param key Message key
     * @return Message with plugin prefix
     */
    public static String get(String key) {
        CSSPVP plugin = CSSPVP.getInstance();
        return plugin.getMessageConfig().getMessage("plugin.prefix") + plugin.getMessageConfig().getMessage(key);
    }

    /**
     * Get a message from the message config with placeholders
     *
     * @param key Message key
     * @param placeholders Map of placeholders to values
     * @return Message with plugin prefix and placeholders replaced
     */
    public static String get(String key, Map<String, String> placeholders) {
        CSSPVP plugin = CSSPVP.getInstance();
        return plugin.getMessageConfig().getMessage("plugin.prefix") + plugin.getMessageConfig().getMessage(key, placeholders);
    }

    /**
     * Get a message from the message config with a single placeholder
     *
     * @param key Message key
     * @param placeholder Placeholder name
     * @param value Placeholder value
     * @return Message with plugin prefix and placeholder replaced
     */
    public static String get(String key, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        return get(key, placeholders);
    }
}