package me.commonsenze.minigames.Commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.Game;
import me.commonsenze.minigames.Objects.User;

public class JoinCommand extends Executor {

	public JoinCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 1) {
			User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser((Player)sender);
			Game game = Minigames.getInstance().getManagerHandler().getGameManager().getGame(args[0]);
			if (game == null) {
				sender.sendMessage(Lang.fail("-nThere currently isn't a valid game."));
				return true;
			}
			if (!Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(game)) {
				sender.sendMessage(Lang.fail("-e"+game+" -nis currently not registered to play."));
				return true;
			}
			if (!game.isReady()) {
				sender.sendMessage(Lang.fail("-e"+game+" -nis currently not ready for gameplay."));
				return true;
			}
			if (game.isStarted()) {
				sender.sendMessage(Lang.fail("-e"+game+" -nis already in progress."));
				return true;
			}
			sender.sendMessage(Lang.success("-nJoining -e"+game+"-n."));
			Bukkit.broadcastMessage(CC.YELLOW + user.getPlayer().getName() + CC.GOLD + " has joined "+ CC.YELLOW+game+ CC.GOLD+".");
			game.join(user.getPlayer());
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		ArrayList<String> help = new ArrayList<>();

		help.add(ServerColor.PRIMARY + "/"+getName()+" <game> - "+ServerColor.SECONDARY+"Join <game>.");

		return help;
	}
}
