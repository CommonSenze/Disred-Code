package me.commonsenze.lobby.Managers.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import lombok.Getter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Abstracts.Menu;
import me.commonsenze.core.Menus.CoreSettingsMenu;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Util.CC;
import me.commonsenze.lobby.Managers.Manager;
import me.commonsenze.lobby.Managers.ManagerHandler;
import me.commonsenze.lobby.Objects.User;
import net.md_5.bungee.api.ChatColor;

@Getter
public class UserManager extends Manager {

	private Set<User> userSet;
	private MongoCollection<Document> usersCollection;
	
	public UserManager(ManagerHandler managerHandler) {
		super(managerHandler);
		this.userSet = new HashSet<>();
		this.usersCollection = getConnection().getDatabase(MongoManager.DATABASE).getCollection(MongoManager.PLAYER_COLLECTION);
		registerAsListener();
		load();
	}

	private void load() {
		for (Player player : getPlugin().getServer().getOnlinePlayers()) {
			registerUser(player);
        }
	}
	
	public User registerUser(Player player) {
		User user = new User(player);
		
		sendToSpawn(player);
		
		userSet.add(user);
        return user;
	}
	
	public void sendToSpawn(Player player) {
		player.teleport(CoreAPI.getInstance().getSpawnLocation());
		player.getInventory().setItem(8, new ItemCreation(Material.CHEST).setDisplayName(CC.GREEN + "Settings "+CC.GRAY + "(Right Click)")
				.addLore("Click to see your settings.")
				.toItemStack());
	}
	
	public void unregisterUser(Player player) {
		UUID uuid = CoreAPI.getInstance().getCache().getUUID(player.getName());
		if (getDocument(uuid) != null)
			this.usersCollection.replaceOne(Filters.eq("uuid", uuid.toString()), getUser(player).createDocument());
		else this.usersCollection.insertOne(getUser(player).createDocument());
		userSet.remove(getUser(player));
	}
	
	public Document getDocument(UUID uuid) {
		return getUsersCollection().find(Filters.eq("uuid", uuid.toString())).first();
	}
	
	public User getUser(Player player) {
		if (player == null)return null;
		return userSet.stream().filter(user -> user.getUniqueId().equals(CoreAPI.getInstance().getCache().getUUID(player.getName()))).findFirst().orElse(null);
	}
	
	public void unload() {
		for (Player player : getPlugin().getServer().getOnlinePlayers()) {
			unregisterUser(player);
		}
	}
	
	@EventHandler
	public void click(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item == null)return;
		if (!item.hasItemMeta())return;
		String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
		if (name.contains("Settings")) {
			event.setCancelled(true);
			Menu menu = new CoreSettingsMenu(event.getPlayer()).create();
			menu.open();
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		Profile profile = CoreAPI.getInstance().getProfile(player);
		
		profile.setInvincible(true);
		
		registerUser(player);
		
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		
		unregisterUser(player);
	}
}
