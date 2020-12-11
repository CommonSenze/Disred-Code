package me.commonsenze.project;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.commonsenze.project.Abstracts.Executor;
import me.commonsenze.project.Commands.TriangleCommand;
import me.commonsenze.project.Managers.ManagerHandler;

public class Core extends JavaPlugin {

	@Getter private static Core instance;
	
	@Getter private ManagerHandler managerHandler;
	private List<Executor> executors;
	
	public void onEnable() {
		Core.instance = this;
		
		this.managerHandler = new ManagerHandler(this);
		registerCommands();
	}
	
	public void onDisable() {
		unregisterCommands();
		
		Core.instance = null;
	}
	
	public void registerCommands() {
		executors = new ArrayList<>(Arrays.asList(
				new TriangleCommand("triangle", "Modify a triangle.")
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
}
