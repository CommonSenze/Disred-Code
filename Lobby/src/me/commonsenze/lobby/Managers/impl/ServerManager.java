package me.commonsenze.lobby.Managers.impl;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.lobby.Managers.Manager;
import me.commonsenze.lobby.Managers.ManagerHandler;

@Getter @Setter
public class ServerManager extends Manager {

	@Getter(value = AccessLevel.NONE)public static final String SERVER_FILE = "config";

	public ServerManager(ManagerHandler managerHandler) {
		super(managerHandler);
		registerAsListener();
	}

	@EventHandler
	public void hunger(FoodLevelChangeEvent e) {
		if (!getEditor(SERVER_FILE).getConfig().getBoolean("hunger-allowed", false)) {
			if (e.getFoodLevel() != 20)e.setFoodLevel(20);
			else e.setCancelled(true);
		}
	}
	
	
	
	@EventHandler
	public void jump(PlayerToggleFlightEvent event) {
		if (event.getPlayer().getGameMode()!=GameMode.CREATIVE) {
			if (!event.getPlayer().isFlying()&&event.getPlayer().getAllowFlight()) {
				event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.IRONGOLEM_THROW, 1, -1);
				event.setCancelled(true);
				event.getPlayer().setAllowFlight(false);
				event.getPlayer().setFlying(false);
				Vector vector = event.getPlayer().getEyeLocation().getDirection();
				vector.multiply(1.5F);
				vector.setY(1);
				event.getPlayer().setVelocity(vector);
			}
		}
	}
	
	@EventHandler
	public void move(PlayerMoveEvent event) {
		if (event.getFrom().getBlock().getRelative(BlockFace.DOWN).getType()!= Material.AIR) {
			event.getPlayer().setAllowFlight(true);
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent event) {
		event.getPlayer().teleport(CoreAPI.getInstance().getSpawnLocation());
	}
}
