package me.commonsenze.minigames.Games;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Objects.User;

public interface Game extends Listener {

	String FOLDER = "Games"+File.separator;
	
	public enum QuitReason {
		DISCONNECT, LEAVE, DEATH, GAME_OVER, CUSTOM;
	}
	
	public default void forEachUser(Consumer<? super User> action) {
		getUsers().stream().filter(user -> user.isOnline()).forEach(action);
	}
	
	public default Stream<User> filter(Predicate<? super User> filter){
		return getUsers().stream().filter(filter);
	}
	
	public default boolean isPlaying(User user) {
		return user.isOnline()&&!user.isSpectating();
	}
	
	public default boolean isStarted() {
		return getGameState() != GameState.LOBBY;
	}
	
	public default boolean isGaming(Player player) {
		return isGaming(CoreAPI.getInstance().getCache().getUUID(player.getName()));
	}

	public default boolean isGaming(UUID uuid) {
		return getUsers().contains(getUser(uuid));
	}
	
	public default void broadcast(String message) {
		forEachUser(user -> user.getPlayer().sendMessage(message));
	}
	
	public default User getUser(UUID uuid) {
		return Minigames.getInstance().getManagerHandler().getUserManager().getUser(uuid);
	}
	
	public default User getUser(Player player) {
		return getUser(player.getUniqueId());
	}
	
	int getMaxPlayers();
	int getRequiredPlayers();
	String getId();
	GameState getGameState();
	void join(Player player);
	void quit(Player player, Game.QuitReason reason);
	Set<User> getUsers();
	long getStartTime();
	void start();
	void setGameSpawn(User user);
	Location getLobbySpawn();
	void end();
	String getTodoList();
	boolean isReady();
	void refresh();
}
