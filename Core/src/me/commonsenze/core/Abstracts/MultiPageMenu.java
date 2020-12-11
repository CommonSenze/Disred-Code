package me.commonsenze.core.Abstracts;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

import me.commonsenze.core.Lang;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Util.CC;
import net.md_5.bungee.api.ChatColor;

public abstract class MultiPageMenu extends Menu {

	private boolean canContinue;
	private int page, itemSize, totalPages, currentSlot;
	private Integer[] startingSlots, searchingSlots;
	private ArrayList<ItemStack> items;
	private boolean searching;
	private Menu prevMenu, nextMenu;

	public MultiPageMenu(Player player, String name) {
		super(name, player, 54);
		this.items = getItems();
		this.itemSize = items.size();
		this.totalPages = itemSize/35 + (itemSize %35 == 0 ? 0 : 1);
		this.startingSlots = new Integer[totalPages];
		this.searchingSlots = new Integer[totalPages];
	}

	@Override
	public MultiPageMenu create() {
		this.page = 0;
		clear();
		load();
		return this;
	}

	@Override
	public void click(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		if (item == null)return;
		if (item.getType() == Material.STAINED_GLASS_PANE&&item.getDurability() == 8)return;

		if (!item.hasItemMeta()) {
			if (updatable(e))
				update();
			return;
		}

		String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

		if (name == null) {
			System.out.println("ERROR NAME IS NULL FOR SLOT " + e.getSlot()+" on menu "+getName());
			return;
		}

		int prevPage = page;

		if (item.getType() == Material.BOOK_AND_QUILL) {
			delete();
			Menu menu = getNextMenu().create();
			menu.open();
		}

		if (name.contains("NEXT")) {
			page += (canContinue ? 1 : 0);
		} else if (name.contains("BACK")) {
			page += -1;
			if (page < 0) {
				page = 0;
				return;
			}
			currentSlot = startingSlots[page];
		} else if (name.contains("Back")) {
			delete();
			Menu menu = getPreviousMenu().create();
			menu.open();
		} else if (name.equals("Search")&&!searching) {
			this.disableDeleteOnClose();
			getPlayer().closeInventory();
			searching = true;
			getPlayer().sendMessage(Lang.success(ChatColor.YELLOW +"Please enter your search query."));
		} else if (name.contains("Turn Search Off")) {
			searching = false;
			currentSlot = startingSlots[page];
			this.items = getItems();
			this.itemSize = items.size();
			update();
		} else if ((e.getSlot()%9 == 0||e.getSlot()%9==8||e.getSlot() < 9)||!updatable(e))return;

		if (!exists())return;

		if (prevPage == page)return;

		update();
	}

	@Override
	public void update() {
		clear();
		load();
	}

	@EventHandler
	public void chat(PlayerCommandPreprocessEvent event) {
		if (searching) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(Lang.fail("-nYou aren't allowed to perform a command until you give an input for the search."));
		}
	}

	private void load() {
		canContinue = true;
		if (getPreviousMenu() != null)
			setItem(0, new ItemCreation(Material.CHEST).setDisplayName(ChatColor.RED + "Back to "+getPreviousMenu().getName()).toItemStack());

		if (searching)
			setItem(4, new ItemCreation(Material.ANVIL).setDisplayName(ChatColor.RED + "Turn Search Off")
					.addLore(" ")
					.addLore("Click to turn search off.")
					.toItemStack());
		else setItem(4, new ItemCreation(Material.MAP).setDisplayName(ChatColor.GOLD + "Search")
				.addLore(" ")
				.addLore("Click to search for an item.")
				.toItemStack());

		if (getNextMenu() != null)
			setItem(8, new ItemCreation(Material.BOOK_AND_QUILL).setDisplayName(ChatColor.GREEN + getNextMenu().getName())
					.addLore(" ")
					.addLore("Click to up up the "+getNextMenu().getName()+".")
					.toItemStack());

		ItemStack it = new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(" ").setDurability((short)7).toItemStack();
		for (int i = 0; i < getInventory().getSize(); i++) {
			if (getInventory().getItem(i) == null&&(i%9 == 0||i%9==8||i<9))
				setItem(i, it);
		}

		if (searching)
			searchingSlots[page] = currentSlot;
		else startingSlots[page] = currentSlot;

		int slot = 0;

		while (itemSize > 0&&canContinue&&getInventory().firstEmpty() != -1) {
			if (getInventory().getItem(slot) == null) {
				setItem(slot, items.get(currentSlot++));
				canContinue = currentSlot < itemSize;
			} else slot++;
		}
		
		if (itemSize == 0&&searching) {
			for (int i = 0; i < getInventory().getSize(); i++) {
				if (getInventory().getItem(i) == null) {
					setItem(i, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short)14).setDisplayName(CC.DARKRED + "No Results Found")
							.addLore("Click the anvil to exit search.").toItemStack());
				}
			}
		}

		if (page != 0)
			setItem(27, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short)14).setDisplayName(ChatColor.RED + ">   BACK   <")
					.addLore(" ")
					.addLore("Page " + (page)+"/"+totalPages)
					.toItemStack());

		if((page+1) < totalPages)
			setItem(35, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short)5).setDisplayName(ChatColor.GREEN + ">   NEXT   <")
					.addLore(" ")
					.addLore("Page " + (page+2)+"/"+totalPages)
					.toItemStack());
		fill(8);
	}

	@EventHandler
	public void chat(AsyncPlayerChatEvent e) {
		if (searching&&e.getPlayer().equals(getPlayer())) {
			e.setCancelled(true);
			search(e.getMessage().toLowerCase());
			open();
			this.enableDeleteOnClose();
		}
	}

	public void search(String string) {
		clear();
		items = getSearchQuery(string);
		itemSize = items.size();
		currentSlot = 0;
		load();
	}

	public final Menu getPreviousMenu() {
		return prevMenu;
	}

	public Menu getNextMenu() {
		return nextMenu;
	}

	public void setNextMenu(Menu nextMenu) {
		this.nextMenu = nextMenu;
	}

	public void setPreviousMenu(Menu prevMenu) {
		this.prevMenu = prevMenu;
	}

	public abstract ArrayList<ItemStack> getItems();

	public abstract ArrayList<ItemStack> getSearchQuery(String name);

	public abstract boolean updatable(InventoryClickEvent e);
}
