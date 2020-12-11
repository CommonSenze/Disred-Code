package me.commonsenze.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.topcat.npclib.NPCManager;

import lombok.AccessLevel;
import lombok.Getter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Commands.BroadcastCommand;
import me.commonsenze.core.Commands.BuildCommand;
import me.commonsenze.core.Commands.ClearChatCommand;
import me.commonsenze.core.Commands.ClearInventoryCommand;
import me.commonsenze.core.Commands.DiscordCommand;
import me.commonsenze.core.Commands.GamemodeCCommand;
import me.commonsenze.core.Commands.GamemodeCommand;
import me.commonsenze.core.Commands.GamemodeSCommand;
import me.commonsenze.core.Commands.HeadCommand;
import me.commonsenze.core.Commands.MessageCommand;
import me.commonsenze.core.Commands.MuteChatCommand;
import me.commonsenze.core.Commands.PingCommand;
import me.commonsenze.core.Commands.PlayTimeCommand;
import me.commonsenze.core.Commands.ProfileCommand;
import me.commonsenze.core.Commands.RankCommand;
import me.commonsenze.core.Commands.RegisterCommand;
import me.commonsenze.core.Commands.ReplyCommand;
import me.commonsenze.core.Commands.SetSpawnCommand;
import me.commonsenze.core.Commands.TeleportCommand;
import me.commonsenze.core.Commands.TeleportHereCommand;
import me.commonsenze.core.Database.MemoryCache;
import me.commonsenze.core.Interfaces.Cache;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Managers.ManagerHandler;
import me.commonsenze.core.Objects.ItemStackAdapter;
import me.commonsenze.core.Objects.LocationAdapter;
import me.commonsenze.core.Objects.LocationArrayAdapter;
import me.commonsenze.core.Objects.PotionEffectAdapter;
import me.commonsenze.core.Util.CC;

@Getter
public class Core extends JavaPlugin {

	@Getter private static Core instance;
	private ManagerHandler managerHandler;
	private Cache cache;
	private CoreAPI api;
	private NPCManager npcManager;
	
	@Getter private static Gson gson = new GsonBuilder()
	        .registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectAdapter())
	        .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
	        .registerTypeHierarchyAdapter(Location.class, new LocationAdapter())
	        .registerTypeHierarchyAdapter(Location[].class, new LocationArrayAdapter())
	        .serializeNulls()
	        .create();
	
	private Editor permissionsEditor;
	
	@Getter(value = AccessLevel.NONE)private ArrayList<Executor> executors = new ArrayList<>();
	@Getter(value = AccessLevel.NONE)private ArrayList<Config> configs = new ArrayList<>();
	
	public void onEnable() {
		Core.instance = this;
		api = new CoreAPI(this);
		npcManager = new NPCManager(this);
		
		this.permissionsEditor = getConfig("permissions");
		loadPermissionDefaults();
		
		this.cache = new MemoryCache();
		this.managerHandler = new ManagerHandler(this);
		
		registerCommands();
	}
	
	public void onDisable() {
		unregisterCommands();
		
		getManagerHandler().save();
		
		Core.instance = null;
	}
	
	private void loadPermissionDefaults() {
		FileConfiguration config = this.permissionsEditor.getConfig();
		
		config.addDefault("commands.general", "commands.%command%");
		config.addDefault("commands.other", "commands.%command%.other");
		config.addDefault("commands.modify", "commands.%command%.modify");

		config.options().copyDefaults(true);
		this.permissionsEditor.saveConfig();
	}
	
	public void registerCommands() {
		executors = new ArrayList<>(Arrays.asList(
				new RegisterCommand("register", "Link your account to our website."),
				new BroadcastCommand("broadcast", "Send a message to all servers.", "bc"),
				new BuildCommand("build", "Toggle your build."),
				new ClearChatCommand("clearchat", "Clear public chat.", "cc"),
				new ClearInventoryCommand("clearinventory", "Clear your inventory.", "ci"),
				new DiscordCommand("discord", "Get the discord link."),
				new GamemodeCCommand("gamemodec", "Set your gamemode to creative.", "gmc"),
				new GamemodeCommand("gamemode", "Set your gamemode.", "gm"),
				new GamemodeSCommand("gamemodes", "Set your gamemode to creative.", "gms"),
				new HeadCommand("head", "Get a head of any player."),
				new MuteChatCommand("mutechat", "Clear public chat.", "mc"),
				new PingCommand("ping", "Check a player's ping."),
				new PlayTimeCommand("playtime", "Get your play time on the server.", "pt"),
				new TeleportCommand("teleport", "Teleport to a player.", "tp"),
				new TeleportHereCommand("teleporthere", "Teleport a player to you.", "tphere"),
				new ReplyCommand("reply", "Reply to the player that messaged you.", "r"),
				new MessageCommand("message", "Message a player.", "msg"),
				new SetSpawnCommand("setspawn", "Set spawn for the server."),
				new ProfileCommand("profile", "Edit profile for a player."),
				new RankCommand("rank", "Edit the ranks.")
				));
		executors.forEach(command -> ((CraftServer) getInstance().getServer()).getCommandMap().register("core", command));
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
		ArrayList<Config> conf = new ArrayList<>(configs);
		for (Config config : conf) {
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
