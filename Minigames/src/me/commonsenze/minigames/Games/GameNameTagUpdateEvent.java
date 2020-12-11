package me.commonsenze.minigames.Games;

import lombok.Getter;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Util.NameTagUpdateEvent;

@Getter
public class GameNameTagUpdateEvent extends NameTagUpdateEvent {

	public enum GameReason {
		START, END, SPECTATE, JOIN, LEAVE;
	}
	
	private GameReason gameReason;
	private Game currentGame;
	
	public GameNameTagUpdateEvent(Profile profile, GameReason gameReason, Game currentGame) {
		super(profile, Reason.PLUGIN);
		this.gameReason = gameReason;
		this.currentGame = currentGame;
	}

}
