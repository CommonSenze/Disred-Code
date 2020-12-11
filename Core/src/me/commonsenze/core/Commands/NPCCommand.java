package me.commonsenze.core.Commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Objects.Profile;

public class NPCCommand extends Executor {

	public NPCCommand(String command, String description, String...aliases) {
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
			
			
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		ArrayList<String> help = new ArrayList<>();

		help.add(ServerColor.PRIMARY + "/"+getName()+" list - "+ServerColor.SECONDARY+"List of npcs showing name and ID.");
		help.add(ServerColor.PRIMARY + "/"+getName()+" create <name> - "+ServerColor.SECONDARY+"Create npc named <name>.");
		help.add(ServerColor.PRIMARY + "/"+getName()+" delete <ID> - "+ServerColor.SECONDARY+"Delete npc with the id <ID>.");
		help.add(ServerColor.PRIMARY + "/"+getName()+" <ID> setskin <player> - "+ServerColor.SECONDARY+"Set skin of npc to <player>.");
		help.add(ServerColor.PRIMARY + "/"+getName()+" <ID> action server <server> - "+ServerColor.SECONDARY+"When the npc is right clicked, they will teleport that player to <server>.");
		

		return help;
	}
}
