package me.commonsenze.core.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;

import com.mongodb.client.model.Filters;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Managers.ManagerHandler;
import me.commonsenze.core.Managers.impl.MongoManager;
import me.commonsenze.core.Managers.impl.RankManager;
import me.commonsenze.core.Util.RankUpdateEvent;
import me.commonsenze.core.Util.RankUpdateEvent.RankChange;
import me.commonsenze.core.Util.TypeUtil;

@Getter
public class Rank implements Cloneable {

	private String name;
	private String tabColor, prefix;
	@Getter(value = AccessLevel.NONE) private UUID uuid;
	@Setter private boolean isDefault;
	private int ranking;
	private List<String> permissions;

	private Rank(String name) {
		Document doc = new Document();
		doc.append("name", this.name = name);
		doc.append("uuid", (this.uuid = UUID.randomUUID()).toString());
		doc.append("tabColor", this.tabColor = "&f");
		doc.append("prefix", this.prefix = "&f");
		doc.append("permissions", Core.getGson().toJson(this.permissions = new ArrayList<>()));
		doc.append("ranking", this.ranking = 0);
		doc.append("isDefault", this.isDefault = false);

		Core.getInstance().getManagerHandler().getMongoManager().getConnection().getDatabase(MongoManager.DATABASE)
		.getCollection(MongoManager.RANK_COLLECTION).insertOne(doc);
	}

	public Rank(Document document) {
		this.name = document.getString("name");
		this.uuid = UUID.fromString(document.getString("uuid"));
		this.ranking = document.getInteger("ranking");
		this.isDefault = document.getBoolean("isDefault");
		this.permissions = Core.getGson().fromJson(document.getString("permissions"), TypeUtil.LIST_STRING);
		this.prefix = document.getString("prefix");
		this.tabColor = document.getString("tabColor");
	}

	private Rank(Rank rank) {
		copy(rank);
	}

	public UUID getUUID() {
		return uuid;
	}

	public void setTabColor(String tabColor) {
		this.tabColor = tabColor;
		RankUpdateEvent event = new RankUpdateEvent(this, RankChange.TAB_COLOR);
		Bukkit.getPluginManager().callEvent(event);
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
		RankUpdateEvent event = new RankUpdateEvent(this, RankChange.RANKING);
		Bukkit.getPluginManager().callEvent(event);
	}

	public List<String> getOwnedPermissions() {
		List<String> perms = getPermissions();
		Core.getInstance().getManagerHandler().getRankManager().getRanks().stream().filter(rank -> hasAuthorityOver(rank)).forEach(rank -> perms.addAll(rank.getPermissions()));
		return perms;
	}

	public void removePermission(String permission) {
		getPermissions().remove(permission.toLowerCase());
		ManagerHandler handler = Core.getInstance().getManagerHandler();
		handler.getProfileManager().getProfileSet().stream().filter(profile -> profile.hasRank()&&profile.getRank().hasAuthorityOver(this)||profile.getRank().equals(this))
		.forEach(profile -> {
			if (!profile.getRank().hasPermission(permission)) {
				handler.getProfileManager().getPermissionAttachment(profile.getPlayer()).setPermission(permission, false);
			}
		});
	}

	public void addPermission(String permission) {
		getPermissions().add(permission.toLowerCase());
		ManagerHandler handler = Core.getInstance().getManagerHandler();
		handler.getProfileManager().getProfileSet().stream().filter(profile -> profile.hasRank()&&profile.getRank().hasAuthorityOver(this)||profile.getRank().equals(this))
		.forEach(profile -> handler.getProfileManager().getPermissionAttachment(profile.getPlayer()).setPermission(permission, true));
	}

	public boolean hasPermission(String permission) {
		return getOwnedPermissions().contains(permission.toLowerCase());
	}

	/**
	 * Checks to see if the rank is high enough ranking.
	 * This is checked to see if the ranking is higher than
	 * <b>or equal to</b> the ranking that was inputed.
	 * @param ranking - the ranking number the rank must be to have authority
	 * @return if the rank's ranking is above the one placed in the parameter
	 */
	public boolean hasAuthority(int ranking) {
		return getRanking() <= ranking;
	}

	/**
	 * Checks to see if the rank has authoritarian powers over
	 * another. Use this method for checking if the rank is above
	 * the one being put into the parameter.
	 * @param rank - the rank that checks the authority over the current rank
	 * @return if the rank is above the one placed in the parameter
	 */
	public boolean hasAuthorityOver(Rank rank) {
		return getRanking() < rank.getRanking();
	}

	public static Rank create(String name) {
		Document doc = null;
		if ((doc = Core.getInstance().getManagerHandler().getRankManager().getRanksCollection().find(Filters.eq("name", name)).first())!=null)
			return Core.getInstance().getManagerHandler().getRankManager().getRank(UUID.fromString(doc.getString("uuid")));
		return new Rank(name);
	}

	public Document getDocument() {
		Document document = new Document("uuid", this.getUUID().toString());
		document.append("name", this.name);
		document.append("tabColor", this.tabColor);
		document.append("prefix", this.prefix);
		document.append("permissions", Core.getGson().toJson(this.permissions));
		document.append("ranking", this.ranking);
		document.append("isDefault", this.isDefault);
		return document;
	}

	public void copy(Rank rank) {
		this.name = rank.name;
		this.isDefault = rank.isDefault;
		this.permissions = rank.permissions;
		this.prefix = rank.prefix;
		this.ranking = rank.ranking;
		this.tabColor = rank.tabColor;
		this.uuid = rank.uuid;
	}

	/**
	 * Creates a mirrored clone of the rank, however, the clone is not automatically 
	 * registered in {@link RankManager} and is a dummy Rank holder. This method is
	 * for saving old settings inside the rank to allow changes and revert back to the old
	 * ones should they be needed.
	 * @return a new rank object with the same Rank variables.
	 */
	public Rank clone() {
		return new Rank(this);
	}

	public boolean equals(Rank rank) {
		return this.name.equals(rank.name)&&this.permissions.equals(rank.permissions)
				&&this.isDefault==rank.isDefault
				&&this.prefix.equals(rank.prefix)
				&&this.ranking==rank.ranking
				&&this.tabColor.equals(rank.tabColor)
				&&this.uuid.equals(rank.uuid);
	}

	public List<UUID> getUsers() {
		return Core.getInstance().getManagerHandler().getProfileManager().getPlayersWithRank(this);
	}

	@Override
	public String toString() {
		return "Rank[name="+this.name+",uuid="+this.uuid.toString()+",tabColor="+this.tabColor+",ranking="+this.ranking+",prefix="+this.prefix+",isDefault="+this.isDefault+",permissions={"+this.permissions.toString()+"}]";
	}
}
