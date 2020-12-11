package me.commonsenze.core.Commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Util.CC;

public class ReplyCommand extends Executor {

	public ReplyCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
		setPlayersOnly(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile((Player) sender);
		if (args.length >= 1) {
			if (!profile.hasMessager()) {
				profile.getPlayer().sendMessage(Lang.fail("-nYou haven't messaged anyone."));
				return true;
			}
			Profile other = Core.getInstance().getManagerHandler().getProfileManager().getProfile(profile.getMessager());
			if (other == null) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+args[0] +" -nis currently not online at the moment."));
				return true;
			}
			
			if (!other.isMessageable()) {
				profile.getPlayer().sendMessage(Lang.fail("-e"+other.getName() +" -nhas disabled their private messaging."));
				return true;
			}
			
			String message = "";

			for (int i = 0; i < args.length; i++) {
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

		help.add(ServerColor.PRIMARY + "/"+getName()+" <message...> - "+ServerColor.SECONDARY+"Send <message...> to the player you were last messaging.");

		return help;
	}
}
