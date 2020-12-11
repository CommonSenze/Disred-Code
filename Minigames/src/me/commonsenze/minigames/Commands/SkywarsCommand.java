package me.commonsenze.minigames.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import me.commonsenze.core.Lang;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Objects.Cuboid;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.Skywars.Skywars;
import me.commonsenze.minigames.Games.Skywars.Objects.Island;

public class SkywarsCommand extends Executor {

	private Map<UUID, Island> islands;
	
	public SkywarsCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
		setPlayersOnly(true);
		this.islands = new HashMap<>();
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
					Location lobby = (Location) Minigames.getGson().fromJson(Skywars.getEditor().getConfig().getString("lobby-location"), new TypeToken<Location>() {}.getType());

					if (lobby == null) {
						sender.sendMessage(Lang.fail("-nSorry, there is currently no lobby spawn for Skywars."));
						return true;
					}
					profile.getPlayer().teleport(lobby);
					profile.getPlayer().sendMessage(Lang.success("-nTeleporting to lobby for Skywars."));
					return true;
				}
				if (args[1].equalsIgnoreCase("game")) {
					if ((Location[]) Minigames.getGson().fromJson(Skywars.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType()) == null) {
						sender.sendMessage(Lang.fail("-nSorry, there is currently no game spawns spawn for Skywars."));
						return true;
					}

					Location game = ((Location[]) Minigames.getGson().fromJson(Skywars.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType()))[0];
					profile.getPlayer().teleport(game);
					profile.getPlayer().sendMessage(Lang.success("-nTeleporting to game for Skywars."));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("world")) {
				if (args[1].equalsIgnoreCase("set")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game Skywars unregister' to modify this gamemode."));
						return true;
					}
					if (Skywars.getEditor().getConfig().contains("world")) {
						sender.sendMessage(Lang.fail("-nThe world for Skywars is already created."));
						return true;
					}
					
					Editor editor = Skywars.getEditor();
					editor.getConfig().set("world", profile.getPlayer().getWorld().getName());
					editor.saveConfig();
					profile.getPlayer().sendMessage(Lang.success("-nTeleporting to lobby for Skywars."));
					return true;
				}
				if (args[1].equalsIgnoreCase("save")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game Skywars unregister' to modify this gamemode."));
						return true;
					}
					if (!Skywars.getEditor().getConfig().contains("world")) {
						sender.sendMessage(Lang.fail("-nThere is no world set for Skywars."));
						return true;
					}
					
					Skywars.saveWorld();
					profile.getPlayer().sendMessage(Lang.success("-nSaving world for Skywars."));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("island")) {
				if (args[1].equalsIgnoreCase("setup")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game Skywars unregister' to modify this gamemode."));
						return true;
					}
					if (islands.containsKey(profile.getUniqueId())) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou are already creating an island."));
						return true;
					}
					
					islands.put(profile.getUniqueId(), new Island());
					profile.getPlayer().sendMessage(Lang.success("-nYou've started the set up of a new Island."));
					return true;
				}
			}
		}
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("location")) {
				if (args[2].equalsIgnoreCase("set")) {
					if (args[1].equalsIgnoreCase("lobby")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game Skywars unregister' to modify this gamemode."));
							return true;
						}
						Editor editor = Skywars.getEditor();
						editor.getConfig().set("lobby-location", Minigames.getGson().toJson(profile.getPlayer().getLocation()));
						editor.saveConfig();
						profile.getPlayer().sendMessage(Lang.success("-nSet lobby location for Skywars."));
						return true;
					}
				}
				if (args[2].equalsIgnoreCase("add")) {
					if (args[1].equalsIgnoreCase("game")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
							return true;
						}
						Location[] locations = new Location[10];

						if (Skywars.getEditor().getConfig().contains("locations"))
							locations = (Location[]) Minigames.getGson().fromJson(Skywars.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType());
						
						int slot = 0;
						
						for (; slot<locations.length; slot++)
							if (locations[slot] == null)break;

						
						locations[slot] = profile.getPlayer().getLocation();

						Editor editor = Skywars.getEditor();
						
						editor.getConfig().set("locations", Minigames.getGson().toJson(locations));
						editor.saveConfig();
						
						profile.getPlayer().sendMessage(Lang.success("-nAdded location number -e"+(slot+1)+"-n/10 to game spawn for Skywars."));
						return true;
					}
				}
			}
			if (args[0].equalsIgnoreCase("location")) {
				if (args[1].equalsIgnoreCase("game")) {
					if ((Location[]) Minigames.getGson().fromJson(Skywars.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType()) == null) {
						sender.sendMessage(Lang.fail("-nSorry, there is currently no game spawns spawn for Skywars."));
						return true;
					}

					if (!NumberUtils.isNumber(args[2])) {
						sender.sendMessage(Lang.fail("-e"+args[2]+" -nis not a valid number."));
						return true;
					}

					int slot = Integer.parseInt(args[2]);

					if (slot < 1||slot>10) {
						sender.sendMessage(Lang.fail("-nYou must pick a number from 1-10."));
						return true;
					}

					Location game = ((Location[]) Minigames.getGson().fromJson(Skywars.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType()))[slot-1];
					profile.getPlayer().teleport(game);
					profile.getPlayer().sendMessage(Lang.success("-nTeleporting to game spawn number "+slot+" for Skywars."));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("island")) {
				if (args[1].equalsIgnoreCase("set")) {
					if (args[2].equalsIgnoreCase("spawn")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game Skywars unregister' to modify this gamemode."));
							return true;
						}
						if (!islands.containsKey(profile.getUniqueId())) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou are not currently creating an island."));
							return true;
						}

						Island island = islands.get(profile.getUniqueId());
						
						island.setGameSpawn(profile.getPlayer().getLocation());
						profile.getPlayer().sendMessage(Lang.success("-nSetting the game spawn for this island."));
						if (island.isCompleted()) {
							profile.getPlayer().sendMessage(Lang.success("-nThe island is now completed and will now be saved to the island list."));
							Set<Island> saved = (Set<Island>) Sets.newHashSet(((Set<?>)Minigames.getGson().fromJson(Skywars.getEditor().getConfig().getString("islands"), new TypeToken<Set<?>>() {}.getType())).toArray(new Island[0]));
							saved.add(island);
							Editor editor = Skywars.getEditor();
							editor.getConfig().set("islands", Minigames.getGson().toJson(saved));
							editor.saveConfig();
						}
						return true;
					}
					if (args[2].equalsIgnoreCase("bounds")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game Skywars unregister' to modify this gamemode."));
							return true;
						}
						if (!islands.containsKey(profile.getUniqueId())) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou are not currently creating an island."));
							return true;
						}

						Island island = islands.get(profile.getUniqueId());
						
						Cuboid cuboid = profile.getSelection();
						
						if (cuboid == null) {
							profile.getPlayer().sendMessage(Lang.fail("-nPlease select the region you would like to be dedicated to the island to scan for chest."));
							return true;
						}
						
						island.setBounds(cuboid);
						if (!island.scanForChest()) {
							island.setBounds(null);
							profile.getPlayer().sendMessage(Lang.fail("-nThe region you selected does not have the required amount of chest: 3."));
							return true;
						}
						profile.getPlayer().sendMessage(Lang.success("-nSetting the boundaries for this island."));
						if (island.isCompleted()) {
							profile.getPlayer().sendMessage(Lang.success("-nThe island is now completed and will now be saved to the island list."));
							Set<Island> saved = (Set<Island>) Sets.newHashSet(((Set<?>)Minigames.getGson().fromJson(Skywars.getEditor().getConfig().getString("islands"), new TypeToken<Set<?>>() {}.getType())).toArray(new Island[0]));
							saved.add(island);
							Editor editor = Skywars.getEditor();
							editor.getConfig().set("islands", Minigames.getGson().toJson(saved));
							editor.saveConfig();
						}
						return true;
					}
					if (args[2].equalsIgnoreCase("cage")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game Skywars unregister' to modify this gamemode."));
							return true;
						}
						if (!islands.containsKey(profile.getUniqueId())) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou are not currently creating an island."));
							return true;
						}

						Island island = islands.get(profile.getUniqueId());
						
						Cuboid cuboid = profile.getSelection();
						
						if (cuboid == null) {
							profile.getPlayer().sendMessage(Lang.fail("-nPlease select the region you would like to be dedicated to the cage."));
							return true;
						}
						
						island.setCage(cuboid);
						profile.getPlayer().sendMessage(Lang.success("-nSetting the cage for this island."));
						if (island.isCompleted()) {
							profile.getPlayer().sendMessage(Lang.success("-nThe island is now completed and will now be saved to the island list."));
							Set<Island> saved = (Set<Island>) Sets.newHashSet(((Set<?>)Minigames.getGson().fromJson(Skywars.getEditor().getConfig().getString("islands"), new TypeToken<Set<?>>() {}.getType())).toArray(new Island[0]));
							saved.add(island);
							Editor editor = Skywars.getEditor();
							editor.getConfig().set("islands", Minigames.getGson().toJson(saved));
							editor.saveConfig();
						}
						return true;
					}
				}
			}
		}
		if (args.length == 4) {
			if (args[0].equalsIgnoreCase("location")) {
				if (args[2].equalsIgnoreCase("set")) {
					if (args[1].equalsIgnoreCase("game")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(Skywars.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game Skywars unregister' to modify this gamemode."));
							return true;
						}
						if (!Skywars.getEditor().getConfig().contains("locations")) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou currently don't have any game locations created."));
							return true;
						}

						if (!NumberUtils.isNumber(args[3])) {
							sender.sendMessage(Lang.fail("-e"+args[3]+" -nis not a valid number."));
							return true;
						}

						int slot = Integer.parseInt(args[3]);

						if (slot < 1||slot>10) {
							sender.sendMessage(Lang.fail("-nYou must pick a number from 1-10."));
							return true;
						}

						Location[] locations = (Location[]) Minigames.getGson().fromJson(Skywars.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType());

						locations[slot-1] = profile.getPlayer().getLocation();

						Editor editor = Skywars.getEditor();
						editor.getConfig().set("locations", Minigames.getGson().toJson(locations));
						editor.saveConfig();
						profile.getPlayer().sendMessage(Lang.success("-nAdded location number -e"+slot+"-n/10 to game spawn for Skywars."));
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
			help.add(ServerColor.PRIMARY + "/"+getName()+" location lobby set - "+ServerColor.SECONDARY+"Set spawn of lobby spawn.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" island setup - "+ServerColor.SECONDARY+"Start the set up process for creating an island.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" island set bounds - "+ServerColor.SECONDARY+"Set the cuboid boundaries for the island.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" island set cage - "+ServerColor.SECONDARY+"Set the player cage for the island.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" island set spawn - "+ServerColor.SECONDARY+"Set the spawn for the island.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" world set - "+ServerColor.SECONDARY+"Set the world that will hold the map of Skywars.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" world save - "+ServerColor.SECONDARY+"Save the Skywars, if any changes were made.");
		}

		return help;
	}

}
