package me.commonsenze.core.Commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;

public class MuteChatCommand extends Executor implements Listener {

	private boolean muted;

	public MuteChatCommand(String command, String description, String...aliases) {
		super(command, description, aliases);
		Bukkit.getPluginManager().registerEvents(this, Core.getInstance());
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (!sender.hasPermission(this.getPermission())) {
				sender.sendMessage(Lang.NO_PERMISSION);
				return true;
			}

			if (!muted) {
				Bukkit.broadcastMessage(Lang.success(sender.getName()+ChatColor.GRAY + " has muted chat"));
				sender.sendMessage(Lang.success("-nYou -emuted -nchat."));
			} else {
				Bukkit.broadcastMessage(Lang.success(sender.getName()+ChatColor.GRAY + " has unmuted chat"));
				sender.sendMessage(Lang.success("-nYou -eunmuted -nchat."));
			}
			muted = !muted;
			return true;
		}

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("-s")) {
				if (!sender.hasPermission(this.getPermission()+".silent")) {
					sender.sendMessage(Lang.NO_PERMISSION);
					return true;
				}

				if (!muted) {
					sender.sendMessage(Lang.success("-nYou -emuted -nchat."));
				} else {
					sender.sendMessage(Lang.success("-nYou -eunmuted -nchat."));
				}
				muted = !muted;
				return true;
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				(player.hasPermission(getPermission()) ? ServerColor.PRIMARY + "/"+getName()+" - "+ServerColor.SECONDARY+"Clear chat.": ""),
				(player.hasPermission(getPermission()+".silent") ? ServerColor.PRIMARY + "/"+getName()+" -s - "+ServerColor.SECONDARY+"Clear chat silently.": "")
				));
	}

	@EventHandler
	public void chat(AsyncPlayerChatEvent e) {
		if (muted&&e.getPlayer().hasPermission(Core.getInstance().getPermissionsEditor().getConfig().getString("chat.muted", "chat.bypass"))) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Lang.fail("-nChat is currently muted. You cannot chat for the moment."));
		}
	}
}
