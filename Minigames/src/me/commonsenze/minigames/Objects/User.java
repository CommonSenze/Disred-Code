package me.commonsenze.minigames.Objects;

import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.mongodb.client.model.Filters;

import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.Game;
import me.commonsenze.minigames.Managers.impl.UserManager;
import me.commonsenze.wrapper.Packets.WrapperPlayServerTitle;

@Getter
public class User {

	@Setter private Game currentGame;
	private Player player;
	private UUID uniqueId;
	@Setter private Location gameSpawn;
	private boolean spectating;
	private UserManager userManager;
	
	public User(Player player, UserManager userManager) {
		this.player = player;
		this.uniqueId = CoreAPI.getInstance().getCache().getUUID(player.getName());
		
		Document document = userManager.getUsersCollection().find(Filters.eq("uuid", this.uniqueId.toString())).first();
		if (document == null)
			document = registerUser();
		load(document);
	}
	
	private Document registerUser() {
		Document document = new Document("uuid", uniqueId.toString());
		return document;
	}
	
	public void spawn() {
		Minigames.getInstance().getManagerHandler().getUserManager().sendToSpawn(getPlayer());
	}
	
	private void load(Document document) {
		
	}
	
	public void setSpectating(boolean spectating) {
		this.spectating = spectating;
		if (spectating)
			getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999*20, 0));
		else getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
	}

	public Document createDocument() {
		Document document = new Document("uuid", this.getUniqueId().toString());
		return document;
	}

	public void sendTitle(TitleAction titleAction, String string) {
		WrapperPlayServerTitle title = new WrapperPlayServerTitle();
		title.setAction(titleAction);
		title.setFadeIn(10);
		title.setStay(40);
		title.setFadeOut(10);
		title.setTitle(WrappedChatComponent.fromText(string));
		title.sendPacket(getPlayer());
	}

	public boolean isOnline() {
		return Bukkit.getPlayer(getUniqueId()) != null;
	}

	public boolean inGame() {
		return getCurrentGame() != null;
	}
}
