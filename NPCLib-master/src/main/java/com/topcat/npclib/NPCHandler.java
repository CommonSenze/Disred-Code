package com.topcat.npclib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import com.mojang.authlib.properties.Property;
import com.topcat.npclib.entity.HumanNPC;
import com.topcat.npclib.entity.NPC;
import com.topcat.npclib.nms.BServer;
import com.topcat.npclib.nms.BWorld;
import com.topcat.npclib.nms.NPCEntity;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;

@Getter 
public class NPCHandler {

	private final Map<World, BWorld> bworlds = new HashMap<>();
	private final HashMap<String, NPC> npcs = new HashMap<>();
	private final HashMap<String, NPCEntity> entities = new HashMap<>();
	private final BServer server;
	private NPCManager nPCManager;

	public NPCHandler(NPCManager npcManager, BServer server) {
		this.server = server;
		this.nPCManager = npcManager;
	}

	public BWorld getBWorld(World world) {
		BWorld bworld = bworlds.get(world);
		if (bworld != null) {
			return bworld;
		}
		bworld = new BWorld(world);
		bworlds.put(world, bworld);
		return bworld;
	}

	public NPC spawnHumanNPC(String name, Location l) {
		int i = 0;
		String id = name;
		while (npcs.containsKey(id)) {
			id = name + i;
			i++;
		}
		return spawnHumanNPC(name, l, id);
	}

	public NPC spawnHumanNPC(String name, Location l, String id) {
		if (npcs.containsKey(id)) {
			server.getLogger().log(Level.WARNING, "NPC with that id already exists, existing NPC returned");
			return npcs.get(id);
		}

		if (name.length() > 16) { // Check and nag if name is too long, spawn
			// NPC anyway with shortened name.
			final String tmp = name.substring(0, 16);
			server.getLogger().log(Level.WARNING, "NPCs can't have names longer than 16 characters,");
			server.getLogger().log(Level.WARNING, name + " has been shortened to " + tmp);
			name = tmp;
		}

		final BWorld world = getBWorld(l.getWorld());

		final NPCEntity npcEntity = new NPCEntity(this, world, name, new PlayerInteractManager(world.getWorldServer()));
		npcEntity.setPositionRotation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());

		final HumanNPC npc = new HumanNPC(npcEntity);

		npcs.put(id, npc);
		entities.put(id, npcEntity);
		
		System.out.println("Spawning npc with id "+id + " Name: "+npc.getName());

		Bukkit.getOnlinePlayers().stream().filter(player -> npc.getBukkitEntity().getLocation().distanceSquared(player.getLocation()) <= Math.pow(npc.getVisibility(), 2)).forEach(player -> {
			npcEntity.spawn(player);
		});

		return npc;
	}

	public void setSkin(NPCEntity npc, String username) {
		List<UUID> uuids = new ArrayList<>(npc.getSpawnedFor());
		uuids.forEach(uuid -> npc.despawn(Bukkit.getPlayer(uuid)));

		try {
			HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://api.ashcon.app/mojang/v2/user/%s", username)).openConnection();
			if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
				ArrayList<String> lines = new ArrayList<>();
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				reader.lines().forEach(lines::add);

				String string = "";

				for (String str : lines) {
					string += str;
				}
				String reply = string;
				int indexOfValue = reply.indexOf("\"value\": \"");
				int indexOfSignature = reply.indexOf("\"signature\": \"");
				String skin = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
				String signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));

				npc.getProfile().getProperties().put("textures", new Property("textures", skin, signature));
			}

			else {
				Bukkit.getConsoleSender().sendMessage("Connection could not be opened when fetching player skin (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		npc.setSkinName(username);
		
		uuids.forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			npc.spawn(player);
		});
	}

	public void despawn(NPC npc) {
		System.out.println("Despawning npc with name "+npc.getEntity().getName());
		despawn(getId(npc));
	}

	public void despawn(String id) {
		final NPC npc = npcs.get(id);
		if (npc != null) {
			Bukkit.getOnlinePlayers().forEach(getNPCEntity(id)::despawn);
			npcs.remove(id);
			entities.remove(id);
		}
	}

	public void despawnAll() {
		for (final NPC npc : npcs.values()) {
			if (npc != null) {
				Bukkit.getOnlinePlayers().forEach(getNPCEntity(getId(npc))::despawn);
			}
		}
		entities.clear();
		npcs.clear();
	}

	public boolean containsNPC(String name) {
		return npcs.containsKey(name);
	}

	public NPC getNPC(String id) {
		return npcs.get(id);
	}

	public NPCEntity getNPCEntity(String id) {
		return entities.get(id);
	}

	public String getId(NPCEntity entity) {
		return entities.entrySet().stream().filter(entry -> entry.getValue().equals(entity)).findFirst().orElse(null).getKey();
	}

	public String getId(NPC entity) {
		return npcs.entrySet().stream().filter(entry -> entry.getValue().equals(entity)).findFirst().orElse(null).getKey();
	}

	public String getId(String name) {
		return npcs.entrySet().stream().filter(entry -> entry.getValue() instanceof HumanNPC&&((HumanNPC) entry.getValue()).getName().equalsIgnoreCase(name))
				.findFirst().orElse(null).getKey();
	}

	public String getId(org.bukkit.entity.Entity e) {
		return npcs.entrySet().stream().filter(entry -> entry.getValue().getBukkitEntity().getEntityId() == ((HumanEntity) e).getEntityId())
				.findFirst().orElse(null).getKey();
	}

	public boolean isNPC(org.bukkit.entity.Entity e) {
		return ((CraftEntity) e).getHandle() instanceof NPCEntity;
	}
}
