package me.commonsenze.lobby.Commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.lobby.Lobby;

public class SpawnCommand extends Executor {

	public SpawnCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		if (args.length == 0) {
			Lobby.getInstance().getManagerHandler().getUserManager().sendToSpawn(player);
			player.sendMessage(Lang.success("-nTeleporting..."));
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
