package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;

public class DiscordCommand extends Executor {

	public DiscordCommand(String command, String description, String...aliases) {
		super(command, description, aliases);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(Lang.success("-nDiscord Link: -ehttps://discord.gg/H6k8epg"));
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				ServerColor.PRIMARY + "/"+getName()+" - "+ServerColor.SECONDARY+"Get the discord link."
				));
	}
}
