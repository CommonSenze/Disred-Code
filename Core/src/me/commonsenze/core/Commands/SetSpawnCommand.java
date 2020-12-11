package me.commonsenze.core.Commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;

public class SetSpawnCommand extends Executor {

	public SetSpawnCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
		setPlayersOnly(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			sender.sendMessage(Lang.NO_PERMISSION);
			return true;
		}
		Player player = (Player) sender;
		if (args.length == 0) {
			Core.getInstance().getManagerHandler().getServerManager().setSpawn(player.getLocation());
			player.sendMessage(Lang.success("-nYou set spawn at your current location."));
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		ArrayList<String> help = new ArrayList<>();

		if (player.hasPermission(getPermission())) {
			help.add(ServerColor.PRIMARY + "/"+getName()+" - "+ServerColor.SECONDARY+"Set spawn at your current player location.");

		}

		return help;
	}
}
