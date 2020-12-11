package me.commonsenze.core.Objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Managers.impl.MongoManager;
import me.commonsenze.core.Managers.impl.ProfileManager;
import me.commonsenze.core.Managers.impl.RankManager;
import me.commonsenze.core.Managers.impl.TimerManager;
import me.commonsenze.core.Util.CC;
import me.commonsenze.core.Util.NameTagUpdateEvent;
import me.commonsenze.core.Util.TypeUtil;
import me.commonsenze.core.Util.NameTagUpdateEvent.Reason;

@Getter
public class Profile {

	@Getter(value = AccessLevel.NONE) private static final int NORMAL_HEALTH = 20;

	@Getter(value = AccessLevel.NONE) private int CURRENT_HEALTH = NORMAL_HEALTH;

	private String name;
	private Player player;
	private UUID uniqueId;
	@Setter private UUID messager;
	private Rank rank;
	private List<String> permissions;
	private TimerManager timerManager;
	private long timeJoined;
	private Team currentTeam;

	@Setter private boolean isFrozen, invincible, messageable = true;
	private boolean vanish;
	@Getter(value = AccessLevel.NONE) @Setter private boolean hasChat = true, hasScoreboard= true, build;

	@Getter(value = AccessLevel.PRIVATE) private ProfileManager profileManager;

	@Getter(value = AccessLevel.NONE) private boolean menuOpened;

	public Profile(Player player, ProfileManager profileManager, RankManager rankManager) {
		this.player = player;
		this.profileManager = profileManager;
		this.name = player.getName();
		this.uniqueId = Core.getInstance().getCache().getUUID(player.getName());
		this.timerManager = new TimerManager();
		Document document = getProfileManager().getProfilesCollection().find(Filters.eq("uuid", this.uniqueId.toString())).first();
		if (document == null)
			document = registerProfile(rankManager);
		load(document, rankManager);
	}

	private Document registerProfile(RankManager rankManager) {
		Document document = new Document("uuid", uniqueId.toString());
		document.append("timeJoined", System.currentTimeMillis());
		document.append("permissions", Core.getGson().toJson(new ArrayList<>()));
		document.append("hasScoreboard", true);
		document.append("hasChat", true);
		document.append("messageable", true);
		if (rankManager.getDefaultRank() != null)
			document.append("rank", rankManager.getDefaultRank().getUUID().toString());
		Core.getInstance().getManagerHandler().getMongoManager().getConnection().getDatabase(MongoManager.DATABASE).getCollection(MongoManager.PLAYER_COLLECTION).insertOne(document);
		return document;
	}

	private void load(Document document, RankManager rankManager) {
		if (document.getString("rank") != null)
			this.rank = rankManager.getRank(UUID.fromString(document.getString("rank")));
		this.hasScoreboard = document.getBoolean("hasScoreboard");
		this.hasChat = document.getBoolean("hasChat");
		this.timeJoined = document.getLong("timeJoined");
		this.permissions = Core.getGson().fromJson(document.getString("permissions"), TypeUtil.LIST_STRING);

		if (!hasRank()&&rankManager.getDefaultRank() != null)
			this.rank = rankManager.getDefaultRank();
	}

	public void setTeam(Team team) {
		if (!team.hasEntry(getName()))
			team.addEntry(getName());
		this.currentTeam = team;
		Bukkit.getOnlinePlayers().forEach(player -> {
			Core.getInstance().getManagerHandler().getScoreboardTeamManager().addTeamToScoreboard(player, team);
		});
	}

	public String getPersonalTeamName() {
		if (rank != null) {
			return String.format("%03d",rank.getRanking())+(getName().length() > 13 ? getName().substring(0, 13) : getName());
		} else {
			return String.format("%03d",999)+(getName().length() > 13 ? getName().substring(0, 13) : getName());
		}
	}

	public void ban(String banner) {
		Date date = new Date();

		Document document = new Document("uuid", getUniqueId());
		document.append("banner", banner);
		document.append("date", date);

		MongoDatabase database = Core.getInstance().getManagerHandler().getMongoManager().getConnection().getDatabase(MongoManager.DATABASE);
		if (database.getCollection(MongoManager.BAN_COLLECTION).findOneAndReplace(Filters.eq("uuid", getUniqueId()), document) == null) {
			database.getCollection(MongoManager.BAN_COLLECTION).insertOne(document);
		}

		String kickMessage = 
				"&cYou are permanently banned from this server.\n"+
						"\n"+
						"&7Reason: &eNo reason specified\n"+
						"&7Find out more: &b&mComing Soon";

		getPlayer().kickPlayer(CC.translate(Core.getInstance().getConfig("messages").getConfig().getString("ban.kick-message",kickMessage)));
	}

	public void ban(String banner, String reason) {
		Date date = new Date();

		Document document = new Document("uuid", getUniqueId());
		document.append("banner", banner);
		document.append("date", date);
		document.append("reason", reason);

		MongoDatabase database = Core.getInstance().getManagerHandler().getMongoManager().getConnection().getDatabase(MongoManager.DATABASE);
		if (database.getCollection(MongoManager.BAN_COLLECTION).findOneAndReplace(Filters.eq("uuid", getUniqueId()), document) == null) {
			database.getCollection(MongoManager.BAN_COLLECTION).insertOne(document);
		}
		getPlayer().kickPlayer(CC.RED + "You've been banned\nReason: "+CC.GRAY+reason);
	}

	public Cuboid getSelection() {
		try {
			Region region = getWorldEdit().getSession(getPlayer()).getSelection(getWorldEdit().getSession(getPlayer()).getSelectionWorld());
			return new Cuboid(getPlayer().getWorld(), region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ(), 
					region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ());
		} catch (IncompleteRegionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public WorldEditPlugin getWorldEdit() {
		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		if (p instanceof WorldEditPlugin) return (WorldEditPlugin) p;
		else return null;
	}

	public void updateDisplayName() {
		getPlayer().setDisplayName((hasRank() ? CC.translate(getRank().getPrefix()): "")+getName());
	}

	public boolean hasMessager() {
		return messager != null;
	}

	public void setVanish(boolean vanish) {
		this.vanish = vanish;

		if (vanish)Core.getInstance().getManagerHandler().getVanishManager().vanish(this);
		else Core.getInstance().getManagerHandler().getVanishManager().unvanish(this);
	}

	public void clearPlayer() {
		for (PotionEffect effect : this.getPlayer().getActivePotionEffects()) {
			this.getPlayer().removePotionEffect(effect.getType());
		}
		this.getPlayer().getPlayer().setExp(0);
		this.getPlayer().getPlayer().setLevel(0);
		this.getPlayer().getPlayer().setFireTicks(0);
		this.getPlayer().getPlayer().getInventory().clear();
		this.getPlayer().getPlayer().getInventory().setArmorContents(null);
		this.getPlayer().setGameMode(GameMode.SURVIVAL);
		this.getPlayer().getPlayer().setAllowFlight(false);
		this.getPlayer().getPlayer().setFlying(false);
		this.getPlayer().setFoodLevel(20);
		this.setMaxHealth(NORMAL_HEALTH);
		this.getPlayer().setFlySpeed(0.2F);
	}

	public boolean canBuild() {
		return build;
	}

	public void setMaxHealth(int health) {
		this.CURRENT_HEALTH = health;
		heal();
	}

	public void heal() {
		this.getPlayer().getPlayer().setMaxHealth(CURRENT_HEALTH);
		this.getPlayer().getPlayer().setHealth(CURRENT_HEALTH);
	}

	public boolean hasPermission(String permission) {
		return getPermissions().contains(permission.toLowerCase());
	}

	public void removePermission(String permission) {
		permission = permission.toLowerCase();
		getPermissions().remove(permission);
		getProfileManager().getPermissionAttachment(getPlayer()).setPermission(permission, false);
	}

	public void addPermission(String permission) {
		permission = permission.toLowerCase();
		getPermissions().add(permission);
		getProfileManager().getPermissionAttachment(getPlayer()).setPermission(permission, true);
	}

	public void setRank(Rank rank) {
		if (this.rank != null) {
			getRank().getOwnedPermissions().forEach(permission -> {
				if (!getPermissions().contains(permission))
					getProfileManager().getPermissionAttachment(getPlayer()).setPermission(permission, false);
			});
		}
		this.rank = rank;
		getRank().getOwnedPermissions().forEach(permission -> {
			getProfileManager().getPermissionAttachment(getPlayer()).setPermission(permission, true);
		});
		NameTagUpdateEvent event = new NameTagUpdateEvent(this, Reason.RANK_CHANGE);
		Bukkit.getPluginManager().callEvent(event);
	}

	public int getWeight() {
		return hasRank() ? getRank().getRanking() : 0;
	}

	public boolean hasRank() {
		return getRank()!=null;
	}

	public boolean hasScoreboardVisibility() {
		return hasScoreboard;
	}

	public boolean hasChat() {
		return hasChat;
	}

	public void setMenuOpened(boolean menuOpened) {
		this.menuOpened = menuOpened;
	}

	public boolean hasMenuOpened() {
		return menuOpened;
	}

	public Document createDocument() {
		Document document = new Document("uuid", this.getUniqueId().toString());
		if (this.rank != null)
			document.append("rank", this.rank.getUUID().toString());
		document.append("hasChat", this.hasChat);
		document.append("permissions", Core.getGson().toJson(this.permissions));
		document.append("hasScoreboard", this.hasScoreboard);
		document.append("messageable", this.messageable);
		if (profileManager.getDocument(getUniqueId()) != null) {
			profileManager.getDocument(getUniqueId()).forEach((key, value) -> {
				if (!document.containsKey(key))document.append(key, value);
			});
		}

		return document;
	}
}
