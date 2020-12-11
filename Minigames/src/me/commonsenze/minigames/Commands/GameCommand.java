package me.commonsenze.minigames.Commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.Game;
import me.commonsenze.minigames.Games.Game.QuitReason;
import me.commonsenze.minigames.Objects.User;

public class GameCommand extends Executor {

	public GameCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("leave")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Lang.fail("-nOnly players can use this command"));
					return true;
				}
				User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser((Player)sender);
				Game game = user.getCurrentGame();
				if (game == null) {
					sender.sendMessage(Lang.fail("-nYou are currently not in a game."));
					return true;
				}
				sender.sendMessage(Lang.success("-nLeaving -e"+game+"-n."));
				game.quit(user.getPlayer(), QuitReason.LEAVE);
				return true;
			}
		}
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("start")) {
				if (!sender.hasPermission(getPermission())) {
					sender.sendMessage(Lang.NO_PERMISSION);
					return true;
				}
				Game game = Minigames.getInstance().getManagerHandler().getGameManager().getGame(args[0]);
				if (game == null) {
					sender.sendMessage(Lang.fail("-nThere currently isn't a valid game."));
					return true;
				}
				if (game.isStarted()) {
					sender.sendMessage(Lang.fail("-e"+game+" -nis already in progress."));
					return true;
				}
				game.start();
				sender.sendMessage(Lang.success("-nForcibly starting -e"+game+"-n."));
				return true;
			}
			if (args[1].equalsIgnoreCase("end")) {
				if (!sender.hasPermission(getPermission())) {
					sender.sendMessage(Lang.NO_PERMISSION);
					return true;
				}
				Game game = Minigames.getInstance().getManagerHandler().getGameManager().getGame(args[0]);
				if (game == null) {
					sender.sendMessage(Lang.fail("-nThere currently isn't a valid game."));
					return true;
				}
				if (!game.isStarted()&&!Minigames.getInstance().getManagerHandler().getGameManager().hasCountdown(game)) {
					sender.sendMessage(Lang.fail("-e"+game+" -nis currently not started."));
					return true;
				} else if (Minigames.getInstance().getManagerHandler().getGameManager().hasCountdown(game)) {
					Minigames.getInstance().getManagerHandler().getGameManager().stopCountdown(game);
					sender.sendMessage(Lang.success("-nForcibly stopping countdown for -e"+game+"-n."));
					return true;
				}
				game.end();
				sender.sendMessage(Lang.success("-nForcibly ending -e"+game+"-n."));
				return true;
			}
			if (args[1].equalsIgnoreCase("check")) {
				if (!sender.hasPermission(getPermission())) {
					sender.sendMessage(Lang.NO_PERMISSION);
					return true;
				}
				Game game = Minigames.getInstance().getManagerHandler().getGameManager().getGame(args[0]);
				if (game == null) {
					sender.sendMessage(Lang.fail("-nThere currently isn't a valid game."));
					return true;
				}
				sender.sendMessage(game.getTodoList());
				return true;
			}
			if (args[1].equalsIgnoreCase("register")) {
				if (!sender.hasPermission(getPermission())) {
					sender.sendMessage(Lang.NO_PERMISSION);
					return true;
				}
				Game game = Minigames.getInstance().getManagerHandler().getGameManager().getGame(args[0]);
				if (game == null) {
					sender.sendMessage(Lang.fail("-nThere currently isn't a valid game."));
					return true;
				}
				if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(game.getId())) {
					sender.sendMessage(Lang.fail("-e"+game+" is already registered."));
					return true;
				}
				Minigames.getInstance().getManagerHandler().getGameManager().register(game);
				sender.sendMessage(Lang.success("-nRegistering -e"+game+"-n."));
				return true;
			}
			if (args[1].equalsIgnoreCase("unregister")) {
				if (!sender.hasPermission(getPermission())) {
					sender.sendMessage(Lang.NO_PERMISSION);
					return true;
				}
				Game game = Minigames.getInstance().getManagerHandler().getGameManager().getGame(args[0]);
				if (game == null) {
					sender.sendMessage(Lang.fail("-nThere currently isn't a valid game."));
					return true;
				}
				if (!Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(game.getId())) {
					sender.sendMessage(Lang.fail("-e"+game+" is already unregistered."));
					return true;
				}
				Minigames.getInstance().getManagerHandler().getGameManager().unregister(game);
				sender.sendMessage(Lang.success("-nUnregistering -e"+game+"-n."));
				return true;
			}
			if (args[1].equalsIgnoreCase("join")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Lang.fail("-nOnly players can use this command"));
					return true;
				}
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
				game.join(user.getPlayer());
				return true;
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		ArrayList<String> help = new ArrayList<>();

		if (player.hasPermission(getPermission())) {
			help.add(ServerColor.PRIMARY + "/"+getName()+" <game> start - "+ServerColor.SECONDARY+"Start <game>.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" <game> end - "+ServerColor.SECONDARY+"End the game that is currently started.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" <game> check - "+ServerColor.SECONDARY+"Check to see if <game> is ready and able to play.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" <game> register - "+ServerColor.SECONDARY+"Register <game> if ready and not registered.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" <game> unregister - "+ServerColor.SECONDARY+"Unregister <game> to make changes to the game.");
		}
		
		help.add(ServerColor.PRIMARY + "/"+getName()+" <game> join - "+ServerColor.SECONDARY+"Join <game>.");
		help.add(ServerColor.PRIMARY + "/"+getName()+" leave - "+ServerColor.SECONDARY+"Leave the game you're currently in.");

		return help;
	}
}
