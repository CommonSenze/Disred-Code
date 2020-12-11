package com.topcat.npclib.nms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import com.comphenix.protocol.PacketType;
import com.mojang.authlib.GameProfile;
import com.topcat.npclib.NPCHandler;
import com.topcat.npclib.NPCManager;

import lombok.Getter;
import lombok.Setter;
import me.commonsenze.wrapper.Packets.WrapperPlayServerEntityHeadRotation;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;

/**
 * 
 * @author martin
 */
public class NPCEntity extends EntityPlayer {

	private int lastTargetId;
	private long lastBounceTick;
	private int lastBounceId;
	@Getter private Set<UUID> spawnedFor;
	private Map<PacketType, Packet<?>> packetList;
	@Getter @Setter private String skinName;

	public NPCEntity(NPCHandler npcHandler, BWorld world, String s, PlayerInteractManager itemInWorldManager) {
		super(npcHandler.getServer().getMCServer(), world.getWorldServer(), new GameProfile(UUID.randomUUID(), s), itemInWorldManager);

		itemInWorldManager.b(EnumGamemode.SURVIVAL);

		playerConnection = new NPCPlayerConnection(npcHandler.getNPCManager(), this);
		lastTargetId = -1;
		lastBounceId = -1;
		lastBounceTick = 0;

		fauxSleeping = true;
		
		spawnedFor = new HashSet<>();
		this.packetList = new HashMap<>();
	}

	public void setBukkitEntity(org.bukkit.entity.Entity entity) {
		bukkitEntity = (CraftEntity) entity;
	}

	@Override
	public boolean a(EntityHuman entity) {
		final EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.NPC_RIGHTCLICKED);
		Bukkit.getPluginManager().callEvent(event);

		return super.a(entity);
	}


	@Override
	public void d(EntityHuman entity) {
		if ((lastBounceId != entity.getId() || System.currentTimeMillis() - lastBounceTick > 1000) && entity.getBukkitEntity().getLocation().distanceSquared(getBukkitEntity().getLocation()) <= 1) {
			final EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.NPC_BOUNCED);
			Bukkit.getPluginManager().callEvent(event);

			lastBounceTick = System.currentTimeMillis();
			lastBounceId = entity.getId();
		}

		if (lastTargetId == -1 || lastTargetId != entity.getId()) {
			final EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.CLOSEST_PLAYER);
			Bukkit.getPluginManager().callEvent(event);
			lastTargetId = entity.getId();
		}

		super.d(entity);
	}

	@Override
	public void c(Entity entity) {
		if (lastBounceId != entity.getId() || System.currentTimeMillis() - lastBounceTick > 1000) {
			final EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.NPC_BOUNCED);
			Bukkit.getPluginManager().callEvent(event);

			lastBounceTick = System.currentTimeMillis();
		}

		lastBounceId = entity.getId();

		super.c(entity);
	}

	@Override
	public void move(double arg0, double arg1, double arg2) {
		setPosition(arg0, arg1, arg2);
	}

	public void spawn(Player player) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		
		DataWatcher watcher = new DataWatcher(null);
		watcher.a(16, (byte)127);
		PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(getId(), watcher, true);
		
		connection.sendPacket(packet);
		
		connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this));

		connection.sendPacket(new PacketPlayOutNamedEntitySpawn(this));

		WrapperPlayServerEntityHeadRotation rotation = new WrapperPlayServerEntityHeadRotation();
		rotation.setEntityId(getId());
		rotation.setHeadYaw(yaw);
		connection.sendPacket((PacketPlayOutEntityHeadRotation)rotation.getHandle().getHandle());
		
		if (!packetList.isEmpty()) {
			packetList.values().forEach(connection::sendPacket);
		}

		spawnedFor.add(player.getUniqueId());
		
		Bukkit.getScheduler().runTaskLater(NPCManager.plugin, () -> {
			connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, this));
		}, 10);
	}

	public void despawn(Player player) {
		spawnedFor.remove(player.getUniqueId());
		PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutEntityDestroy(getId()));
	}
	
	public void addPacket(PacketType type, Packet<?> packet) {
		packetList.put(type,packet);
	}
	
	public void removePacketType(PacketType type) {
		packetList.remove(type);
	}
	
	public boolean isSpawnedFor(UUID uuid) {
		return spawnedFor.contains(uuid);
	}
}