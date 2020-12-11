package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Objects.Profile;

public class GamemodeSCommand extends Executor {

	public GamemodeSCommand(String command, String description, String...aliases) {
		super(command, description, aliases);
		setPlayersOnly(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(((Player)sender).getUniqueId());

		if (profile == null)return true;
		
		if (args.length == 0) {
			if (!profile.getPlayer().hasPermission("commands.gamemode")) {
				profile.getPlayer().sendMessage(Lang.NO_PERMISSION);
				return true;
			}
			
			GameMode gamemode = GameMode.SURVIVAL;
			
			profile.getPlayer().setGameMode(gamemode);
			profile.getPlayer().sendMessage(Lang.success("-nYou set your gamemode to -e"+gamemode.name()+"-n."));
			return true;
		}
		
		if (args.length == 1) {
			if (!profile.getPlayer().hasPermission("commands.gamemode.others")) {
				profile.getPlayer().sendMessage(Lang.NO_PERMISSION);
				return true;
			}
			
			Profile target = Core.getInstance().getManagerHandler().getProfileManager().getProfile(Bukkit.getPlayer(args[0]));

			if (target == null) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+args[0] + " -nisn't online at the moment."));
				return true;
			}
			
			GameMode gamemode = GameMode.SURVIVAL;
			
			target.getPlayer().setGameMode(gamemode);
			target.getPlayer().sendMessage(Lang.success("-n"+profile.getPlayer().getName()+" set your gamemode to -e"+gamemode.name()+"-n."));
			profile.getPlayer().sendMessage(Lang.success("-nYou set -e"+target.getPlayer().getName()+"-n's gamemode to -e"+gamemode.name()+"-n."));
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				(player.hasPermission(getPermission()) ? ServerColor.PRIMARY + "/"+getName()+" - "+ServerColor.SECONDARY+"Set you gamemode to survival.": ""),
				(player.hasPermission(getPermission()) ? ServerColor.PRIMARY + "/"+getName()+" <player> - "+ServerColor.SECONDARY+"Set you gamemode to survival for <player>.": "")
				));
	}
}
