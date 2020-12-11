package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Util.CC;

public class MessageCommand extends Executor {

	public MessageCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
		setPlayersOnly(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile((Player) sender);
		if (args.length >= 2) {
			Profile other = Core.getInstance().getManagerHandler().getProfileManager().getProfile(Bukkit.getPlayer(args[0]));
			if (other == null) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+args[0] +" -nis currently not online at the moment."));
				return true;
			}

			if (other == profile) {
				profile.getPlayer().sendMessage(Lang.fail("-nYou cannot message yourself."));
				return true;
			}

			if (!other.isMessageable()) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+other.getName() +" -nhas disabled their private messaging."));
				return true;
			}

			String message = "";

			for (int i = 1; i < args.length; i++) {
				message += args[i] + " ";
			}

			message = message.trim();

			profile.getPlayer().sendMessage(CC.DARKGRAY + "(" + CC.BLUE + "To " + CC.GRAY + other.getName()
			+ CC.DARKGRAY + ") " + CC.GRAY + message);

			other.getPlayer().sendMessage(CC.DARKGRAY + "(" + CC.GREEN + "From " + CC.GRAY + profile.getName()
			+ CC.DARKGRAY + ") " + CC.GRAY + message);

			profile.setMessager(other.getUniqueId());
			other.setMessager(profile.getUniqueId());
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		ArrayList<String> help = new ArrayList<>();

		help.add(ServerColor.PRIMARY + "/"+getName()+" <player> <message...> - "+ServerColor.SECONDARY+"Send <message...> to <player>.");

		return help;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> tab = new ArrayList<>();

		if (args.length == 1) {
			Core.getInstance().getManagerHandler().getProfileManager().getProfileSet()
			.stream().filter(player -> isTabResults(player.getName(), args[0])&&player.isMessageable()&&(!(sender instanceof Player)||((Player)sender).canSee(player.getPlayer()))).forEach(player -> tab.add(player.getName()));
		}

		Collections.sort(tab);
		return tab;
	}
}
