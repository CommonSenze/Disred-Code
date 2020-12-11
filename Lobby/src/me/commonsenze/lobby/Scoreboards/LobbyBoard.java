package me.commonsenze.lobby.Scoreboards;

import org.bukkit.entity.Player;

import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Interfaces.ScoreboardAdapter;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Scoreboard.ScoreboardUpdateEvent;
import me.commonsenze.core.Util.CC;

public class LobbyBoard implements ScoreboardAdapter {

	@Override
	public boolean isUpdatable(ScoreboardUpdateEvent event) {
		Profile profile = CoreAPI.getInstance().getProfile(event.getPlayer());
		event.setTitle(CC.AQUA + "Nova "+CC.WHITE+" Lobby");
		event.setSeparator(SEPERATOR);

		event.addLine(CC.WHITE +"Rank: "+CC.AQUA +(profile.hasRank() ? profile.getRank().getName() : "None"));
		event.addLine(CC.WHITE +"Online: "+CC.AQUA +CoreAPI.getInstance().getProfiles().size());
		event.addLine("  ");
		event.addLine(CC.GRAY +CC.ITALICS +"localhost");
		return true;
	}

	@Override
	public boolean isAvailable(Player player) {
		return true;
	}

	@Override
	public int getWeight() {
		return 0;
	}

	
}
