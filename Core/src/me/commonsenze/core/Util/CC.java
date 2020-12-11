package me.commonsenze.core.Util;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

public class CC {

	public static final String BOLD = ChatColor.BOLD.toString();
    public static final String STRIKETHROUGH = ChatColor.STRIKETHROUGH.toString();
    public static final String UNDERLINE = ChatColor.UNDERLINE.toString();
    public static final String ITALICS = ChatColor.ITALIC.toString();
    public static final String RESET = ChatColor.RESET.toString();
    public static final String AQUA = ChatColor.AQUA.toString();
    public static final String BLACK = ChatColor.BLACK.toString();
    public static final String BLUE = ChatColor.BLUE.toString();
    public static final String GOLD = ChatColor.GOLD.toString();
    public static final String GREEN = ChatColor.GREEN.toString();
    public static final String PINK = ChatColor.LIGHT_PURPLE.toString();
    public static final String RED = ChatColor.RED.toString();
    public static final String WHITE = ChatColor.WHITE.toString();
    public static final String YELLOW = ChatColor.YELLOW.toString();
    public static final String DARKAQUA = ChatColor.DARK_AQUA.toString();
    public static final String DARKBLUE = ChatColor.DARK_BLUE.toString();
    public static final String GRAY = ChatColor.GRAY.toString();
    public static final String DARKGRAY = ChatColor.DARK_GRAY.toString();
    public static final String DARKGREEN = ChatColor.DARK_GREEN.toString();
    public static final String PURPLE = ChatColor.DARK_PURPLE.toString();
    public static final String DARKRED = ChatColor.DARK_RED.toString();
	public static final String ITALIC = ChatColor.ITALIC.toString();
	public static final String MAGIC = ChatColor.MAGIC.toString();

    public static String getColorName(ChatColor color) {
    	return color.name();
    }
    
    public static boolean isChatColor(String string) {
		if (string.isEmpty())return false;
		return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string)).isEmpty();
	}
    
    public static String translate(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    public static List<String> translate(List<String> lines) {
        List<String> toReturn = new ArrayList<>();

        for (String line : lines) {
            toReturn.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return toReturn;
    }

    public static List<String> translate(String[] lines) {
        List<String> toReturn = new ArrayList<>();

        for (String line : lines) {
            if (line != null) {
                toReturn.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        return toReturn;
    }

	public static String clear(String message) {
		return ChatColor.stripColor(message);
	}
}
