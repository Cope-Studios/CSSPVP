package com.copestudios.csspvp.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Colorize a string with Minecraft color codes and hex colors
     *
     * @param text Text to colorize
     * @return Colorized text
     */
    public static String colorize(String text) {
        if (text == null) {
            return "";
        }

        // Convert hex colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + hexColor.charAt(0) + ChatColor.COLOR_CHAR + hexColor.charAt(1)
                    + ChatColor.COLOR_CHAR + hexColor.charAt(2) + ChatColor.COLOR_CHAR + hexColor.charAt(3)
                    + ChatColor.COLOR_CHAR + hexColor.charAt(4) + ChatColor.COLOR_CHAR + hexColor.charAt(5));
        }

        matcher.appendTail(buffer);

        // Convert & color codes
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}