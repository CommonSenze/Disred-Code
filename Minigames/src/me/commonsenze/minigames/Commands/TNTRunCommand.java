package me.commonsenze.minigames.Commands;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.gson.reflect.TypeToken;

import me.commonsenze.core.Lang;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.Skywars.Skywars;
import me.commonsenze.minigames.Games.TNTRun.TNTRun;

public class TNTRunCommand extends Executor {

	public TNTRunCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
		setPlayersOnly(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission(getPermission())) {
			sender.sendMessage(Lang.NO_PERMISSION);
			return true;
		}
		Profile profile = CoreAPI.getInstance().getProfile((Player)sender);

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("location")) {
				if (args[1].equalsIgnoreCase("lobby")) {
					Location lobby = (Location) Minigames.getGson().fromJson(TNTRun.getEditor().getConfig().getString("lobby-location"), new TypeToken<Location>() {}.getType());

					if (lobby == null) {
						sender.sendMessage(Lang.fail("-nSorry, there is currently no lobby spawn for TNT Run."));
						return true;
					}
					profile.getPlayer().teleport(lobby);
					profile.getPlayer().sendMessage(Lang.success("-nTeleporting to lobby for TNT Run."));
					return true;
				}
				if (args[1].equalsIgnoreCase("game")) {
					Location game = ((Location) Minigames.getGson().fromJson(TNTRun.getEditor().getConfig().getString("game-location"), new TypeToken<Location>() {}.getType()));
					
					if (game == null) {
						sender.sendMessage(Lang.fail("-nSorry, there is currently no game spawn for TNT Run."));
						return true;
					}
					
					profile.getPlayer().teleport(game);
					profile.getPlayer().sendMessage(Lang.success("-nTeleporting to game for TNT Run."));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("world")) {
				if (args[1].equalsIgnoreCase("set")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game Skywars unregister' to modify this gamemode."));
						return true;
					}
					if (TNTRun.getEditor().getConfig().contains("world")) {
						sender.sendMessage(Lang.fail("-nThe world for TNT Run is already created."));
						return true;
					}
					
					Editor editor = TNTRun.getEditor();
					editor.getConfig().set("world", profile.getPlayer().getWorld().getName());
					editor.saveConfig();
					profile.getPlayer().sendMessage(Lang.success("-nTeleporting to lobby for TNT Run."));
					return true;
				}
				if (args[1].equalsIgnoreCase("save")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game Skywars unregister' to modify this gamemode."));
						return true;
					}
					if (!TNTRun.getEditor().getConfig().contains("world")) {
						sender.sendMessage(Lang.fail("-nThere is no world set for TNT Run."));
						return true;
					}
					
					TNTRun.saveWorld();
					profile.getPlayer().sendMessage(Lang.success("-nSaving world for TNT Run."));
					return true;
				}
			}
		}
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("location")) {
				if (args[2].equalsIgnoreCase("set")) {
					if (args[1].equalsIgnoreCase("lobby")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(TNTRun.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game TNTRun unregister' to modify this gamemode."));
							return true;
						}
						Editor editor = TNTRun.getEditor();
						editor.getConfig().set("lobby-location", Minigames.getGson().toJson(profile.getPlayer().getLocation()));
						editor.saveConfig();
						profile.getPlayer().sendMessage(Lang.success("-nSet lobby location for TNT Run."));
						return true;
					}
					if (args[1].equalsIgnoreCase("game")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(TNTRun.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game TNTRun unregister' to modify this gamemode."));
							return true;
						}
						Editor editor = TNTRun.getEditor();
						editor.getConfig().set("game-location", Minigames.getGson().toJson(profile.getPlayer().getLocation()));
						editor.saveConfig();
						profile.getPlayer().sendMessage(Lang.success("-nSet game location for TNT Run."));
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		ArrayList<String> help = new ArrayList<>();

		if (player.hasPermission(getPermission())) {
			help.add(ServerColor.PRIMARY + "/"+getName()+" location lobby - "+ServerColor.SECONDARY+"Teleport to lobby spawn.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" location game - "+ServerColor.SECONDARY+"Teleport to game spawn.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" location lobby set - "+ServerColor.SECONDARY+"Set spawn of lobby spawn.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" location game set - "+ServerColor.SECONDARY+"Set spawn of game spawn.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" world set - "+ServerColor.SECONDARY+"Set the world that will hold the map of TNTRun.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" world save - "+ServerColor.SECONDARY+"Save the TNTRun, if any changes were made.");
		}

		return help;
	}

}
