package me.commonsenze.lobby.Objects;

import java.util.UUID;

import org.bson.Document;
import org.bukkit.entity.Player;

import lombok.Getter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.lobby.Enums.State;

@Getter
public class User {

	private Player player;
	private UUID uniqueId;
	private State state;
	
	public User(Player player) {
		this.player = player;
		this.uniqueId = CoreAPI.getInstance().getCache().getUUID(player.getName());
		this.state = State.SPAWN;
	}

	public Document createDocument() {
		Document document = new Document("uuid", this.getUniqueId().toString());
		return document;
	}
}
