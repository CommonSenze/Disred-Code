package me.commonsenze.core.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import lombok.Getter;

public class TeamBuilder {

	@Getter private String name;
	private Team team;
	
	public TeamBuilder() {
		this("temp");
	}
	
	public TeamBuilder(String name) {
		this.name = name;
		this.team = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam(name);
	}
	
	public TeamBuilder(Team team) {
		this.name = team.getName();
		this.team = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam(name);
		this.team.setAllowFriendlyFire(team.allowFriendlyFire());
		this.team.setPrefix(team.getPrefix());
		this.team.setCanSeeFriendlyInvisibles(team.canSeeFriendlyInvisibles());
		this.team.setNameTagVisibility(team.getNameTagVisibility());
		this.team.setSuffix(team.getSuffix());
	}
	
	public TeamBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	public TeamBuilder setPrefix(String name) {
		this.team.setPrefix(name);
		return this;
	}
	
	public TeamBuilder setNameTagVisibility(NameTagVisibility visibility) {
		this.team.setNameTagVisibility(visibility);
		return this;
	}
	
	public TeamBuilder setCanSeeFriendlyInvisibles(boolean canSeeFriendlyInvisibles) {
		this.team.setCanSeeFriendlyInvisibles(canSeeFriendlyInvisibles);
		return this;
	}
	
	public TeamBuilder addPlayers(Player... players) {
		for (Player player : players) {
			this.team.addEntry(player.getName());
		}
		return this;
	}
	
	public Team toTeam() {
		return team;
	}
}
