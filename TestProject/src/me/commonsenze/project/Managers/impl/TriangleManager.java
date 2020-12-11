package me.commonsenze.project.Managers.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import lombok.Getter;
import me.commonsenze.project.Lang;
import me.commonsenze.project.Managers.Manager;
import me.commonsenze.project.Managers.ManagerHandler;
import me.commonsenze.project.Objects.Triangle;
import me.commonsenze.project.Objects.Vertice;

public class TriangleManager extends Manager {

	@Getter private Map<UUID, Triangle> triangles;

	public TriangleManager(ManagerHandler managerHandler) {
		super(managerHandler);
		registerAsListener();
		this.triangles = new HashMap<>();
	}

	public void put(UUID uuid, Triangle triangle) {
		triangles.put(uuid, triangle);
	}
	
	public Triangle getTriangle(UUID uuid) {
		return triangles.get(uuid);
	}
	
	public boolean hasTriangle(UUID uuid) {
		return triangles.containsKey(uuid);
	}

	public void remove(UUID uuid) {
		if (!hasTriangle(uuid))return;
		Bukkit.getScheduler().cancelTask(triangles.get(uuid).getTriangleTask().getTaskId());
		triangles.remove(uuid);
	}
	
	public void unload() {
		for (UUID uuid : new HashMap<>(triangles).keySet()) {
			remove(uuid);
		}
		triangles.clear();
	}
	
	@EventHandler
	public void interect(PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_AIR) return;
		if (!hasTriangle(event.getPlayer().getUniqueId()))return;
		
		Triangle triangle = getTriangle(event.getPlayer().getUniqueId());
		
		if (triangle.hasVerticies()) return;
		
		Vector vector = event.getPlayer().getEyeLocation().add(event.getPlayer().getLocation().getDirection()).toVector();
		
		triangle.addVertice(new Vertice(vector));
		event.getPlayer().sendMessage(Lang.success("-nAdded point -e"+vector.getX()+", "+vector.getY()+", "+vector.getZ()+" -nto your triangle."));
		
		if (triangle.hasVerticies())
			event.getPlayer().sendMessage(Lang.success("-nYour triangle is ready to be drawn. Run the command '-e/triangle draw-n' when you're ready."));
	}
}
