package me.commonsenze.minigames.Scoreboards;

import org.bukkit.entity.Player;

import me.commonsenze.core.Interfaces.ScoreboardAdapter;
import me.commonsenze.core.Scoreboard.ScoreboardUpdateEvent;
import me.commonsenze.core.Util.CC;
import me.commonsenze.core.Util.DateUtil;
import me.commonsenze.core.Util.DateUtil.MaxConverter;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.GameState;
import me.commonsenze.minigames.Games.TNTRun.TNTRun;
import me.commonsenze.minigames.Objects.User;

public class TNTRunScoreboard implements ScoreboardAdapter {

	@Override
	public boolean isUpdatable(ScoreboardUpdateEvent event) {
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(event.getPlayer());
		TNTRun game = (TNTRun) user.getCurrentGame();
		if (user.getCurrentGame().getGameState() == GameState.STARTED) {
			event.setTitle(CC.GREEN + game.toString().toUpperCase());

			int[] format = DateUtil.convertElapsedTime(game.getStartTime(), System.currentTimeMillis(), MaxConverter.MINUTES);

			event.addLine(CC.GRAY + "Duration: "+format[1] +"m " + format[0]+"s");
			event.addLine(" ");
			event.addLine(CC.WHITE + "Players Alive: "+CC.GREEN+((int)game.filter(game::isPlaying).count()));
			event.addLine(" ");

			event.addLine(CC.GRAY +CC.ITALICS +"localhost");
			return true;
		}
		return false;
	}

	@Override
	public boolean isAvailable(Player player) {
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(player);
		return user.getCurrentGame() != null&&user.getCurrentGame() instanceof TNTRun;
	}

	@Override
	public int getWeight() {
		return 0;
	}

}
