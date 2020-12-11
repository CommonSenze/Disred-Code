package me.commonsenze.minigames.Scoreboards;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import me.commonsenze.core.Interfaces.ScoreboardAdapter;
import me.commonsenze.core.Scoreboard.ScoreboardUpdateEvent;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.GameState;
import me.commonsenze.minigames.Games.AmongUs.AmongUs;
import me.commonsenze.minigames.Games.AmongUs.AmongUs.Imposter;
import me.commonsenze.minigames.Games.AmongUs.Objects.Sabatoge;
import me.commonsenze.minigames.Games.AmongUs.Objects.Task;
import me.commonsenze.minigames.Objects.User;

public class AmongUsScoreboard implements ScoreboardAdapter {

	@Override
	public boolean isUpdatable(ScoreboardUpdateEvent event) {
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(event.getPlayer());
		AmongUs game = (AmongUs) user.getCurrentGame();
		if (user.getCurrentGame().getGameState() == GameState.STARTED) {
			event.setTitle(CC.GREEN + game +" "+CC.WHITE+"("+(game.isImposter(user) ? CC.RED + "Imposter" : CC.AQUA + "Crewmate")+CC.WHITE+")");

			double taskRatio = new BigDecimal((game.getTotalTask().keySet().stream().filter(task -> task.isCompleted()).count() / (double)game.getTotalTask().size())*100).doubleValue();

			event.addLine("Duration: ");
			event.addLine(" ");
			event.addLine(CC.WHITE + "Total Task Completed: "+CC.BLUE + ((int)taskRatio) + "%");
			event.addLine(" ");
			event.addLine(CC.WHITE + "Total Task Completed: "+CC.BLUE + ((int)taskRatio) + "%");

			if (game.isImposter(user)) {
				Imposter imposter = game.getImposter(user);
				long killCooldown = (imposter.isPaused() ? imposter.getPausedCooldownLeft()+System.currentTimeMillis() : imposter.getKillCooldown());
				
				double timeRemaining = new BigDecimal((killCooldown - System.currentTimeMillis())/1000D).setScale(1, RoundingMode.HALF_UP).doubleValue();
				
				if (timeRemaining > 0) {
					event.addLine(CC.WHITE+"Kill Cooldown: "+CC.GRAY+timeRemaining);
				} else {
					event.addLine(CC.WHITE+"Kill Cooldown: "+CC.BLUE+"Ready");
				}

				if (Sabatoge.getSabatogeCooldown() > System.currentTimeMillis()) {
					double sabatogeTimeRemaining = new BigDecimal((Sabatoge.getSabatogeCooldown() - System.currentTimeMillis())/1000D).setScale(1, RoundingMode.HALF_UP).doubleValue();
					event.addLine(CC.WHITE+"Sabatoge Cooldown: "+CC.GRAY+sabatogeTimeRemaining);
				} else
					event.addLine(CC.WHITE+"Current Sabatoge: "+CC.BLUE+(game.getSabatoge() != null ? game.getSabatoge().getType() : "None"));
				event.addLine(" ");
			} else {
				int total = (int)game.getTotalTask().entrySet().stream().filter(entry -> entry.getValue().equals(user.getUniqueId())).count();
				int completed = (int)game.getTotalTask().entrySet().stream().filter(entry -> entry.getValue().equals(user.getUniqueId())&&entry.getKey().isCompleted()).count();
				event.addLine(CC.WHITE + "Your Task Completed: "+CC.YELLOW + completed +CC.GRAY+ "/"+CC.BLUE+total);
				event.addLine(" ");
				event.addLine(CC.WHITE + "Task List:");
				for (Task task : 
					game.getTotalTask().entrySet().stream().filter(entry -> entry.getValue().equals(user.getUniqueId())).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())).keySet()) {
					event.addLine((task.isCompleted() ? CC.GREEN : CC.GRAY) + task.getType().getName() + 
							(task.isPartedTask() ? (task.getAmount() > 1&& task.getAmount()!= task.getFinishedAmount()? CC.YELLOW :"") +" ("+task.getAmount() + "/"+task.getFinishedAmount() +")" : ""));
				}
				event.addLine(" ");
			}

			event.addLine(CC.GRAY +CC.ITALICS +"localhost");
			return true;
		}
		return false;
	}

	@Override
	public boolean isAvailable(Player player) {
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(player);
		return user.getCurrentGame() != null&&user.getCurrentGame() instanceof AmongUs&&user.getCurrentGame().getGameState()!=GameState.LOBBY;
	}

	@Override
	public int getWeight() {
		return 0;
	}

}
