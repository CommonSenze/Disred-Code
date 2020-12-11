package me.commonsenze.core.Interfaces;

import org.bukkit.entity.Player;

import me.commonsenze.core.Scoreboard.ScoreboardUpdateEvent;
import net.md_5.bungee.api.ChatColor;

public interface ScoreboardAdapter {
	
	String SEPERATOR = ChatColor.GRAY+ChatColor.STRIKETHROUGH.toString() + "--------------------";
	
	boolean isUpdatable(ScoreboardUpdateEvent event);
    
    boolean isAvailable(Player player);
    
    int getWeight();
}
