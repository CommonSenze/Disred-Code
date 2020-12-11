package com.topcat.npclib.entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.comphenix.protocol.PacketType;
import com.topcat.npclib.NPCManager;
import com.topcat.npclib.nms.NPCEntity;

import lombok.Getter;
import me.commonsenze.wrapper.Packets.WrapperPlayClientArmAnimation;
import me.commonsenze.wrapper.Packets.WrapperPlayServerEntityTeleport;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutBed;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.WorldServer;

public class HumanNPC extends NPC {

	@Getter private NPCEntity npcEntity;

	public HumanNPC(NPCEntity npcEntity) {
		super(npcEntity);
		this.npcEntity = npcEntity;
	}

	public void animateArmSwing() {
		WrapperPlayClientArmAnimation armAnimation = new WrapperPlayClientArmAnimation();

		armAnimation.setEntityID(getEntity().getId());
		armAnimation.setAnimation(WrapperPlayClientArmAnimation.Animations.SWING_ARM);

		((WorldServer) getEntity().world).tracker.a(getEntity(), (PacketPlayInArmAnimation)armAnimation.getHandle().getHandle());
	}

	public void actAsHurt() {
		WrapperPlayClientArmAnimation armAnimation = new WrapperPlayClientArmAnimation();

		armAnimation.setEntityID(getEntity().getId());
		armAnimation.setAnimation(WrapperPlayClientArmAnimation.Animations.DAMAGE_ANIMATION);

		((WorldServer) getEntity().world).tracker.a(getEntity(), (PacketPlayInArmAnimation)armAnimation.getHandle().getHandle());
	}

	public void setItemInHand(Material m) {
		setItemInHand(m, (short) 0);
	}

	public void setItemInHand(Material m, short damage) {
		((HumanEntity) getEntity().getBukkitEntity()).setItemInHand(new ItemStack(m, 1, damage));
	}

	public void setName(String name) {
		try {
			final Class<?> clazz = EntityHuman.class;
			final Field nameField = clazz.getDeclaredField("name");
			nameField.setAccessible(true);
			nameField.set(getEntity(), name);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return ((NPCEntity) getEntity()).getName();
	}

	public PlayerInventory getInventory() {
		return ((HumanEntity) getEntity().getBukkitEntity()).getInventory();
	}

	public void putInBed(Location bed, Location next) {
		PacketPlayOutBed packetplayoutbed = new PacketPlayOutBed(npcEntity, new BlockPosition(bed.getBlockX(), bed.getBlockY(), bed.getBlockZ()));
		
		WrapperPlayServerEntityTeleport tp = new WrapperPlayServerEntityTeleport();
		tp.setEntityID(npcEntity.getId());
		tp.setLoc(next);
		//		npcEntity.a(new BlockPosition(bed.getBlockX(), bed.getBlockY(), bed.getBlockZ()));
		npcEntity.addPacket(PacketType.Play.Server.BED, packetplayoutbed);
		npcEntity.addPacket(PacketType.Play.Server.ENTITY_TELEPORT, (PacketPlayOutEntityTeleport)tp.getHandle().getHandle());
		npcEntity.getSpawnedFor().forEach(uuid -> {
			((CraftPlayer)Bukkit.getPlayer(uuid)).getHandle().playerConnection.sendPacket(packetplayoutbed);
			try {
				NPCManager.manager.sendServerPacket(Bukkit.getPlayer(uuid), tp.getHandle());
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public void getOutOfBed() {
		((NPCEntity) getEntity()).a(true, true, true);
	}

	public void setSneaking() {
		getEntity().setSneaking(true);
	}

	public void lookAtPoint(Location point) {
		if (getEntity().getBukkitEntity().getWorld() != point.getWorld()) {
			return;
		}
		final Location npcLoc = ((LivingEntity) getEntity().getBukkitEntity()).getEyeLocation();
		final double xDiff = point.getX() - npcLoc.getX();
		final double yDiff = point.getY() - npcLoc.getY();
		final double zDiff = point.getZ() - npcLoc.getZ();
		final double DistanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
		final double DistanceY = Math.sqrt(DistanceXZ * DistanceXZ + yDiff * yDiff);
		double newYaw = Math.acos(xDiff / DistanceXZ) * 180 / Math.PI;
		final double newPitch = Math.acos(yDiff / DistanceY) * 180 / Math.PI - 90;
		if (zDiff < 0.0) {
			newYaw = newYaw + Math.abs(180 - newYaw) * 2;
		}
		getEntity().yaw = (float) (newYaw - 90);
		getEntity().pitch = (float) newPitch;
		try {
			Field field = EntityLiving.class.getDeclaredField("aP");
			field.setAccessible(true);
			field.set(((EntityPlayer) getEntity()), (float) (newYaw - 90));
			field.setAccessible(!field.isAccessible());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

}