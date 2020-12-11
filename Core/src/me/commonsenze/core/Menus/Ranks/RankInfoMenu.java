package me.commonsenze.core.Menus.Ranks;

import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Menu;
import me.commonsenze.core.Menus.RankListMenu;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Objects.Rank;
import me.commonsenze.core.Util.CC;
import net.md_5.bungee.api.ChatColor;

public class RankInfoMenu extends Menu {

	@Getter private Rank rank, prevRank;
	private boolean editingPrefix,editingTab, editingRanking, deleting;

	public RankInfoMenu(Player player, Rank rank) {
		super(rank.getName() + "'s Info", player, 27);
		this.prevRank = rank.clone();
		this.rank = rank;
	}

	private RankInfoMenu(Player player, Rank rank, Rank prevRank) {
		this(player, rank);
		this.prevRank = prevRank;
	}

	@Override
	public Menu create() {
		setItem(0, new ItemCreation(Material.REDSTONE).setDisplayName(ChatColor.RED + "Back to Rank List").toItemStack());
		ItemCreation permissions = new ItemCreation(Material.EYE_OF_ENDER).setDisplayName(CC.DARKAQUA + "Permissions");
		ItemCreation users = new ItemCreation(Material.SKULL_ITEM).setDurability((short)SkullType.SKELETON.ordinal()).setDisplayName(CC.DARKAQUA + "Rank Users");

		int maxCount = 6;

		if (!rank.getPermissions().isEmpty()) {
			for (int i = 0; i < rank.getPermissions().size(); i++) {
				if (i == maxCount) {
					permissions.addLore(" ");
					permissions.addLore(CC.AQUA + "Click to view more...");
					break;
				}
				String string = rank.getPermissions().get(i);
				permissions.addLore("- "+CC.GREEN + string);
			}
		} else {
			permissions.addLore(CC.RED + "Currently none. /"+CC.DARKRED+"rank "+rank.getName() + " add <permission>");
			permissions.addLore(CC.RED+"to add more.");
		}

		if (!rank.getUsers().isEmpty()) {
		for (int i = 0; i < rank.getUsers().size(); i++) {
			if (i == maxCount) {
				users.addLore(" ");
				users.addLore(CC.AQUA + "Click to view more...");
				break;
			}
			UUID uuid = rank.getUsers().get(i);
			users.addLore("- "+CC.GREEN + Bukkit.getOfflinePlayer(uuid).getName());
		}
	} else {
		users.addLore(CC.RED + "Currently none. /"+CC.DARKRED+"rank "+rank.getName() + " set <player>");
		users.addLore(CC.RED+"to set someone's rank.");
	}

		setItem(4, new ItemCreation(Material.RECORD_10).setDisplayName(CC.PINK + "Ranking: "+CC.WHITE + rank.getRanking())
				.addLore("Click to edit ranking.")
				.toItemStack());
		setItem(5, new ItemCreation(Material.RECORD_9).setDisplayName(CC.PINK + "Prefix: "+CC.WHITE + rank.getPrefix())
				.addLore("Click to edit prefix.")
				.toItemStack());
		setItem(6, new ItemCreation(Material.RECORD_12).setDisplayName(CC.PINK + "Tab Color: "+CC.WHITE + CC.getColorName(ChatColor.getByChar(rank.getTabColor().charAt(1))))
				.addLore("Click to edit tab color.")
				.toItemStack());
		setItem(7, new ItemCreation(Material.EMERALD).setDisplayName(CC.PINK + "Default: "+CC.WHITE + (rank.isDefault() ? CC.GREEN + "True" : CC.RED + "False"))
				.addLore("Click to toggle whether or not")
				.addLore(rank.getName()+" is the default rank.")
				.toItemStack());

		setItem(10, permissions.toItemStack());
		setItem(12, users.toItemStack());

		setItem(26, new ItemCreation(Material.DROPPER).setDisplayName(CC.RED + "Delete "+rank.getName())
				.addLore("Click to delete the "+rank.getName()+" rank.")
				.toItemStack());
		fill(7);
		return this;
	}

	@Override
	public void update() {
		clear();
		create();
	}

	@Override
	public void removeMenu() {
		super.removeMenu();
		if (deleting) {
			Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
				confirm(getPlayer(), value -> {
					if (value) {
						this.deleting = false;
						Core.getInstance().getManagerHandler().getRankManager().deleteRank(rank.getUUID());
						getPlayer().sendMessage(Lang.success("-nDeleted rank -e"+rank.getName()+"-n."));
					} else {
						this.deleting = false;
						getPlayer().sendMessage(Lang.success("-nUndid delete of rank -e"+rank.getName()+"-n."));
						Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
							Menu menu = new RankInfoMenu(getPlayer(), rank, prevRank).create();
							menu.open();
						}, 2);
					}
				}, "Confirm Delete", "Undo Delete");
			}, 2);
		} else if (!rank.equals(prevRank)) {
			Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
				confirm(getPlayer(), value -> {
					if (value) {
						getPlayer().sendMessage(Lang.success("-nSaving changes to -e"+rank.getName()+"-n."));
						if (rank.isDefault())
							Core.getInstance().getManagerHandler().getRankManager().setDefaultRank(rank);
					} else {
						rank.copy(prevRank);
						getPlayer().sendMessage(Lang.success("-nReverting changes from -e"+rank.getName()+"-n."));
					}
				}, "Save Changes", "Discard Changes");
			}, 2);
		}
	}

	@Override
	public void click(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
		if (name.contains("Prefix")) {
			disableDeleteOnClose();
			e.getWhoClicked().closeInventory();
			e.getWhoClicked().sendMessage(CC.YELLOW + "Please send the new prefix name.");
			editingPrefix = true;
		} else if (name.contains("Ranking")) {
			disableDeleteOnClose();
			e.getWhoClicked().closeInventory();
			e.getWhoClicked().sendMessage(CC.YELLOW + "Please send the new ranking.");
			editingRanking = true;
		} else if (name.contains("Tab Color")) {
			disableDeleteOnClose();
			e.getWhoClicked().closeInventory();
			e.getWhoClicked().sendMessage(CC.YELLOW + "Please send the new tab color.");
			editingTab = true;
		}
		if (name.contains("Default")) {
			rank.setDefault(!rank.isDefault());
			update();
			getPlayer().sendMessage(Lang.success("-nYou made -e"+rank.getName()+"-n's default status to "+rank.isDefault()+"."));
		}

		if (name.contains("Delete")) {
			this.deleting = true;
			delete();
		}
		if (name.equalsIgnoreCase("Back to Rank List")) {
			delete();
			if (rank.equals(prevRank)) {
				Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
					Menu menu = new RankListMenu(getPlayer()).create();
					menu.open();
				}, 2);
			}
		}
	}

	@EventHandler
	public void chat(AsyncPlayerChatEvent event) {
		if (!event.getPlayer().equals(getPlayer()))return;
		String message = event.getMessage();
		if (editingPrefix) {
			event.setCancelled(true);
			if (message.equalsIgnoreCase("\"\"")) message = "";
			rank.setPrefix(message);
			editingPrefix = false;
			Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
				Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
					update();
					open();
					enableDeleteOnClose();
				}, 3);
				event.getPlayer().sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s prefix: [" + rank.getPrefix() + CC.AQUA + "]");
			}, 1);
		}
		if (editingRanking) {
			event.setCancelled(true);
			if (!NumberUtils.isNumber(message)) {
				event.getPlayer().sendMessage(Lang.fail("-e"+message + " -nis not a valid number."));
				return;
			}
			rank.setRanking(Integer.parseInt(message));
			editingRanking = false;
			Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
				Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
					update();
					open();
					enableDeleteOnClose();
				}, 3);
				event.getPlayer().sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s ranking: [" + rank.getRanking() + CC.AQUA + "]");
			}, 1);
		}
		if (editingTab) {
			event.setCancelled(true);
			if (!CC.isChatColor(message)) {
				event.getPlayer().sendMessage(Lang.fail("-e"+message + " -nis not a valid chat color."));
				return;
			}
			if (message.equalsIgnoreCase("\"\"")) message = "&f";
			rank.setTabColor(message);
			editingTab = false;
			Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
				Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
					update();
					open();
					enableDeleteOnClose();
				}, 3);
				event.getPlayer().sendMessage(CC.WHITE + rank.getName() + CC.AQUA + "'s tab color: [" + rank.getTabColor()+ CC.getColorName(ChatColor.getByChar(rank.getTabColor().charAt(1))) + CC.AQUA + "]");
			}, 1);
		}
	}

	@EventHandler
	public void chat(PlayerCommandPreprocessEvent event) {
		if (editingPrefix||editingRanking||editingTab) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(Lang.fail("-nYou aren't allowed to perform a command until you give an input for the "+rank.getName()+" rank."));
		}
	}
}
