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

public class BuildCommand extends Executor {

	public BuildCommand(String command, String description, String...aliases) {
		super(command, description, aliases);
		setPlayersOnly(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(((Player)sender).getUniqueId());

		if (profile == null)return true;
		
		if (args.length == 0) {
			if (!profile.getPlayer().hasPermission(this.getPermission())) {
				profile.getPlayer().sendMessage(Lang.NO_PERMISSION);
				return true;
			}
			
			profile.setBuild(!profile.canBuild());
			profile.getPlayer().sendMessage(Lang.success("-nYou -e"+(profile.canBuild() ? "enabled" : "disabled") + " -nyour building."));
			return true;
		}
		
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
			
			target.setBuild(!target.canBuild());
			target.getPlayer().sendMessage(Lang.success("-e"+profile.getPlayer().getName()+" -nhas -e"+(target.canBuild() ? "enabled" : "disabled") + " -nyour building."));
			profile.getPlayer().sendMessage(Lang.success("-nYou -e"+(target.canBuild() ? "enabled" : "disabled") + " -nbuilding for -e"+target.getPlayer().getName()+"-n."));
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				(player.hasPermission(getPermission()) ? ServerColor.PRIMARY + "/"+getName()+" - "+ServerColor.SECONDARY+"Enable/Disable your building.": ""),
				(player.hasPermission(getPermission()+".others") ? ServerColor.PRIMARY + "/"+getName()+" <player> - "+ServerColor.SECONDARY+"Enable/Disable <player>'s building.": "")
				));
	}
}
