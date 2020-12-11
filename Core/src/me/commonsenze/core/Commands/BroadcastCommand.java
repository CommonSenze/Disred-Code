package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import net.md_5.bungee.api.ChatColor;

public class BroadcastCommand extends Executor {

	public BroadcastCommand(String command, String description, String...args) {
		super(command, description, args);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length >= 1) {
			String message = "";

			for (int i = 0; i < args.length; i++) {
				message += args[i] + (i + 1 == args.length ? "" : " ");
			}

			String prefix = Core.getInstance().getConfig("messages").getMessage("broadcast.prefix");
			
			Bukkit.broadcastMessage(" ");
			Bukkit.broadcastMessage(prefix + ChatColor.WHITE + message);
			Bukkit.broadcastMessage(" ");
			return true;
		}
		return false;
	}
	
	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				ServerColor.PRIMARY + "/"+getName()+" <message...> - "+ServerColor.SECONDARY+getDescription()
				));
	}
}
