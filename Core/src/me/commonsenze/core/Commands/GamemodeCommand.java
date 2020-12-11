package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Objects.Profile;

public class GamemodeCommand extends Executor {

	public GamemodeCommand(String command, String description, String...aliases) {
		super(command, description, aliases);
		setPlayersOnly(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(((Player)sender).getUniqueId());

		if (args.length == 1) {
			if (!profile.getPlayer().hasPermission(this.getPermission())) {
				profile.getPlayer().sendMessage(Lang.NO_PERMISSION);
				return true;
			}
			
			GameMode gamemode = parseGamemode(args[0]);
			
			if (gamemode == null) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+args[0]+" -nisn't a valid gamemode."));
				return true;
			}
			
			profile.getPlayer().setGameMode(gamemode);
			profile.getPlayer().sendMessage(Lang.success("-nYou set your gamemode to -e"+gamemode.name()+"-n."));
			return true;
		}
		
		if (args.length == 2) {
			if (!profile.getPlayer().hasPermission(this.getPermission()+".others")) {
				profile.getPlayer().sendMessage(Lang.NO_PERMISSION);
				return true;
			}
			
			Profile target = Core.getInstance().getManagerHandler().getProfileManager().getProfile(Bukkit.getPlayer(args[1]));

			if (target == null) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+args[1] + " -nisn't online at the moment."));
				return true;
			}
			
			GameMode gamemode = parseGamemode(args[0]);
			
			if (gamemode == null) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+args[0]+" -nisn't a valid gamemode."));
				return true;
			}
			
			target.getPlayer().setGameMode(gamemode);
			target.getPlayer().sendMessage(Lang.success("-n"+profile.getPlayer().getName()+" set your gamemode to -e"+gamemode.name()+"-n."));
			profile.getPlayer().sendMessage(Lang.success("-nYou set -e"+target.getPlayer().getName()+"-n's gamemode to -e"+gamemode.name()+"-n."));
			return true;
		}
		return false;
	}
	
	public GameMode parseGamemode(String string) {
		try {
			int i = Integer.parseInt(string);
			
			switch (i) {
			case 0: return GameMode.SURVIVAL;
			case 1: return GameMode.CREATIVE;
			case 2: return GameMode.ADVENTURE;
			case 3: return GameMode.SPECTATOR;
			default:
				break;
			}
		} catch (NumberFormatException e) {}
		if (GameMode.SURVIVAL.name().toLowerCase().startsWith(string.toLowerCase()))return GameMode.SURVIVAL;
		if (GameMode.ADVENTURE.name().toLowerCase().startsWith(string.toLowerCase()))return GameMode.ADVENTURE;
		if (GameMode.CREATIVE.name().toLowerCase().startsWith(string.toLowerCase()))return GameMode.CREATIVE;
		if (GameMode.SPECTATOR.name().toLowerCase().startsWith(string.toLowerCase()))return GameMode.SPECTATOR;
		return null;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> tab = new ArrayList<>();
		if (args.length == 1) {
			for (GameMode gamemode : GameMode.values()) {
				if (isTabResults(gamemode.name(), args[1]))tab.add(gamemode.name());
			}
		}
		return tab;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				(player.hasPermission(getPermission()) ? ServerColor.PRIMARY + "/"+getName()+" <gamemode> - "+ServerColor.SECONDARY+"Set you gamemode to <gamemode>.": ""),
				(player.hasPermission(getPermission()) ? ServerColor.PRIMARY + "/"+getName()+" <gamemode> <player> - "+ServerColor.SECONDARY+"Set you gamemode to <gamemode> for <player>.": "")
				));
	}
}
