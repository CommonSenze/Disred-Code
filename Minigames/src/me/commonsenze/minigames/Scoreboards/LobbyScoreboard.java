package me.commonsenze.minigames.Scoreboards;

import org.bukkit.entity.Player;

import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Interfaces.ScoreboardAdapter;
import me.commonsenze.core.Scoreboard.ScoreboardUpdateEvent;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Objects.User;

public class LobbyScoreboard implements ScoreboardAdapter {

	@Override
	public boolean isUpdatable(ScoreboardUpdateEvent event) {
		event.setTitle(CC.AQUA + "Server "+CC.WHITE+" Lobby");
		event.setSeparator(SEPERATOR);

		event.addLine(CC.WHITE +"Playing: "+CC.AQUA +((int)Minigames.getInstance().getManagerHandler().getUserManager().getUserSet().stream().filter(u -> u.inGame()).count()));
		event.addLine(CC.WHITE +"Online: "+CC.AQUA +CoreAPI.getInstance().getProfiles().size());
		event.addLine("  ");
		event.addLine(CC.GRAY +CC.ITALICS +"localhost");
		return true;
	}

	@Override
	public boolean isAvailable(Player player) {
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(player);
		return user.getCurrentGame() == null;
	}

	@Override
	public int getWeight() {
		return 0;
	}
}
