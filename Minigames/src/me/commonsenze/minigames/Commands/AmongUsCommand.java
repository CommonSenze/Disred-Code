package me.commonsenze.minigames.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.gson.reflect.TypeToken;

import me.commonsenze.core.Lang;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Objects.Cuboid;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.AmongUs.AmongUs;
import me.commonsenze.minigames.Games.AmongUs.Objects.Sabatoge;
import me.commonsenze.minigames.Games.AmongUs.Objects.Task;

public class AmongUsCommand extends Executor {

	private Map<UUID, Location> teleporters;

	public AmongUsCommand(String command, String description, String... aliases) {
		super(command, description, aliases);
		this.teleporters = new HashMap<>();
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
			if (args[0].equalsIgnoreCase("emergencybutton")) {
				if (args[1].equalsIgnoreCase("set")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
						return true;
					}
					Editor editor = AmongUs.getEditor();
					editor.getConfig().set("emergency-meeting-button", Minigames.getGson().toJson(profile.getPlayer().getLocation()));
					editor.saveConfig();
					profile.getPlayer().sendMessage(Lang.success("-nSet emergency meeting button for Among Us."));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("location")) {
				if (args[1].equalsIgnoreCase("lobby")) {
					Location lobby = (Location) Minigames.getGson().fromJson(AmongUs.getEditor().getConfig().getString("lobby-location"), new TypeToken<Location>() {}.getType());

					if (lobby == null) {
						sender.sendMessage(Lang.fail("-nSorry, there is currently no lobby spawn for Among Us."));
						return true;
					}
					profile.getPlayer().teleport(lobby);
					profile.getPlayer().sendMessage(Lang.success("-nTeleporting to lobby for Among Us."));
					return true;
				}
				if (args[1].equalsIgnoreCase("game")) {
					if ((Location[]) Minigames.getGson().fromJson(AmongUs.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType()) == null) {
						sender.sendMessage(Lang.fail("-nSorry, there is currently no game spawns spawn for Among Us."));
						return true;
					}

					Location game = ((Location[]) Minigames.getGson().fromJson(AmongUs.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType()))[0];
					profile.getPlayer().teleport(game);
					profile.getPlayer().sendMessage(Lang.success("-nTeleporting to game for Among Us."));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("vent")) {
				if (args[1].equalsIgnoreCase("add")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
						return true;
					}
					Cuboid cuboid = profile.getSelection();
					if (cuboid == null) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must make a world edit selection to add a vent."));
						return true;
					}

					Minigames.getInstance().getManagerHandler().getVentManager().create(cuboid);
					profile.getPlayer().sendMessage(Lang.success("-nCreated vent for Among Us."));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("teleporter")) {
				if (args[1].equalsIgnoreCase("create")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
						return true;
					}

					if (teleporters.containsKey(profile.getUniqueId())) {
						sender.sendMessage(Lang.fail("-nYou are already creating a teleporter. If you wish to finish\nthe teleporter please do '/amongus teleporter finish'"));
						return true;
					}

					if (Minigames.getInstance().getManagerHandler().getVentManager().getVents().stream()
							.filter(vent -> vent.getBounds().contains(profile.getPlayer().getLocation())).count() == 0) {
						sender.sendMessage(Lang.fail("-nYou must be in a vent to create a teleporter."));
						return true;
					}

					teleporters.put(profile.getUniqueId(), profile.getPlayer().getLocation());
					profile.getPlayer().sendMessage(Lang.success("-nCreated first location for the teleporter.\nTo set the second please do '/amongus teleporter finish'"));
					return true;
				}
				if (args[1].equalsIgnoreCase("finish")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
						return true;
					}

					if (!teleporters.containsKey(profile.getUniqueId())) {
						sender.sendMessage(Lang.fail("-nYou don't have a teleporter created to finish. If you wish to create\nthe teleporter please do '/amongus teleporter create'"));
						return true;
					}

					if (Minigames.getInstance().getManagerHandler().getVentManager().getVents().stream()
							.filter(vent -> vent.getBounds().contains(profile.getPlayer().getLocation())).count() == 0) {
						sender.sendMessage(Lang.fail("-nYou must be in a vent to create a teleporter."));
						return true;
					}

					Minigames.getInstance().getManagerHandler().getTeleporterManager().add(teleporters.get(profile.getUniqueId()), profile.getPlayer().getLocation());
					profile.getPlayer().sendMessage(Lang.success("-nCreated new teleporter in Among Us."));
					teleporters.remove(profile.getUniqueId());
					return true;
				}
			}
		}
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("location")) {
				if (args[2].equalsIgnoreCase("set")) {
					if (args[1].equalsIgnoreCase("lobby")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
							return true;
						}
						Editor editor = AmongUs.getEditor();
						editor.getConfig().set("lobby-location", Minigames.getGson().toJson(profile.getPlayer().getLocation()));
						editor.saveConfig();
						profile.getPlayer().sendMessage(Lang.success("-nSet lobby location for Among Us."));
						return true;
					}
					if (args[1].equalsIgnoreCase("bed")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
							return true;
						}
						Editor editor = AmongUs.getEditor();
						editor.getConfig().set("bed-location", Minigames.getGson().toJson(profile.getPlayer().getLocation()));
						editor.saveConfig();
						profile.getPlayer().sendMessage(Lang.success("-nSet bed location for Among Us."));
						return true;
					}
				}
				if (args[2].equalsIgnoreCase("add")) {
					if (args[1].equalsIgnoreCase("game")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
							return true;
						}
						Location[] locations = new Location[10];

						if (AmongUs.getEditor().getConfig().contains("locations"))
							locations = (Location[]) Minigames.getGson().fromJson(AmongUs.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType());
						
						int slot = 0;
						
						for (; slot<locations.length; slot++)
							if (locations[slot] == null)break;

						
						locations[slot] = profile.getPlayer().getLocation();

						Editor editor = AmongUs.getEditor();
						
						editor.getConfig().set("locations", Minigames.getGson().toJson(locations));
						editor.saveConfig();
						
						profile.getPlayer().sendMessage(Lang.success("-nAdded location number -e"+(slot+1)+"-n/10 to game spawn for Among Us."));
						return true;
					}
				}
			}
			if (args[0].equalsIgnoreCase("task")) {
				if (args[1].equalsIgnoreCase("set")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
						return true;
					}
					Task.Type type = Task.Type.valueOf(args[2].toUpperCase());

					if (type == null) {
						sender.sendMessage(Lang.fail("-e"+args[2].toUpperCase()+" -nis not a valid Task type.\nTo see available task, please do '/amongus task set ' and press tab."));
						return true;
					}

					Cuboid cuboid = profile.getSelection();
					if (cuboid == null) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must make a world edit selection to add a task."));
						return true;
					}

					Task.setTaskRegion(type, cuboid);
					profile.getPlayer().sendMessage(Lang.success("-nSet area for the task type -e"+type.name()+" -nfor Among Us."));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("sabatoge")) {
				if (args[1].equalsIgnoreCase("set")) {
					if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
						return true;
					}
					Sabatoge.Type type = Sabatoge.Type.valueOf(args[2].toUpperCase());

					if (type == null) {
						sender.sendMessage(Lang.fail("-e"+args[2].toUpperCase()+" -nis not a valid Sabatoge type.\nTo see available task, please do '/amongus sabatoge set ' and press tab."));
						return true;
					}

					Cuboid cuboid = profile.getSelection();
					if (cuboid == null) {
						profile.getPlayer().sendMessage(Lang.fail("-nYou must make a world edit selection to add a sabatoge."));
						return true;
					}

					Sabatoge.setTaskRegion(type, cuboid);
					profile.getPlayer().sendMessage(Lang.success("-nSet area for the sabatoge type -e"+type.name()+" -nfor Among Us."));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("location")) {
				if (args[1].equalsIgnoreCase("game")) {
					if ((Location[]) Minigames.getGson().fromJson(AmongUs.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType()) == null) {
						sender.sendMessage(Lang.fail("-nSorry, there is currently no game spawns spawn for Among Us."));
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

					Location game = ((Location[]) Minigames.getGson().fromJson(AmongUs.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType()))[slot-1];
					profile.getPlayer().teleport(game);
					profile.getPlayer().sendMessage(Lang.success("-nTeleporting to game spawn number "+slot+" for Among Us."));
					return true;
				}
			}
		}
		if (args.length == 4) {
			if (args[0].equalsIgnoreCase("task")) {
				if (args[1].equalsIgnoreCase("shootmob")) {
					if (args[2].equalsIgnoreCase("set")) {
						if (args[3].equalsIgnoreCase("mobarea")) {
							if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
								profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
								return true;
							}
							Cuboid cuboid = profile.getSelection();
							if (cuboid == null) {
								profile.getPlayer().sendMessage(Lang.fail("-nYou must make a world edit selection to add a task."));
								return true;
							}

							Editor editor = Task.getEditor();
							editor.getConfig().set(Task.Type.SHOOT_MOBS.name() +".mobarea", Minigames.getGson().toJson(cuboid));
							editor.saveConfig();
							profile.getPlayer().sendMessage(Lang.success("-nSet mob area for the shoot mob task in Among Us."));
							return true;
						}
					}
				}
				if (args[1].equalsIgnoreCase("throwtrash")) {
					if (args[2].equalsIgnoreCase("set")) {
						if (args[3].equalsIgnoreCase("trashshoot")) {
							if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
								profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
								return true;
							}
							Cuboid cuboid = profile.getSelection();
							if (cuboid == null) {
								profile.getPlayer().sendMessage(Lang.fail("-nYou must make a world edit selection to add a task."));
								return true;
							}

							Editor editor = Task.getEditor();
							editor.getConfig().set(Task.Type.THROW_TRASH.name() +".trash-shoot", Minigames.getGson().toJson(cuboid));
							editor.saveConfig();
							profile.getPlayer().sendMessage(Lang.success("-nSet trash area for the throw trash task in Among Us."));
							return true;
						}
					}
				}
			}
			if (args[0].equalsIgnoreCase("location")) {
				if (args[2].equalsIgnoreCase("set")) {
					if (args[1].equalsIgnoreCase("game")) {
						if (Minigames.getInstance().getManagerHandler().getGameManager().isRegistered(AmongUs.getName().replaceAll(" ", ""))) {
							profile.getPlayer().sendMessage(Lang.fail("-nYou must first '/game amongus unregister' to modify this gamemode."));
							return true;
						}
						if (!AmongUs.getEditor().getConfig().contains("locations")) {
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

						Location[] locations = (Location[]) Minigames.getGson().fromJson(AmongUs.getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType());

						locations[slot-1] = profile.getPlayer().getLocation();

						Editor editor = AmongUs.getEditor();
						editor.getConfig().set("locations", Minigames.getGson().toJson(locations));
						editor.saveConfig();
						profile.getPlayer().sendMessage(Lang.success("-nAdded location number -e"+slot+"-n/10 to game spawn for Among Us."));
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
			help.add(ServerColor.PRIMARY + "/"+getName()+" location game [#] - "+ServerColor.SECONDARY+"Teleport to game spawn.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" location lobby set - "+ServerColor.SECONDARY+"Set spawn of lobby spawn.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" location bed set - "+ServerColor.SECONDARY+"Set the location you're standing on to be the location \nwhere the head of the bed is.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" location game set <#> - "+ServerColor.SECONDARY+"Set spawn for game spawn number <#>.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" location game add - "+ServerColor.SECONDARY+"Add spawn for game spawn.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" task set <task> - "+ServerColor.SECONDARY+"With a cuboid set (WorldEdit), set the area that will belong for <task>.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" task shootmob set mobarea - "+ServerColor.SECONDARY+"With a cuboid set (WorldEdit), set the area that\nwill belong for the mobs in the ShootMob task.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" task throwtrash set trashshoot - "+ServerColor.SECONDARY+"With a cuboid set (WorldEdit), set the area that\nwill belong for the trash shoot in the ThrowTrash task.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" sabatoge set <sabatoge> - "+ServerColor.SECONDARY+"With a cuboid set (WorldEdit), set the area that will belong for <sabatoge>.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" vent add - "+ServerColor.SECONDARY+"With a cuboid set (WorldEdit), set the area that will belong for <task>.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" teleporter create - "+ServerColor.SECONDARY+"Create the first out of 2 locations for the teleporter."
					+ "\nYou will be able to come back to set the second in"
					+ "\n'/"+getName()+" teleporter finish' command.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" teleporter finish - "+ServerColor.SECONDARY+"Finish the 2 part step for creating the teleporter.");
			help.add(ServerColor.PRIMARY + "/"+getName()+" emergencybutton set - "+ServerColor.SECONDARY+"Set location of the emergency meeting button.");
		}

		return help;
	}

}
