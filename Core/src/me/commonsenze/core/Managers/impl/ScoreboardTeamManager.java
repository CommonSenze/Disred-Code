package me.commonsenze.core.Managers.impl;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import me.commonsenze.core.Managers.Manager;
import me.commonsenze.core.Managers.ManagerHandler;

public class ScoreboardTeamManager extends Manager {

	public ScoreboardTeamManager(ManagerHandler managerHandler) {
		super(managerHandler);
	}

	public void addTeamToScoreboard(Player player, Team team) {
		if (player.getScoreboard().getTeam(team.getName())==null) {
			player.getScoreboard().registerNewTeam(team.getName());
		}
		player.getScoreboard().getTeam(team.getName()).setPrefix(team.getPrefix());
		player.getScoreboard().getTeam(team.getName()).setNameTagVisibility(team.getNameTagVisibility());
		player.getScoreboard().getTeam(team.getName()).setCanSeeFriendlyInvisibles(team.canSeeFriendlyInvisibles());
		team.getEntries().stream().filter(entry -> !player.getScoreboard().getTeam(team.getName()).hasEntry(entry)).forEach(player.getScoreboard().getTeam(team.getName())::addEntry);
	}
}
