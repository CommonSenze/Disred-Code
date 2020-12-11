package me.commonsenze.minigames.Util;

import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import lombok.Getter;
import me.commonsenze.minigames.Objects.User;

public class UserEvent extends PlayerEvent {

	private static final HandlerList HANDLER_LIST = new HandlerList();
	
	@Getter private User user;
	
	public UserEvent(User user) {
		super(user.getPlayer());
		this.user = user;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	
	public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
