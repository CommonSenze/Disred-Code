package me.commonsenze.minigames.Managers.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.scoreboard.NameTagVisibility;

import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.Objects.TeamBuilder;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Games.Game;
import me.commonsenze.minigames.Games.GameNameTagUpdateEvent;
import me.commonsenze.minigames.Games.GameNameTagUpdateEvent.GameReason;
import me.commonsenze.minigames.Games.AmongUs.AmongUs;
import me.commonsenze.minigames.Games.TNTRun.TNTRun;
import me.commonsenze.minigames.Managers.Manager;
import me.commonsenze.minigames.Managers.ManagerHandler;
import me.commonsenze.minigames.Objects.Countdown;
import me.commonsenze.minigames.Util.IncompleteGameRegisterException;

@Getter @Setter
public class GameManager extends Manager {

	private Set<Game> games, registered;
	private Map<String, Countdown> countdowns;

	public GameManager(ManagerHandler managerHandler) {
		super(managerHandler);
		this.games = new HashSet<>();
		this.registered = new HashSet<>();
		this.countdowns = new HashMap<>();
		registerAsListener();
		load();
	}

	private void load() {
		create(new AmongUs());
		create(new TNTRun());
	}

	public void create(Game game) {
		try {
			register(game);
		} catch (IncompleteGameRegisterException ex) {
			ex.printStackTrace();
		}
		this.games.add(game);
	}

	public void register(Game game) throws IncompleteGameRegisterException {
		if (games.contains(game))game.refresh();
		if (!game.isReady()) {
			throw new IncompleteGameRegisterException(game.toString());
		}
		this.registered.add(game);
	}

	public void unregister(Game game) {
		this.registered.remove(game);
	}

	public void reset(Game game, int seconds) {
		stopCountdown(game);
		startCountdown(game, seconds);
	}

	public boolean isRegistered(String name) {
		return isRegistered(getGame(name));
	}

	public boolean isRegistered(Game game) {
		return registered.stream().filter(g -> g.equals(game)).findFirst().orElse(null) != null;
	}

	public Countdown getCountdown(Game game) {
		return countdowns.get(game.getId());
	}

	public void stopCountdown(Game game) {
		if (hasCountdown(game)) {
			countdowns.get(game.getId()).cancel();
			countdowns.remove(game.getId());
		}
	}

	public void startCountdown(Game game, int seconds) {
		if (!hasCountdown(game)) {
			countdowns.put(game.getId(), new Countdown(game, seconds));
			countdowns.get(game.getId()).runTaskTimer(getPlugin(), 0, 20);
		}
	}

	public boolean hasCountdown(Game game) {
		return countdowns.containsKey(game.getId());
	}

	public Game getGame(String name) {
		return games.stream().filter(game -> game.getId().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public void unload() {
		for (Game game : registered) {
			game.end();
		}
		games.clear();
		registered.clear();
	}

	@EventHandler
	public void nameTagGameChange(GameNameTagUpdateEvent event) {
		if (event.getGameReason() == GameReason.JOIN) {
			event.getProfile().setTeam(new TeamBuilder(event.getProfile().getCurrentTeam()).setNameTagVisibility(NameTagVisibility.NEVER).toTeam());
			event.getProfile().getPlayer().setPlayerListName(CC.GRAY +CC.MAGIC + "123456789012");
		}
	}
}
