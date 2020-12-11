package me.commonsenze.kitpvp.Objects;

import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.mongodb.client.model.Filters;

import lombok.Getter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.kitpvp.KitPvP;
import me.commonsenze.kitpvp.Managers.impl.UserManager;
import me.commonsenze.wrapper.Packets.WrapperPlayServerTitle;

@Getter
public class User {

	private Player player;
	private UUID uniqueId;
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
		KitPvP.getInstance().getManagerHandler().getUserManager().sendToSpawn(getPlayer());
	}
	
	private void load(Document document) {
		
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
}
