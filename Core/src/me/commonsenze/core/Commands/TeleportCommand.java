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

public class TeleportCommand extends Executor {

	public TeleportCommand(String command, String description, String...aliases) {
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
			
			Profile target = Core.getInstance().getManagerHandler().getProfileManager().getProfile(Bukkit.getPlayer(args[0]));

			if (target == null) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+args[0] + " -nisn't online at the moment."));
				return true;
			}
			
			profile.getPlayer().teleport(target.getPlayer());
			profile.getPlayer().sendMessage(Lang.success("-nTeleporting to -e"+target.getPlayer().getName()+"-n."));
			return true;
		}
		
		if (args.length == 2) {
			if (!profile.getPlayer().hasPermission(this.getPermission()+".others")) {
				profile.getPlayer().sendMessage(Lang.NO_PERMISSION);
				return true;
			}
			
			Profile from = Core.getInstance().getManagerHandler().getProfileManager().getProfile(Bukkit.getPlayer(args[0]));

			if (from == null) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+args[0] + " -nisn't online at the moment."));
				return true;
			}
			
			Profile to = Core.getInstance().getManagerHandler().getProfileManager().getProfile(Bukkit.getPlayer(args[1]));

			if (to == null) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+args[1] + " -nisn't online at the moment."));
				return true;
			}

			from.getPlayer().teleport(to.getPlayer());
			from.getPlayer().sendMessage(Lang.success("-e"+profile.getPlayer().getName()+" -nteleported you to -e"+to.getPlayer().getName()+"-n."));
			to.getPlayer().sendMessage(Lang.success("-e"+profile.getPlayer().getName()+" -nteleported -e"+from.getPlayer().getName()+" -nto you."));
			profile.getPlayer().sendMessage(Lang.success("-nTeleporting -e"+from.getPlayer().getName()+" -nto -e"+to.getPlayer().getName()+"-n."));
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
