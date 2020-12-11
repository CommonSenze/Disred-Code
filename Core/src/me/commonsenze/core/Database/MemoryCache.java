package me.commonsenze.core.Database;

import java.util.HashMap;
import java.util.UUID;

import me.commonsenze.core.Interfaces.Cache;

public class MemoryCache implements Cache {

	private final HashMap<String, UUID> playerNames = new HashMap<>();
	
	@Override
	public void put(String name, UUID uuid) {
		if (playerNames.containsValue(uuid)&&getName(uuid).equals(name))return;
		else if (playerNames.containsValue(uuid)) playerNames.remove(getName(uuid));
		playerNames.put(name, uuid);
	}
	
	@Override
	public String getName(UUID uuid) {
		return playerNames.entrySet().stream().filter(entry -> entry.getValue().equals(uuid)).findFirst().orElse(null).getKey();
	}
	
	@Override
	public UUID getUUID(String name) {
		return playerNames.get(name);
	}
}
