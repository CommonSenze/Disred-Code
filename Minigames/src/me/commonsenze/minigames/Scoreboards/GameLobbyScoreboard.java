package me.commonsenze.minigames.Scoreboards;

import org.bukkit.entity.Player;

import me.commonsenze.core.Lang;
import me.commonsenze.core.Interfaces.ScoreboardAdapter;
import me.commonsenze.core.Scoreboard.ScoreboardUpdateEvent;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.Game;
import me.commonsenze.minigames.Games.GameState;
import me.commonsenze.minigames.Objects.User;

public class GameLobbyScoreboard implements ScoreboardAdapter {

	@Override
	public boolean isUpdatable(ScoreboardUpdateEvent event) {
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(event.getPlayer());
		Game game = user.getCurrentGame();
		event.setTitle(CC.GREEN + game +" "+Lang.VERT_DIVIDER+" Lobby");
		event.addLine(" ");
		event.addLine(CC.WHITE+"Map: "+CC.GREEN+"Default");
		if (Minigames.getInstance().getManagerHandler().getGameManager().hasCountdown(game)) {
			event.addLine(" ");
			event.addLine(CC.WHITE+"Starting in "+CC.GREEN+Minigames.getInstance().getManagerHandler().getGameManager().getCountdown(game).getTime());
		}
		event.addLine(" ");
		event.addLine(CC.WHITE+"Players: "+CC.GREEN+game.getUsers().size()+ CC.WHITE+"/"+CC.GREEN+game.getMaxPlayers());
		event.addLine(" ");
		event.addLine(CC.GRAY + "localhost");
		return true;
	}

	@Override
	public boolean isAvailable(Player player) {
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(player);
		return user.getCurrentGame() != null&&user.getCurrentGame().getGameState() == GameState.LOBBY;
	}

	@Override
	public int getWeight() {
		return 0;
	}
}
