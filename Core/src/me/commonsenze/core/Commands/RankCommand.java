package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.Getter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Abstracts.Menu;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Managers.ManagerHandler;
import me.commonsenze.core.Managers.impl.LogManager.LogType;
import me.commonsenze.core.Menus.RankListMenu;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Objects.Rank;
import me.commonsenze.core.Util.CC;
import net.md_5.bungee.api.ChatColor;

public class RankCommand extends Executor {

	@Getter private ManagerHandler managerHandler;

	public RankCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
		this.managerHandler = Core.getInstance().getManagerHandler();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (sender instanceof Player) {
				Profile profile = getManagerHandler().getProfileManager().getProfile((Player)sender);
				sender.sendMessage(Lang.success("-nYour current rank is -e"+(profile.hasRank() ? profile.getRank().getName() : "None") + "-n."));
				sender.sendMessage(Lang.success("-n/-erank help -nto view all possible commands."));
			}
			return true;
		}
		if (!sender.hasPermission(getPermission())) return false;
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("list")) {
				if (sender instanceof Player) {
					Profile profile = getManagerHandler().getProfileManager().getProfile((Player)sender);
					Menu menu = new RankListMenu(profile.getPlayer());
					menu.create().open();
				}
				return true;
			}
		}
		if (args.length == 2) {
			Rank rank = getManagerHandler().getRankManager().getRank(args[0]);
			if (args[1].equalsIgnoreCase("create")) {
				if (!sender.hasPermission(getPermission("commands.modify")))return false;
				if (sender instanceof Player) {
					Profile profile = getManagerHandler().getProfileManager().getProfile((Player)sender);
					if (!profile.hasRank()&&!sender.isOp()) {
						sender.sendMessage(Lang.fail("-nYou cannot modify a rank you're under."));
						return true;
					}
				}
				if (rank != null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis already a valid rank."));
					return true;
				}
				rank = getManagerHandler().getRankManager().createRank(args[0]);
				sender.sendMessage(Lang.success("-nCreated rank -e"+rank.getName()+" -nwith a ranking of -e"+rank.getRanking()+"-n."));
				return true;
			}
			if (args[1].equalsIgnoreCase("delete")) {
				if (!sender.hasPermission(getPermission("commands.modify")))return false;
				if (sender instanceof Player) {
					Profile profile = getManagerHandler().getProfileManager().getProfile((Player)sender);
					if ((!profile.hasRank()||!profile.getRank().hasAuthorityOver(rank))&&!sender.isOp()) {
						sender.sendMessage(Lang.fail("-nYou cannot modify a rank you're under."));
						return true;
					}
				}
				if (rank == null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
					return true;
				}
				getManagerHandler().getRankManager().deleteRank(rank.getUUID());
				sender.sendMessage(Lang.success("-nDeleted rank -e"+rank.getName()+"-n."));
				return true;
			}
			if (args[1].equalsIgnoreCase("prefix")) {
				if (rank == null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
					return true;
				}
				sender.sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s prefix: [" + CC.translate(rank.getPrefix()) + CC.AQUA + "]");
				return true;
			}
			if (args[1].equalsIgnoreCase("tabcolor")) {
				if (rank == null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
					return true;
				}
				sender.sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s tab color: [" + CC.translate(rank.getTabColor())+ CC.getColorName(ChatColor.getByChar(rank.getTabColor().charAt(1))) + CC.AQUA + "]");
				return true;
			}
			if (args[1].equalsIgnoreCase("ranking")) {
				if (rank == null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
					return true;
				}
				sender.sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s ranking: [" + rank.getRanking() + CC.AQUA + "]");
				return true;
			}
			if (args[1].equalsIgnoreCase("default")) {
				if (rank == null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
					return true;
				}
				sender.sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s default status: [" + (rank.isDefault() ? CC.GREEN : CC.RED) + rank.isDefault() + CC.AQUA + "]");
				return true;
			}
			if (args[1].equalsIgnoreCase("permissions")) {
				if (rank == null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
					return true;
				}

				sender.sendMessage(CC.WHITE + rank.getName() + "'s Permissions");
				for (String string : rank.getPermissions()) {
					sender.sendMessage(CC.WHITE+"- "+ CC.AQUA +string);
				}
				sender.sendMessage(" ");
				return true;
			}
		}
		if (args.length == 3) {
			Rank rank = getManagerHandler().getRankManager().getRank(args[0]);
			if (!sender.hasPermission(getPermission("commands.modify")))return false;
			if (sender instanceof Player) {
				Profile profile = getManagerHandler().getProfileManager().getProfile((Player)sender);
				if (rank == null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
					return true;
				}
				if ((!profile.hasRank()||!profile.getRank().hasAuthorityOver(rank))&&!sender.isOp()) {
					sender.sendMessage(Lang.fail("-nYou cannot modify a rank you're under."));
					return true;
				}
			}
			if (args[1].equalsIgnoreCase("add")) {
				if (rank == null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
					return true;
				}
				if (rank.hasPermission(args[2])) {
					sender.sendMessage(Lang.fail("-e"+rank.getName()+" -nalready owns permission node -e"+args[2]+"-n."));
					return true;
				}
				rank.addPermission(args[2]);
				sender.sendMessage(CC.AQUA + args[2] + " was added to " +CC.WHITE+ rank.getName() +CC.AQUA + "'s permission nodes.");
				return true;
			}
			if (args[1].equalsIgnoreCase("remove")) {
				if (rank == null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
					return true;
				}
				if (!rank.hasPermission(args[2])) {
					sender.sendMessage(Lang.fail("-e"+rank.getName()+" -ndoesn't own the permission node -e"+args[2]+"-n."));
					return true;
				}
				rank.removePermission(args[2]);
				sender.sendMessage(CC.AQUA + args[2] + " was removed from " +CC.WHITE+ rank.getName() +CC.AQUA + "'s permission nodes.");
				return true;
			}
			if (args[1].equalsIgnoreCase("set")) {
				if (rank == null) {
					sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
					return true;
				}
				Profile profile = getManagerHandler().getProfileManager().getProfile(Bukkit.getPlayer(args[2]));
				if (profile == null) {
					sender.sendMessage(Lang.fail("-e"+args[2] + " -nis not online at the moment."));
					return true;
				}
				getManagerHandler().getLogManager().log(LogType.RANK, profile.getUniqueId(), sender.getName() + " set "+profile.getName() + "'s rank to "+rank.getName() + " (UUID: "+rank.getUUID()+")");
				profile.setRank(rank);
				sender.sendMessage(CC.WHITE + profile.getName() +CC.AQUA + " was set to the " +CC.WHITE+ rank.getName() +CC.AQUA + " rank.");
				return true;
			}
		}
		if (args.length == 4) {
			if (args[2].equalsIgnoreCase("set")) {
				Rank rank = getManagerHandler().getRankManager().getRank(args[0]);
				if (!sender.hasPermission(getPermission("commands.modify")))return false;
				if (sender instanceof Player) {
					Profile profile = getManagerHandler().getProfileManager().getProfile((Player)sender);
					if ((!profile.hasRank()||!profile.getRank().hasAuthorityOver(rank))&&!sender.isOp()) {
						sender.sendMessage(Lang.fail("-nYou cannot modify a rank you're under."));
						return true;
					}
				}
				if (args[1].equalsIgnoreCase("prefix")) {
					if (rank == null) {
						sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
						return true;
					}

					if (args[3].equalsIgnoreCase("\"\"")) args[3] = "";

					rank.setPrefix(args[3]);
					sender.sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s prefix: [" + CC.translate(rank.getPrefix()) + CC.AQUA + "]");
					return true;
				}
				if (args[1].equalsIgnoreCase("tabcolor")) {
					if (rank == null) {
						sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
						return true;
					}
					if (!CC.isChatColor(args[3])) {
						sender.sendMessage(Lang.fail("-e"+args[3] + " -nis not a valid chat color."));
						return true;
					}
					rank.setTabColor(args[3]);
					sender.sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s tab color: [" + CC.translate(rank.getTabColor())+ CC.getColorName(ChatColor.getByChar(rank.getTabColor().charAt(1))) + CC.AQUA + "]");
					return true;
				}
				if (args[1].equalsIgnoreCase("ranking")) {
					if (rank == null) {
						sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
						return true;
					}

					if (!NumberUtils.isNumber(args[3])) {
						sender.sendMessage(Lang.fail("-e"+args[3] + " -nis not a valid number."));
						return true;
					}

					rank.setRanking(Integer.parseInt(args[3]));
					sender.sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s ranking: [" + rank.getRanking() + CC.AQUA + "]");
					return true;
				}
				if (args[1].equalsIgnoreCase("default")) {
					if (rank == null) {
						sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
						return true;
					}
					if (Boolean.parseBoolean(args[3])) {
						getManagerHandler().getRankManager().setDefaultRank(rank);
					} else rank.setDefault(false);
					sender.sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s default status: [" + (rank.isDefault() ? CC.GREEN : CC.RED) + rank.isDefault() + CC.AQUA + "]");
					return true;
				}
			}
		}
		if (args.length >= 4) {
			if (args[2].equalsIgnoreCase("set")) {
				Rank rank = getManagerHandler().getRankManager().getRank(args[0]);
				if (args[1].equalsIgnoreCase("prefix")&&args[3].startsWith("\"")&&args[args.length-1].endsWith("\"")) {
					if (!sender.hasPermission(getPermission("commands.modify")))return false;
					if (sender instanceof Player) {
						Profile profile = getManagerHandler().getProfileManager().getProfile((Player)sender);
						if ((!profile.hasRank()||!profile.getRank().hasAuthorityOver(rank))&&!sender.isOp()) {
							sender.sendMessage(Lang.fail("-nYou cannot modify a rank you're under."));
							return true;
						}
					}
					if (rank == null) {
						sender.sendMessage(Lang.fail("-e"+args[0] + " -nis not a valid rank."));
						return true;
					}

					args[3] = args[3].substring(1);
					String prefix = "";

					for (int i = 3; i < args.length; i++) {
						prefix += args[i] + " ";
					}

					prefix = prefix.trim().substring(0, prefix.trim().length() - 1);

					rank.setPrefix(prefix);
					sender.sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s prefix: [" + CC.translate(rank.getPrefix()) + CC.AQUA + "]");
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		ArrayList<String> help = new ArrayList<>();

		help.add(ServerColor.PRIMARY + "/"+getName()+" - "+ServerColor.SECONDARY+"Check your current rank.");

		if (player.hasPermission(getPermission())) {
			help.add(ServerColor.PRIMARY + "/"+getName()+" list - "+ServerColor.SECONDARY+"List ranks.");

			help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> prefix - "+ServerColor.SECONDARY+"Check the prefix for <rank>.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> tabcolor - "+ServerColor.SECONDARY+"Check the tabcolor for <rank>.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> ranking - "+ServerColor.SECONDARY+"Check the ranking for <rank>.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> default - "+ServerColor.SECONDARY+"Check to see if <rank> is the default rank.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> permissions - "+ServerColor.SECONDARY+"Check the permissions for <rank>.");

			if (player.hasPermission(getPermission("commands.modify"))) {
				help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> set <user> - "+ServerColor.SECONDARY+"Set <user>'s rank to <rank>.");

				help.add(ServerColor.PRIMARY + "/"+getName()+" <name> create - "+ServerColor.SECONDARY+"Create <rank>.");
				help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> delete - "+ServerColor.SECONDARY+"Delete <rank>.");

				help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> prefix set <prefix...> - "+ServerColor.SECONDARY+"Set the prefix for <rank>. (if prefix has spaces surround with quotation marks)");
				help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> tabcolor set <tabcolor> - "+ServerColor.SECONDARY+"Set the tabcolor for <rank>.");
				help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> ranking set <ranking> - "+ServerColor.SECONDARY+"Set the ranking for <rank>.");
				help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> default set <default> - "+ServerColor.SECONDARY+"Set current rank to the default.");

				help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> add <permission> - "+ServerColor.SECONDARY+"Add a permission node to <rank>.");
				help.add(ServerColor.PRIMARY + "/"+getName()+" <rank> remove <permission> - "+ServerColor.SECONDARY+"Remove a permission node from <rank>.");
			}
		}
		return help;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> tab = new ArrayList<>();

		if (args.length == 1) {
			if (sender.hasPermission(getPermission())) {
				for (Rank rank : getManagerHandler().getRankManager().getRanks()) {
					if (isTabResults(rank.getName(), args[0]))tab.add(rank.getName());
				}
			}
		}
		if (args.length == 2) {
			if (sender.hasPermission(getPermission())) {
				if (getManagerHandler().getRankManager().getRanks().stream().filter(rank -> rank.getName().equalsIgnoreCase(args[0])).count() != 0) {
					for (String string : Arrays.asList("set", "prefix","tabcolor","ranking","default","permissions","add", "remove")) {
						if (isTabResults(string, args[1]))tab.add(string);
					}
				}
			}
		}

		if (args.length == 3) {
			if (args[1].equalsIgnoreCase("set")) {
				if (sender.hasPermission(getPermission("commands.modify"))) {
					Core.getInstance().getManagerHandler().getProfileManager().getProfileSet()
					.stream().filter(player -> isTabResults(player.getName(), args[2])).forEach(player -> tab.add(player.getName()));
				}
			}
		}

		Collections.sort(tab);
		return tab;
	}
}
