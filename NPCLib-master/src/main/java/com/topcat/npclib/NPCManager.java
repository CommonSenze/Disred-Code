package com.topcat.npclib;

import java.io.IOException;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.topcat.npclib.nms.BServer;
import com.topcat.npclib.nms.NPCEntity;
import com.topcat.npclib.nms.NPCNetworkManager;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.Entity;

/**
 * 
 * @author martin
 */
public class NPCManager implements Listener {

	private final int taskid;
	private NPCNetworkManager npcNetworkManager;
	public static JavaPlugin plugin;
	public static ProtocolManager manager;
	@Getter private NPCHandler npcHandler;

	public NPCManager(JavaPlugin plugin) {
		manager = ProtocolLibrary.getProtocolManager();

		try {
			npcNetworkManager = new NPCNetworkManager();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		NPCManager.plugin = plugin;
		npcHandler = new NPCHandler(this, BServer.getInstance());

		taskid = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				final HashSet<String> toRemove = new HashSet<>();
				for (final String i : npcHandler.getNpcs().keySet()) {
					final Entity j = npcHandler.getNpcs().get(i).getEntity();
					j.Y();
					if (j.dead) {
						toRemove.add(i);
					}
				}
				for (final String n : toRemove) {
					npcHandler.getNpcs().remove(n);
				}
			}
		}, 1L, 1L);
		
		manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.BED) {
            
            public void onPacketSending(PacketEvent event) {
                
                
            }
        });
		
		Bukkit.getServer().getPluginManager().registerEvents(new SL(), plugin);
		Bukkit.getServer().getPluginManager().registerEvents(new WL(), plugin);
	}

	public boolean isNPC(org.bukkit.entity.Entity e) {
		return ((CraftEntity) e).getHandle() instanceof NPCEntity;
	}

	//	public void rename(String id, String name) {
	//		if (name.length() > 16) { // Check and nag if name is too long, spawn
	//			// NPC anyway with shortened name.
	//			final String tmp = name.substring(0, 16);
	//			server.getLogger().log(Level.WARNING, "NPCs can't have names longer than 16 characters,");
	//			server.getLogger().log(Level.WARNING, name + " has been shortened to " + tmp);
	//			name = tmp;
	//		}
	//		final HumanNPC npc = (HumanNPC) getNPC(id);
	//		npc.setName(name);
	//		final BWorld b = getBWorld(npc.getBukkitEntity().getLocation().getWorld());
	//		final WorldServer s = b.getWorldServer();
	//		try {
	//			Method m = s.getClass().getDeclaredMethod("d", new Class[] { Entity.class });
	//			m.setAccessible(true);
	//			m.invoke(s, npc.getEntity());
	//			m = s.getClass().getDeclaredMethod("c", new Class[] { Entity.class });
	//			m.setAccessible(true);
	//			m.invoke(s, npc.getEntity());
	//		} catch (final Exception ex) {
	//			ex.printStackTrace();
	//		}
	//		s.everyoneSleeping();
	//	}

	public NPCNetworkManager getNPCNetworkManager() {
		return npcNetworkManager;
	}

	private class SL implements Listener {
		@EventHandler
		public void onPluginDisable(PluginDisableEvent event) {
			if (event.getPlugin() == plugin) {
				npcHandler.despawnAll();
				Bukkit.getServer().getScheduler().cancelTask(taskid);
			}
		}
	}

	private class WL implements Listener {
		@EventHandler
		public void move(PlayerMoveEvent event) {
			npcHandler.getEntities().entrySet().stream()
			.forEach(entry -> {
				if (entry.getValue().getBukkitEntity().getLocation().distanceSquared(event.getFrom()) <= Math.pow(getNpcHandler().getNPC(getNpcHandler().getId(entry.getValue())).getVisibility(), 2)&&!entry.getValue().isSpawnedFor(event.getPlayer().getUniqueId()))
					entry.getValue().spawn(event.getPlayer());
				else if (entry.getValue().getBukkitEntity().getLocation().distanceSquared(event.getFrom()) > Math.pow(getNpcHandler().getNPC(getNpcHandler().getId(entry.getValue())).getVisibility(), 2)&&entry.getValue().isSpawnedFor(event.getPlayer().getUniqueId()))entry.getValue().despawn(event.getPlayer());
			});
		}
		
		@EventHandler
		public void teleport(PlayerTeleportEvent event) {
			npcHandler.getEntities().entrySet().stream()
			.forEach(entry -> {
				if (entry.getValue().getBukkitEntity().getLocation().distanceSquared(event.getTo()) <= Math.pow(getNpcHandler().getNPC(getNpcHandler().getId(entry.getValue())).getVisibility(), 2)&&!entry.getValue().isSpawnedFor(event.getPlayer().getUniqueId()))
					entry.getValue().spawn(event.getPlayer());
				else if (entry.getValue().getBukkitEntity().getLocation().distanceSquared(event.getTo()) > Math.pow(getNpcHandler().getNPC(getNpcHandler().getId(entry.getValue())).getVisibility(), 2)&&entry.getValue().isSpawnedFor(event.getPlayer().getUniqueId()))entry.getValue().despawn(event.getPlayer());
			});
		}
		
		@EventHandler
		public void join(PlayerJoinEvent event) {
			npcHandler.getEntities().values().stream().filter(npc -> npc.getBukkitEntity().getLocation().distanceSquared(event.getPlayer().getLocation()) <= Math.pow(getNpcHandler().getNPC(getNpcHandler().getId(npc)).getVisibility(), 2))
			.forEach(npc -> {
				npc.spawn(event.getPlayer());
			});
		}
		
		@EventHandler
		public void quit(PlayerQuitEvent event) {
			npcHandler.getEntities().values().stream().filter(npc -> npc.isSpawnedFor(event.getPlayer().getUniqueId()))
			.forEach(npc -> {
				npc.despawn(event.getPlayer());
			});
		}
	}
}