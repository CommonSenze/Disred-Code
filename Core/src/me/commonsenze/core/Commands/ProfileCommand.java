package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Managers.impl.ProfileManager;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Util.CC;

public class ProfileCommand extends Executor {

	public ProfileCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			sender.sendMessage(Lang.NO_PERMISSION);
			return true;
		}
		if (args.length == 3) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
			UUID uuid = Core.getInstance().getCache().getUUID(player.getName());
			ProfileManager profileManager = Core.getInstance().getManagerHandler().getProfileManager();
			if (args[1].equalsIgnoreCase("add")) {
				if (!player.isOnline()) {
					if (!player.hasPlayedBefore()) {
						sender.sendMessage(Lang.fail("-e"+args[0] + " -nhas never played on the server before."));
						return true;
					}
					if (profileManager.hasPermission(uuid, args[2])) {
						sender.sendMessage(Lang.fail("-e"+player.getName()+" -nalready owns permission node -e"+args[2]+"-n."));
						return true;
					}

					profileManager.addOfflinePermission(uuid, args[2]);
				} else {
					Profile profile = profileManager.getProfile(player.getPlayer());
					if (profile.hasPermission(args[2])) {
						sender.sendMessage(Lang.fail("-e"+player.getName()+" -nalready owns permission node -e"+args[2]+"-n."));
						return true;
					}

					Core.getInstance().getManagerHandler().getProfileManager().getProfile(player.getPlayer()).addPermission(args[2]);
				}

				sender.sendMessage(CC.AQUA + args[2] + " was added to " +CC.WHITE+ player.getName() +CC.AQUA + "'s permission nodes.");
				return true;
			}
			if (args[1].equalsIgnoreCase("remove")) {
				if (!player.isOnline()) {
					if (!player.hasPlayedBefore()) {
						sender.sendMessage(Lang.fail("-e"+args[0] + " -nhas never played on the server before."));
						return true;
					}
					if (!profileManager.hasPermission(uuid, args[2])) {
						sender.sendMessage(Lang.fail("-e"+player.getName()+" -ndoesn't own the permission node -e"+args[2]+"-n."));
						return true;
					}
					profileManager.removeOfflinePermission(uuid, args[2]);
				} else {
					Profile profile = profileManager.getProfile(player.getPlayer());
					if (!profile.hasPermission(args[2])) {
						sender.sendMessage(Lang.fail("-e"+player.getName()+" -ndoesn't own the permission node -e"+args[2]+"-n."));
						return true;
					}

					Core.getInstance().getManagerHandler().getProfileManager().getProfile(player.getPlayer()).removePermission(args[2]);
				}
				sender.sendMessage(CC.AQUA + args[2] + " was removed from " +CC.WHITE+ player.getName() +CC.AQUA + "'s permission nodes.");
				return true;
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		ArrayList<String> help = new ArrayList<>();

		if (player.hasPermission(getPermission())) {
			help.add(ServerColor.PRIMARY + "/"+getName()+" <player> add <permission> - "+ServerColor.SECONDARY+"Add a permission node to <player>.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" <player> remove <permission> - "+ServerColor.SECONDARY+"Remove a permission node from <player>.");

		}

		return help;
	}
}
