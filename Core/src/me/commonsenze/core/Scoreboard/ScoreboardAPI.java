package me.commonsenze.core.Scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.commonsenze.core.Core;
import me.commonsenze.core.Interfaces.ScoreboardAdapter;

public class ScoreboardAPI implements Listener, Runnable {

	private static final String OBJECTIVE_ID = "objective";
	private final List<ScoreboardAdapter> adapters = new ArrayList<>();
	private final Core plugin;

	public ScoreboardAPI(Core plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this, 0L, 1);
	}

	public void updateScoreboard(Player player) {
		Scoreboard board = player.getScoreboard();
		Objective objective = board.getObjective(OBJECTIVE_ID);

		if (objective == null) {
			try {objective = board.registerNewObjective(OBJECTIVE_ID, "dummy");} catch (Exception e) {return;}
			objective.setDisplayName("");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}

		ScoreboardUpdateEvent event = new ScoreboardUpdateEvent(player, objective.getDisplayName());

		plugin.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
			for (String entry : board.getEntries()) {
				board.resetScores(entry);
			}
			return;
		}

		if (!objective.getDisplayName().equals(event.getTitle())) {
			objective.setDisplayName(event.getTitle());
		}

		List<ScoreboardLine> lines = event.getLines();

		if (lines.size() > 0) {
			if (!event.getHeader().isEmpty()) {
				event.setLine(0, event.getHeader());
			}

			if (!event.getFooter().isEmpty()) {
				event.addLine(event.getFooter());
			}
		}

		List<Team> teams = new ArrayList<>();

		for (int i = 0; i < ChatColor.values().length; i++) {
			if (board.getTeam("#line-" + i) == null) {
				board.registerNewTeam("#line-" + i);
			}

			teams.add(board.getTeam("#line-" + i));
		}

		for (int i = 0; i < lines.size(); i++) {
			Team team = teams.get(i);
			ScoreboardLine line = event.getLine(i);
			String prefix = line.getPrefix();
			String suffix = line.getSuffix();

			if (!team.getPrefix().equals(prefix)) {
				team.setPrefix(prefix);
			}

			if (!team.getSuffix().equals(suffix)) {
				team.setSuffix(line.getSuffix());
			}

			String entry = ChatColor.values()[i] + line.getPrefixFinalColor();
			Set<String> entries = team.getEntries();

			if (entries.size() == 0) {

				team.addEntry(entry);

				objective.getScore(entry).setScore(lines.size() - i);
			} else if (entries.size() == 1) {
				String already = entries.iterator().next();

				if (!entry.equals(already)) {
					board.resetScores(already);

					team.removeEntry(already);
					team.addEntry(entry);

					objective.getScore(entry).setScore(lines.size() - i);
				} else {
					objective.getScore(already).setScore(lines.size() - i);
				}
			}
		}

		for (int i = lines.size(); i < ChatColor.values().length; i++) {
			Team team = teams.get(i);
			Set<String> entries = team.getEntries();

			if (entries.size() > 0) {
				for (String entry : entries) {

					board.resetScores(entry);
					team.removeEntry(entry);

				}
			}
		}
	}

	@EventHandler
	public void onScoreboardUpdate(ScoreboardUpdateEvent event) {
		if (Core.getInstance().getManagerHandler().getProfileManager().getProfile(event.getPlayer())==null) {
			event.setCancelled(true);
			return;
		}
		
		for (ScoreboardAdapter adapter : sortAdapters()) {
			if (!adapter.isAvailable(event.getPlayer()))continue;
			if (adapter.isUpdatable(event))break;
		}
		
		if (event.getLines().isEmpty()||!Core.getInstance().getManagerHandler().getProfileManager().getProfile(event.getPlayer()).hasScoreboardVisibility())event.setCancelled(true);
	}
	
	public List<ScoreboardAdapter> sortAdapters() {
		List<ScoreboardAdapter> toReturn = new ArrayList<>(adapters);
        toReturn.sort(Comparator.comparingInt(ScoreboardAdapter::getWeight));
        Collections.reverse(toReturn);
        return toReturn;
	}
	
	public void addScoreboard(ScoreboardAdapter scoreboard) {
		adapters.add(scoreboard);
	}

	@Override
	public void run() {
		plugin.getServer().getOnlinePlayers().forEach(this::updateScoreboard);
	}

	public void removeScoreboard(ScoreboardAdapter scoreboard) {
		adapters.remove(scoreboard);
	}
}
