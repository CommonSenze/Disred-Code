package me.commonsenze.minigames.Managers.impl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;

import lombok.Getter;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.AmongUs.Objects.Teleporter;
import me.commonsenze.minigames.Managers.Manager;
import me.commonsenze.minigames.Managers.ManagerHandler;

public class TeleporterManager extends Manager {

	@Getter private Set<Teleporter> teleporters;
	
	public TeleporterManager(ManagerHandler managerHandler) {
		super(managerHandler);
		this.teleporters = new HashSet<>();
		load();
	}
	
	public void load() {
		File folder = new File(Minigames.getInstance().getDataFolder() +File.separator +Teleporter.TELEPORTER_FOLDER.substring(0, Teleporter.TELEPORTER_FOLDER.length()-1));
		
		if (!folder.exists())folder.mkdirs();
		
		File[] files = folder.listFiles();
		
		for (File file : files) {
			teleporters.add(new Teleporter(Minigames.getInstance().createConfig(file)));
		}
	}
	
	public void add(Location first, Location second) {
		teleporters.add(new Teleporter(first, second));
	}
	
	public void remove(Teleporter teleporter) {
		teleporters.remove(teleporter);
	}
}
