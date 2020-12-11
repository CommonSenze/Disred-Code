package me.commonsenze.kitpvp.Managers.impl;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Objects.TeamBuilder;
import me.commonsenze.core.Util.NameTagUpdateEvent;
import me.commonsenze.core.Util.NameTagUpdateEvent.Reason;
import me.commonsenze.kitpvp.Managers.Manager;
import me.commonsenze.kitpvp.Managers.ManagerHandler;

@Getter @Setter
public class ServerManager extends Manager {

	@Getter(value = AccessLevel.NONE)public static final String SERVER_FILE = "config";

	public ServerManager(ManagerHandler managerHandler) {
		super(managerHandler);
		registerAsListener();
	}

	@EventHandler
	public void hunger(FoodLevelChangeEvent e) {
		if (!getEditor(SERVER_FILE).getConfig().getBoolean("hunger-allowed", false)) {
			if (e.getFoodLevel() != 20)e.setFoodLevel(20);
			else e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void nameTagEvent(NameTagUpdateEvent event) {
		if (event.getReason() == Reason.PLAYER_JOIN) {
			event.getProfile().setTeam(new TeamBuilder(event.getProfile().getPersonalTeamName())
					.setPrefix(CoreAPI.getInstance().getManagerHandler().getPrefixManager().getPrefix(event.getProfile()))
					.toTeam());
			CoreAPI.getInstance().getProfiles().stream().filter(profile -> !profile.equals(event.getProfile())).forEach(profile -> {
				event.getScoreboardTeamManager().addTeamToScoreboard(event.getProfile().getPlayer(), profile.getCurrentTeam());
			});
		} else if (event.getReason() == Reason.RANK_CHANGE) {
			event.getProfile().setTeam(new TeamBuilder(event.getProfile().getPersonalTeamName())
					.setPrefix(CoreAPI.getInstance().getManagerHandler().getPrefixManager().getPrefix(event.getProfile()))
					.toTeam());
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent event) {
		event.getPlayer().teleport(CoreAPI.getInstance().getSpawnLocation());
	}
}
