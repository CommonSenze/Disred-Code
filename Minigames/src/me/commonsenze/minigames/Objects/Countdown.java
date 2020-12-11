package me.commonsenze.minigames.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Setter;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Games.Game;

public class Countdown extends BukkitRunnable {

	private Game game;
	@Setter private int seconds;

	public Countdown(Game game, int seconds) {
		this.game = game;
		this.seconds = seconds;
	}

	@Override
	public void run() {
		if (seconds >= 60&&seconds%30 ==0)
			Bukkit.broadcastMessage(CC.BLUE + game + " will begin in "+(seconds/60) +" minute"+(seconds/60 == 1 ? "" : "s")+(seconds%60 == 0 ? "" : " and "+(seconds%60)+" seconds"));
		else if (seconds < 15&&(seconds % 15 == 0 || seconds < 5)&&seconds != 0) {
			game.forEachUser(user -> {
				user.getPlayer().playSound(user.getPlayer().getLocation(), Sound.NOTE_BASS, 1, 1);
			});
			Bukkit.broadcastMessage(CC.BLUE + game + " will begin in "+seconds +" second"+(seconds == 1 ? "" : "s"));
		} else if (seconds == 0) {
			game.start();
			cancel();
			return;
		}
		
		seconds--;
	}

	public String getTime() {
		return seconds+"s";
	}
}
