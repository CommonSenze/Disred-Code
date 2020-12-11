package me.commonsenze.core.Menus;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.commonsenze.core.Core;
import me.commonsenze.core.Abstracts.Menu;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Util.CC;
import net.md_5.bungee.api.ChatColor;

public class CoreSettingsMenu extends Menu {

	public CoreSettingsMenu(Player player) {
		super("Core Settings Menu", player, 45);
	}

	@Override
	public Menu create() {
		Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(getPlayer());
		
		setItem(4, new ItemCreation(Material.SKULL_ITEM).setDisplayName(CC.GOLD + "Settings")
				.addLore(" ")
				.addLore("Click the icons to")
				.addLore("change that respective setting.")
				.setDurability((short) SkullType.PLAYER.ordinal())
				.setOwner(getPlayer().getName())
				.toItemStack());
		
		setItem(11, new ItemCreation(Material.PAPER).setDisplayName(CC.GOLD + "Chat"+CC.WHITE +": " + 
				(profile.hasChat() ? CC.GREEN + "Enabled":CC.RED + "Disabled"))
				.addLore("Toggle public chat.")
				.toItemStack());
		
		setItem(15, new ItemCreation(Material.BOOK_AND_QUILL).setDisplayName(CC.GOLD + "Private Messaging"+CC.WHITE +": " + 
				(profile.isMessageable() ? CC.GREEN + "Enabled":CC.RED + "Disabled"))
				.addLore("Toggle private messages.")
				.toItemStack());
		
		setItem(22, new ItemCreation(Material.PAINTING).setDisplayName(CC.GOLD + "Scoreboard"+CC.WHITE +": " + 
				(profile.hasScoreboardVisibility() ? CC.GREEN + "Enabled":CC.RED + "Disabled"))
				.addLore("Toggle scoreboard.")
				.toItemStack());
		
		setItem(29, new ItemCreation(Material.SKULL_ITEM).setDisplayName(CC.GOLD + "Friends")
				.addLore("View all your friends.")
				.setDurability((short)SkullType.PLAYER.ordinal())
				.toItemStack());
		
		setItem(33, new ItemCreation(Material.NAME_TAG).setDisplayName(CC.GOLD + "Tag Selector")
				.addLore("View all your friends.")
				.toItemStack());
		
		fill(8);
		getPlayer().updateInventory();
		return this;
	}

	@Override
	public void update() {
		create();
	}

	@Override
	public void click(InventoryClickEvent e) {
		Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(getPlayer());
		ItemStack item = e.getCurrentItem();
		String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

		boolean value = !name.contains("Enabled");

		if (name.contains("Chat")) {
			profile.setHasChat(value);
		} else if (name.contains("Scoreboard")) {
			profile.setHasScoreboard(value);
		} else if (name.contains("Private Messaging")) {
			profile.setMessageable(value);
		} else return;
		
		update();
	}

}
