package me.commonsenze.minigames.Games.Skywars.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.Interfaces.Callback;
import me.commonsenze.core.Objects.Cuboid;
import me.commonsenze.minigames.Games.Skywars.Objects.ChestItems.Type;
import me.commonsenze.minigames.Objects.User;

@Getter @Setter
public class Island {

	public static final int REQUIRED_CHEST_AMOUNT = 3;

	private User owner;
	private Location gameSpawn;
	private Cuboid cage;
	private Cuboid bounds;
	private ChestItems chestItems;
	private List<Chest> chests;

	public Island() {
		this(null, null, null);
	}

	public Island(Cuboid bounds, Cuboid cage, Location gameSpawn) {
		this.bounds = bounds;
		this.chests = new ArrayList<>();
		this.cage = cage;
		this.gameSpawn = gameSpawn;
		this.chestItems = new ChestItems();
		scanForChest();
	}

	public void spawnItems() {
		Random random = new Random();
		for (Chest chest : chests) {
			for (Type type : Type.values()) {
				int allowed = random.nextInt(type.getMaxAllowed())+1;
				for (int i = 0; i < allowed; i++) {
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), chestItems.byType(type));
				}
			}
		}
	}

	public boolean scanForChest() {
		forEachBlock(block -> {
			Material type = block.getType();

			if (type != Material.CHEST) {
				return;
			}

			Chest chest = (Chest) block.getState();

			chests.add(chest);
		});
		return chests.size() >= REQUIRED_CHEST_AMOUNT;
	}

	public void setOwner(User owner) {
		this.owner = owner;
		if (isInUse())
			owner.setGameSpawn(getGameSpawn());
	}

	public boolean isInUse() {
		return owner!=null;
	}

	public boolean isCompleted() {
		return bounds != null&&chests!= null&&cage!= null&&gameSpawn!=null&&chests.size() >= REQUIRED_CHEST_AMOUNT;
	}
	
	public void refillChest() {
		chests.forEach(chest -> {
			chest.getInventory().clear();
		});
		chestItems.upgradeLoot();
		spawnItems();
	}

	private void forEachBlock(Callback<Block> callback) {
		Location start = bounds.getLowerNE();
		Location end = bounds.getUpperSW();
		World world = bounds.getWorld();

		for (int x = start.getBlockX(); x <= end.getBlockX(); x++) {
			for (int y = start.getBlockY(); y <= end.getBlockY(); y++) {
				for (int z = start.getBlockZ(); z <= end.getBlockZ(); z++) {
					callback.callback(world.getBlockAt(x, y, z));
				}
			}
		}
	}

	public boolean isOwner(User owner) {
		return owner.equals(this.owner);
	}
}
