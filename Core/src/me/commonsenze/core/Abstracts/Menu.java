package me.commonsenze.core.Abstracts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Objects.Profile;
import net.md_5.bungee.api.ChatColor;

public abstract class Menu implements Listener {

	private String name;
	private Inventory inventory;
	private UUID uuid;
	protected boolean deleteOnClose = true, exists = false, editable;
	@Setter private boolean multipleInputs;
	@Getter @Setter private boolean uncloseable;

	public Menu(String name, Player player, int size) {
		this.name = ChatColor.stripColor(name);
		this.uuid = Core.getInstance().getCache().getUUID(player.getName());
		this.inventory = Bukkit.createInventory(player, size, name);
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public String getName() {
		return name;
	}

	public void disableDeleteOnClose() {
		deleteOnClose = false;
	}

	public void enableDeleteOnClose() {
		deleteOnClose = true;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public boolean allowsMultipleInputs() {
		return multipleInputs;
	}

	public void clear() {
		this.inventory.clear();
	}

	public void addItem(ItemStack item) {
		this.inventory.addItem(item);
	}

	public void setItem(int slot, ItemStack item) {
		this.inventory.setItem(slot, item);
	}

	public ItemStack getItem(int slot) {
		return this.inventory.getItem(slot);
	}

	public void open() {
		if (!exists) {
			Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(getPlayer());
			if (profile.hasMenuOpened()) {
				profile.getPlayer().closeInventory();
			}
			profile.setMenuOpened(true);
			exists = true;
			Bukkit.getPluginManager().registerEvents(this, Core.getInstance());
		}
		getPlayer().openInventory(getInventory());
	}
	
	public void addViewer(Player player) {
		Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(player);
		if (profile.hasMenuOpened()) {
			profile.getPlayer().closeInventory();
		}
		profile.setMenuOpened(true);
		profile.getPlayer().openInventory(getInventory());
	}

	public void delete() {
		exists = false;
		new HashSet<>(getInventory().getViewers()).forEach(human -> human.closeInventory());
		removeMenu();
	}

	public void removeMenu() {
		Core.getInstance().getManagerHandler().getProfileManager().getProfile(getPlayer()).setMenuOpened(false);
		HandlerList.unregisterAll(this);
	}

	public boolean exists() {
		return exists;
	}

	public boolean isEditable() {
		return editable;
	}
	
	public void confirm(Player player, Consumer<Boolean> action, String accept, String deny) {
		Menu menu = new Menu("Confirmation", player, 9) {

			@Override
			public Menu create() {
				this.setUncloseable(true);
				ItemStack def = new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short) 8).setDisplayName("").toItemStack();
				setItem(2,def);
				setItem(3, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short) 5).setDisplayName(ChatColor.GREEN + accept).toItemStack());
				setItem(4,def);
				setItem(5, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short) 14).setDisplayName(ChatColor.RED + deny).toItemStack());
				setItem(6,def);
				return this;
			}

			@Override
			public void update() {}

			@Override
			public void click(InventoryClickEvent e) {
				if (!e.getCurrentItem().hasItemMeta()||e.getCurrentItem().getItemMeta().getDisplayName().isEmpty())return;
				this.setUncloseable(false);
				String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
				this.delete();
				action.accept(name.contains(accept));
			}
		}.create();
		menu.open();
	}

	@EventHandler
	public void event(InventoryClickEvent event) {
		if (!exists())
			removeMenu();
		if (!event.getWhoClicked().equals(getPlayer())&&!allowsMultipleInputs())
			return;
		if (!event.getView().getTopInventory().equals(event.getClickedInventory()))return;
		if (event.getClickedInventory().getTitle() != getName())return;
		event.setCancelled(!isEditable());
		ItemStack item = event.getCurrentItem();
		if (item == null)
			return;
		click(event);
	}
	
	@EventHandler
	public void close(InventoryCloseEvent event) {
		System.out.println("CLOSING MENU: "+event.getInventory().getTitle() + " for: "+event.getPlayer().getName() +" Uncloseable? "+isUncloseable());
		if (!event.getPlayer().equals(getPlayer())&&!getInventory().getViewers().contains(event.getPlayer()))
			return;
		if (isUncloseable()) {
			Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
				open();
			}, 2);
			return;
		}
		Core.getInstance().getManagerHandler().getProfileManager().getProfile(getPlayer()).setMenuOpened(false);
		if (deleteOnClose&&exists()) {
			removeMenu();
		}
	}

	protected void fill(int color) {
		ItemStack item = new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(" ").setDurability((short) color)
				.toItemStack();
		for (int i = 0; i < getInventory().getSize(); i++) {
			if (getInventory().getItem(i) == null)
				setItem(i, item);
		}
	}

	protected ArrayList<Integer> format(int amount) {
		if (amount == 1) {
			return new ArrayList<>(Arrays.asList(4));
		}
		if (amount == 2) {
			return new ArrayList<>(Arrays.asList(2, 6));
		}
		if (amount == 3) {
			return new ArrayList<>(Arrays.asList(2, 4, 6));
		}
		if (amount == 4) {
			return new ArrayList<>(Arrays.asList(1, 3, 5, 7));
		}
		if (amount == 5) {
			return new ArrayList<>(Arrays.asList(0, 2, 4, 6, 8));
		}
		ArrayList<Integer> ints = new ArrayList<>();
		for (int i = 1, slot = 1; slot <= amount; i++) {
			if (i % 9 == 0 && i % 8 == 0)
				continue;
			ints.add(slot);
			slot++;
		}
		return ints;
	}

	public abstract Menu create();

	public abstract void update();

	public abstract void click(InventoryClickEvent e);
}
