package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Objects.Profile;

public class PingCommand extends Executor {

	public PingCommand(String command, String description, String...aliases) {
		super(command, description, aliases);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			int ping = ((CraftPlayer)sender).getHandle().ping;
			sender.sendMessage(Lang.success("-nYour ping: -e"+ping));
			return true;
		}
		if (args.length == 1) {
			Profile target = Core.getInstance().getManagerHandler().getProfileManager().getProfile(Bukkit.getPlayer(args[0]));
			if (target == null) {
				sender.sendMessage(Lang.fail("-nSorry, "+args[0]+" is not online right now."));
				return true;
			}

			int ping = ((CraftPlayer)target.getPlayer()).getHandle().ping;
			sender.sendMessage(Lang.success("-n"+target.getPlayer().getName()+"'s ping: -e"+ping));
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				ServerColor.PRIMARY + "/"+getName()+" - "+ServerColor.SECONDARY+"Check your ping.",
				ServerColor.PRIMARY + "/"+getName()+" <player> - "+ServerColor.SECONDARY+"Check <player>'s ping."
				));
	}

}
