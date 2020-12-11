package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;

public class ClearChatCommand extends Executor {

	public ClearChatCommand(String command, String description, String...aliases) {
		super(command, description, aliases);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (!sender.hasPermission(this.getPermission())) {
				sender.sendMessage(Lang.NO_PERMISSION);
				return true;
			}
			
			for (int i = 0; i < 100; i++)
				Bukkit.broadcastMessage(" ");
			Bukkit.broadcastMessage(Lang.success(sender.getName()+ChatColor.GRAY + " has cleared chat"));
			sender.sendMessage(Lang.success("-nYou cleared chat."));
			return true;
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("-s")) {
				if (!sender.hasPermission(this.getPermission()+".silent")) {
					sender.sendMessage(Lang.NO_PERMISSION);
					return true;
				}
				
				for (int i = 0; i < 100; i++)
					Bukkit.broadcastMessage(" ");
				sender.sendMessage(Lang.success("-nYou cleared chat."));
				return true;
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				(player.hasPermission(getPermission()) ? ServerColor.PRIMARY + "/"+getName()+" - "+ServerColor.SECONDARY+"Clear chat.": ""),
				(player.hasPermission(getPermission()+".silent") ? ServerColor.PRIMARY + "/"+getName()+" -s - "+ServerColor.SECONDARY+"Clear chat silently.": "")
				));
	}
}
