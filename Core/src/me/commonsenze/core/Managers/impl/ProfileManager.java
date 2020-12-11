package me.commonsenze.core.Managers.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.util.Vector;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import lombok.Getter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Managers.Manager;
import me.commonsenze.core.Managers.ManagerHandler;
import me.commonsenze.core.Objects.PermissionBase;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Objects.Rank;
import me.commonsenze.core.Util.NameTagUpdateEvent;
import me.commonsenze.core.Util.NameTagUpdateEvent.Reason;
import me.commonsenze.core.Util.TypeUtil;
import net.md_5.bungee.api.ChatColor;

@Getter
public class ProfileManager extends Manager {

	private Set<Profile> profileSet;
	private MongoCollection<Document> profilesCollection;
	private HashMap<UUID, PermissionAttachment> permissionAttachments;

	public ProfileManager(ManagerHandler managerHandler) {
		super(managerHandler);
		this.profileSet = new HashSet<>();
		this.profilesCollection = getConnection().getDatabase(MongoManager.DATABASE).getCollection(MongoManager.PLAYER_COLLECTION);
		this.permissionAttachments = new HashMap<>();
		registerAsListener();
		load();
	}

	private void load() {
		for (Player player : getPlugin().getServer().getOnlinePlayers()) {
			registerProfile(player);
		}
	}

	public Profile registerProfile(Player player) {
		getCache().put(player.getName(), player.getUniqueId());
		try {
			Field field = CraftHumanEntity.class.getDeclaredField("perm");
			field.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			field.set(player, new PermissionBase(getCache().getUUID(player.getName())));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		createPermissionAttachment(player);
		Profile profile = new Profile(player, this, managerHandler.getRankManager());
		profileSet.add(profile);
		if (profile.hasRank())
			profile.getRank().getOwnedPermissions().forEach(permission -> {
				getPermissionAttachment(player).setPermission(permission, true);
			});
		profile.getPermissions().forEach(permission -> {
			getPermissionAttachment(player).setPermission(permission, true);
		});
		return profile;
	}

	public void unregisterProfile(Player player) {
		UUID uuid = getCache().getUUID(player.getName());
		if (getDocument(uuid) != null)
			this.profilesCollection.replaceOne(Filters.eq("uuid", uuid.toString()), getProfile(player).createDocument());
		else this.profilesCollection.insertOne(getProfile(player).createDocument());
		profileSet.remove(getProfile(player));
		removePermissionAttachment(player);
	}

	public void createPermissionAttachment(Player player) {
		UUID uuid = getCache().getUUID(player.getName());
		if (!permissionAttachments.containsKey(uuid))
			permissionAttachments.put(uuid, player.addAttachment(getPlugin()));
	}

	public PermissionAttachment getPermissionAttachment(Player player) {
		UUID uuid = getCache().getUUID(player.getName());
		if (!permissionAttachments.containsKey(uuid))
			permissionAttachments.put(uuid, player.addAttachment(getPlugin()));
		return permissionAttachments.get(uuid);
	}

	public void removePermissionAttachment(Player player) {
		UUID uuid = getCache().getUUID(player.getName());
		player.removeAttachment(permissionAttachments.get(uuid));
		permissionAttachments.remove(uuid);
	}

	public List<Profile> getPermissionedProfiles(String permission) {
		return profileSet.stream().filter(profile -> profile.hasPermission(permission)).collect(Collectors.toList());
	}

	public Profile getProfile(Player player) {
		if (player == null)return null;
		return profileSet.stream().filter(profile -> profile.getUniqueId().equals(getCache().getUUID(player.getName()))).findFirst().orElse(null);
	}

	public List<Profile> getSortedProfiles() {
		List<Profile> toReturn = new ArrayList<>(profileSet);
		toReturn.sort(Comparator.comparingInt(Profile::getWeight));
		Collections.reverse(toReturn);
		return toReturn;
	}

	public Rank getRank(UUID uuid) {
		return managerHandler.getRankManager().getRank(uuid);
	}

	public boolean hasPermission(UUID uuid, String permission) {
		return getOfflinePermissions(uuid).contains(permission.toLowerCase());
	}

	public List<UUID> getPlayersWithRank(Rank rank){
		List<UUID> uuids = new ArrayList<>();

		getProfilesCollection().find(Filters.eq("rank", rank.getUUID().toString()))
		.iterator()
		.forEachRemaining(doc -> uuids.add(UUID.fromString(doc.getString("uuid"))));
		return uuids;
	}

	public void removeOfflinePermission(UUID uuid, String permission) {
		List<String> perms = getOfflinePermissions(uuid);
		perms.remove(permission.toLowerCase());
		Document doc = getDocument(uuid);
		doc.put("permissions", Core.getGson().toJson(perms));
		this.profilesCollection.replaceOne(Filters.eq("uuid", uuid.toString()), doc);
	}

	public void addOfflinePermission(UUID uuid, String permission) {
		List<String> perms = getOfflinePermissions(uuid);
		perms.add(permission.toLowerCase());
		Document doc = getDocument(uuid);
		doc.put("permissions", Core.getGson().toJson(perms));
		this.profilesCollection.replaceOne(Filters.eq("uuid", uuid.toString()), doc);
	}

	public List<String> getOfflinePermissions(UUID uuid) {
		return Core.getGson().fromJson(getDocument(uuid).getString("permissions"), TypeUtil.LIST_STRING);
	}

	public Document getDocument(UUID uuid) {
		return getProfilesCollection().find(Filters.eq("uuid", uuid.toString())).first();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void join(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		Profile profile = registerProfile(player);

		profile.clearPlayer();

		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

		NameTagUpdateEvent e = new NameTagUpdateEvent(profile, Reason.PLAYER_JOIN);
		Bukkit.getPluginManager().callEvent(e);

		event.setJoinMessage(ChatColor.GREEN + "✔ " +ChatColor.GRAY + player.getName() + " has connected");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void quit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		unregisterProfile(player);

		event.setQuitMessage(ChatColor.RED + "✖ " +ChatColor.GRAY + player.getName() + " has disconnected");
	}

	@EventHandler
	public void damage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))return;
		Profile profile = getProfile(getCache().getUUID(event.getEntity().getName()));

		if (profile.isInvincible())event.setCancelled(true);
	}

	@EventHandler
	public void frozen(PlayerMoveEvent event) {
		Profile profile = getProfile(getCache().getUUID(event.getPlayer().getName()));

		if (profile.isFrozen()&&(event.getTo().getBlockX() != event.getFrom().getBlockX()
				||event.getTo().getBlockY() != event.getFrom().getBlockY()||event.getTo().getBlockZ() != event.getFrom().getBlockZ()))
			event.setTo(event.getFrom());
	}

	@EventHandler
	public void build(BlockBreakEvent event) {
		Profile profile = getProfile(getCache().getUUID(event.getPlayer().getName()));

		if (!profile.canBuild()||profile.getPlayer().getGameMode()!=GameMode.CREATIVE)event.setCancelled(true);
	}

	@EventHandler
	public void build(BlockPlaceEvent event) {
		Profile profile = getProfile(getCache().getUUID(event.getPlayer().getName()));

		if (!profile.canBuild()||profile.getPlayer().getGameMode()!=GameMode.CREATIVE)event.setCancelled(true);
	}

	@EventHandler
	public void death(PlayerDeathEvent event) {
		Profile profile = getProfile(getCache().getUUID(event.getEntity().getName()));
		event.setDeathMessage(null);
		event.getEntity().spigot().respawn();
		profile.getPlayer().setVelocity(new Vector());
		profile.clearPlayer();
		Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getInstance(), new Runnable() {
			public void run() {
				profile.getPlayer().getPlayer().setFireTicks(0);
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void spawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(Core.getInstance().getManagerHandler().getServerManager().getSpawn());
	}

	public Profile getProfile(UUID uniqueId) {
		return profileSet.stream().filter(profile -> profile.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
	}

	public void unload() {
		for (Player player : getPlugin().getServer().getOnlinePlayers()) {
			unregisterProfile(player);
		}
	}
}
