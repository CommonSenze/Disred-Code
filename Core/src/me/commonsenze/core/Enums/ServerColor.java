package me.commonsenze.core.Enums;

import me.commonsenze.core.Core;
import me.commonsenze.core.Managers.impl.ServerManager;
import net.md_5.bungee.api.ChatColor;

public enum ServerColor {

	PRIMARY(Core.getInstance().getConfig(ServerManager.SERVER_FILE).getConfig().getString("server-color.primary", "&b")),
	SECONDARY(Core.getInstance().getConfig(ServerManager.SERVER_FILE).getConfig().getString("server-color.secondary", "&f"));
	
	private String color;
	
	private ServerColor(String color) {
		this.color = color;
	}
	
	public String getColor() {
		return ChatColor.translateAlternateColorCodes('&', color);
	}
	
	@Override
	public String toString() {
		return getColor();
	}
}
