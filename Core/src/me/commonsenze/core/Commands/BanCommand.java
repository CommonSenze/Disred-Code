package me.commonsenze.core.Commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;

public class BanCommand extends Executor {

	public BanCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		ArrayList<String> help = new ArrayList<>();

		if (player.hasPermission(getPermission())) {
			help.add(ServerColor.PRIMARY + "/"+getName()+" <player> [reason...] - "+ServerColor.SECONDARY+"Ban <player>.");
		}
		
		if (player.hasPermission(getPermission()+".silent")) {
			help.add(ServerColor.PRIMARY + "/"+getName()+" <player> -s [reason...] - "+ServerColor.SECONDARY+"Ban <player> silently.");
		}

		return help;
	}
}
