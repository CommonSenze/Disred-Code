package me.commonsenze.core.Util;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Getter;
import me.commonsenze.core.Objects.Rank;

@Getter
public class RankUpdateEvent extends Event {
	
	private static final HandlerList HANDLER_LIST = new HandlerList();
	
	public enum RankChange {
		RANKING, NAME, TAB_COLOR;
	}
	
	private Rank rank;
	private RankChange change;
	
	public RankUpdateEvent(Rank rank, RankChange change) {
		this.rank = rank;
		this.change = change;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}
