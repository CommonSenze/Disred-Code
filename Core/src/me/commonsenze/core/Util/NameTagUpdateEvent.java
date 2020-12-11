package me.commonsenze.core.Util;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Getter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Managers.impl.ScoreboardTeamManager;
import me.commonsenze.core.Objects.Profile;

@Getter
public class NameTagUpdateEvent extends Event {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private Profile profile;
	private Reason reason;
	private ScoreboardTeamManager scoreboardTeamManager;
	
	public enum Reason {
		RANK_CHANGE, PLAYER_JOIN, PLUGIN;
	}
	
	public NameTagUpdateEvent(Profile profile, Reason reason) {
		this.profile = profile;
		this.reason = reason;
		this.scoreboardTeamManager = Core.getInstance().getManagerHandler().getScoreboardTeamManager();
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}
