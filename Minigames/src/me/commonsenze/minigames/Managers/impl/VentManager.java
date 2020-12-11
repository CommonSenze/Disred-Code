package me.commonsenze.minigames.Managers.impl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import me.commonsenze.core.Objects.Cuboid;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.AmongUs.Objects.Vent;
import me.commonsenze.minigames.Managers.Manager;
import me.commonsenze.minigames.Managers.ManagerHandler;

public class VentManager extends Manager {

	@Getter private Set<Vent> vents;
	
	public VentManager(ManagerHandler managerHandler) {
		super(managerHandler);
		this.vents = new HashSet<>();
		load();
	}
	
	public void load() {
		File folder = new File(Minigames.getInstance().getDataFolder() +File.separator +Vent.VENT_FOLDER.substring(0, Vent.VENT_FOLDER.length()-1));
		
		if (!folder.exists())folder.mkdirs();
		
		File[] files = folder.listFiles();
		
		for (File file : files) {
			vents.add(new Vent(Minigames.getInstance().createConfig(file)));
		}
	}

	public void create(Cuboid cuboid) {
		vents.add(new Vent(cuboid));
	}
	
	public void remove(Vent vent) {
		vents.remove(vent);
	}
}
