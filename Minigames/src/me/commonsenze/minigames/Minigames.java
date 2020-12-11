package me.commonsenze.minigames;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import lombok.AccessLevel;
import lombok.Getter;
import me.commonsenze.core.Core;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Interfaces.ScoreboardAdapter;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Commands.AmongUsCommand;
import me.commonsenze.minigames.Commands.GameCommand;
import me.commonsenze.minigames.Commands.JoinCommand;
import me.commonsenze.minigames.Commands.SpawnCommand;
import me.commonsenze.minigames.Commands.TNTRunCommand;
import me.commonsenze.minigames.Games.Skywars.Objects.Island;
import me.commonsenze.minigames.Games.Skywars.Objects.IslandAdapter;
import me.commonsenze.minigames.Managers.ManagerHandler;
import me.commonsenze.minigames.Scoreboards.AmongUsScoreboard;
import me.commonsenze.minigames.Scoreboards.GameLobbyScoreboard;
import me.commonsenze.minigames.Scoreboards.LobbyScoreboard;
import me.commonsenze.minigames.Scoreboards.TNTRunScoreboard;

@Getter
public class Minigames extends JavaPlugin {

	@Getter private static Minigames instance;
	private ManagerHandler managerHandler;
	
	@Getter(value = AccessLevel.NONE)private ArrayList<Executor> executors = new ArrayList<>();
	@Getter(value = AccessLevel.NONE)private ArrayList<Config> configs = new ArrayList<>();
	@Getter(value = AccessLevel.NONE)private List<ScoreboardAdapter> adapters = new ArrayList<>();
	
	@Getter private static Gson gson = Core.getGson().newBuilder()
			.registerTypeAdapter(Island.class, new IslandAdapter())
			.create();
	
	public void onEnable() {
		Minigames.instance = this;
		this.managerHandler = new ManagerHandler(this);
		
		registerCommands();
		registerScoreboards();
	}
	
	public void onDisable() {
		unregisterScoreboards();
		unregisterCommands();
		this.managerHandler.save();
		Minigames.instance = null;
	}
	
	public void registerScoreboards() {
		(adapters = Arrays.asList(
				new AmongUsScoreboard(),
				new LobbyScoreboard(),
				new GameLobbyScoreboard(),
				new TNTRunScoreboard()
				)).forEach(scoreboard -> CoreAPI.getInstance().registerScoreboard(scoreboard));
	}
	
	public void unregisterScoreboards() {
		adapters.forEach(scoreboard -> CoreAPI.getInstance().unregisterScoreboard(scoreboard));
	}
	
	public void registerCommands() {
		executors = new ArrayList<>(Arrays.asList(
				new AmongUsCommand("amongus", "Commands to modify Among Us game."),
				new GameCommand("game", "View possible commands for games."),
				new SpawnCommand("spawn", "Teleport to spawn."),
				new JoinCommand("join", "Join a game."),
				new TNTRunCommand("tntrun", "Commands to modify TNT Run game.", "trun")
				));
		executors.forEach(command -> ((CraftServer) getInstance().getServer()).getCommandMap().register("minigames", command));
	}

	public void unregisterCommands() {
		for (Executor executor : executors) {
			unregister(executor);
		}
		executors.clear();
	}

	public void unregister(BukkitCommand cmd) {
		try {
			Object result = getPrivateField(getServer().getPluginManager(), "commandMap");
			SimpleCommandMap commandMap = (SimpleCommandMap) result;
			Object map = getPrivateField(commandMap, "knownCommands");
			@SuppressWarnings("unchecked")
			HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
			knownCommands.remove(cmd.getName());
			for (String alias : cmd.getAliases()){
				if(knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(cmd.getName())){
					knownCommands.remove(alias);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Object getPrivateField(Object object, String field)throws SecurityException,
	NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = object.getClass();
		Field objectField = clazz.getDeclaredField(field);
		objectField.setAccessible(true);
		Object result = objectField.get(object);
		objectField.setAccessible(false);
		return result;
	}
	
	public void reloadConfigs() {
		for (Config editor : configs) {
			editor.reloadConfig();
		}
	}

	public Editor createConfig(File file) {
		for (Config config : new ArrayList<>(configs)) {
			if (config.getName().equalsIgnoreCase(file.getPath().replaceAll(".yml", "")))return config;
		}
		return new Config(file);
	}

	public Editor getConfig(String name) {
		return getConfig("",name, instance);
	}

	public Editor getConfig(String folder, String name) {
		return getConfig(folder,name, instance);
	}

	public Editor getConfig(String name, Plugin plugin) {
		return getConfig("",name, plugin);
	}

	public Editor getConfig(String folder, String name, Plugin plugin) {
		for (Config config : new ArrayList<>(configs)) {
			if (config.getName().equalsIgnoreCase(File.separator+folder+File.separator+name))return config;
		}
		return add(new Config(folder, name, plugin));
	}

	public void removeConfig(Config config) {
		this.configs.remove(config);
	}

	private Editor add(Config config) {
		this.configs.add(config);
		return config;
	}
}

class Config implements Editor {

	private File file;
	private FileConfiguration config;

	public Config(String name, Plugin plugin) {
		this("",name, plugin);
	}

	public Config(String folder, String name, Plugin plugin) {
		if (!plugin.getDataFolder().exists())
			plugin.getDataFolder().mkdir();
		String[] folders = folder.split("/");

		String s = "";

		for (int i = 0; i < folders.length; i++) {
			File fold = new File(plugin.getDataFolder()+s, folders[i]);
			if (!fold.exists())fold.mkdir();
			s += File.separator + folders[i];
		}

		this.file = new File(plugin.getDataFolder() + s, name+".yml");

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe("Could not create "+file.getName() +" ("+s+") ["+plugin.getDataFolder() + s+"]");
			}
		}

		config = YamlConfiguration.loadConfiguration(file);
	}

	public Config(File file) {
		this.file = file;

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe("Could not create "+file.getName() +" for spawns");
			}
		}

		config = YamlConfiguration.loadConfiguration(file);
	}

	public void delete() {
		file.delete();
	}

	public String getMessage(String key) {
		return CC.translate(config.getString(key));
	}

	public List<String> getMessages(String key) {
		List<String> messages = new ArrayList<>();
		for (String message : config.getStringList(key)) {
			messages.add(CC.translate(message));
		}
		return messages;
	}

	public String getName() {
		return file.getPath().replaceAll(".yml", "");
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public File getFile() {
		return file;
	}

	public <T> T addDefault(String key, T obj) {
		config.addDefault(key, obj);
		config.options().copyDefaults(true);
		saveConfig();
		return obj;
	}

	public void saveConfig() {
		try {
			config.save(file);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe("Could not save "+file.getName());
		}
	}

	public void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(file);
	}
}