package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.commonsenze.core.Core;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Util.CC;
import me.commonsenze.core.Util.DateUtil;
import me.commonsenze.core.Util.DateUtil.MaxConverter;

public class PlayTimeCommand extends Executor {

	public PlayTimeCommand(String command, String description, String...aliases) {
		super("playtime", "Get your play time on the server.", "pt");
		setPlayersOnly(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
        Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(((Player)sender).getUniqueId());
        
		if (args.length == 0) {
			int[] times = DateUtil.convertElapsedTime(profile.getTimeJoined(), System.currentTimeMillis(), MaxConverter.DAYS);
			
			String message = CC.YELLOW + profile.getName() + " has been playing for "+CC.GOLD + (times[3] != 0 ? times[3] + " day" + (times[3] != 1 ? "s " : " ") : "") +
					(times[2] != 0 ? times[2] + " hour" + (times[2] != 1 ? "s " : " ") : "") +
					(times[1] != 0 ? times[1] + " minute" + (times[1] != 1 ? "s " : " ") : "") +
					(times[0] != 0 ? times[0] + " second" + (times[0] != 1 ? "s " : " ") : "") +CC.YELLOW+ ".";
			
			profile.getPlayer().sendMessage(message);
			return true;
		}
		return false;
	}
	
	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				ServerColor.PRIMARY + "/"+getName()+"- "+ServerColor.SECONDARY+getDescription(),
				ServerColor.PRIMARY + "/"+getName()+" <player> - "+ServerColor.SECONDARY+"Get the playtime for <player>."
				));
	}
}
