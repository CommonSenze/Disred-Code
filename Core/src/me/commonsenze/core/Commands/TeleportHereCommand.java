package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Objects.Profile;

public class TeleportHereCommand extends Executor {

	public TeleportHereCommand(String command, String description, String...aliases) {
		super(command, description,aliases);
		setPlayersOnly(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(((Player)sender).getUniqueId());

		if (args.length == 1) {
			if (!profile.getPlayer().hasPermission(this.getPermission()+".others")) {
				profile.getPlayer().sendMessage(Lang.NO_PERMISSION);
				return true;
			}
			
			Profile target = Core.getInstance().getManagerHandler().getProfileManager().getProfile(Bukkit.getPlayer(args[0]));

			if (target == null) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+args[0] + " -nisn't online at the moment."));
				return true;
			}

			target.getPlayer().teleport(profile.getPlayer());
			profile.getPlayer().sendMessage(Lang.success("-nTeleporting -e"+target.getPlayer().getName()+" -nto you."));
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				(player.hasPermission(getPermission()) ? ServerColor.PRIMARY + "/"+getName()+" <player> - "+ServerColor.SECONDARY+"Teleport to <player>.": ""),
				(player.hasPermission(getPermission()) ? ServerColor.PRIMARY + "/"+getName()+" <fromPlayer> <toPlayer> - "+ServerColor.SECONDARY+"Teleport <fromPlayer> to <toPlayer>.": "")
				));
	}
}
