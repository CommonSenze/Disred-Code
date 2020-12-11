package me.commonsenze.core.Managers.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import lombok.Getter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Managers.Manager;
import me.commonsenze.core.Managers.ManagerHandler;
import me.commonsenze.core.Objects.Rank;
import me.commonsenze.core.Util.NameTagUpdateEvent;
import me.commonsenze.core.Util.RankUpdateEvent;
import me.commonsenze.core.Util.NameTagUpdateEvent.Reason;

public class RankManager extends Manager {

	@Getter private MongoCollection<Document> ranksCollection;
	@Getter private Set<Rank> ranks;
	
	public RankManager(ManagerHandler managerHandler) {
		super(managerHandler);
		this.ranksCollection = getConnection().getDatabase(MongoManager.DATABASE).getCollection(MongoManager.RANK_COLLECTION);
		this.ranks = new HashSet<>();
		registerAsListener();
		this.load();
	}

	private void load() {
		this.ranksCollection.find().forEach((Block<Document>) document -> {
			ranks.add(new Rank(document));
		});
	}
	
	public void save() {
		for (Rank rank : ranks) {
			this.ranksCollection.replaceOne(Filters.eq("uuid", rank.getUUID().toString()), rank.getDocument());
		}
	}
	
	public Rank getRank(String name) {
		return ranks.stream().filter(rank -> rank.getName().equals(name)).findFirst().orElse(null);
	}

	public Rank getRank(UUID uuid) {
		return ranks.stream().filter(rank -> rank.getUUID().equals(uuid)).findFirst().orElse(null);
	}
	
	public Rank getDefaultRank() {
		return ranks.stream().filter(rank -> rank.isDefault()).findFirst().orElse(null);
	}
	
	public void setDefaultRank(Rank def) {
		ranks.stream().filter(rank -> rank.isDefault()).forEach(rank -> rank.setDefault(false));
		
		def.setDefault(true);
	}
	
	public void deleteRank(UUID uuid) {
		getRank(uuid).getUsers().stream().filter(id -> Bukkit.getPlayer(id)!= null)
		.forEach(id -> Core.getInstance().getManagerHandler().getProfileManager().getProfile(id).setRank(getDefaultRank()));
		ranks.remove(getRank(uuid));
		this.ranksCollection.deleteOne(Filters.eq("uuid", uuid.toString()));
	}
	
	public Rank createRank(String name) {
		ranks.add(Rank.create(name));
		return getRank(name);
	}

	public List<Rank> getSortedRanks() {
		if (ranks.isEmpty())return new ArrayList<>();
		List<Rank> toReturn = new ArrayList<>(ranks);
        toReturn.sort(Comparator.comparingInt(Rank::getRanking));
        return toReturn;
	}
	
	@EventHandler
	public void rankUpdate(RankUpdateEvent event) {
		event.getRank().getUsers().stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).forEach(uuid -> {
			NameTagUpdateEvent e = new NameTagUpdateEvent(managerHandler.getProfileManager().getProfile(uuid), Reason.RANK_CHANGE);
			Bukkit.getPluginManager().callEvent(e);
		});
	}
}
