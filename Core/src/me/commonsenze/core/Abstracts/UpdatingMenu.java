package me.commonsenze.core.Abstracts;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

import me.commonsenze.core.Core;

public abstract class UpdatingMenu extends Menu implements Runnable {

	private int id;
	
	public UpdatingMenu(String name, Player player, int size) {
		super(name, player, size);
		id = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getInstance(), this, 0, this.updateRate()).getTaskId();
	}
	
	@Override
	public void run() {
		update();
	}
	
	@EventHandler
	public void closeMenu(InventoryCloseEvent e) {
		if (!e.getPlayer().equals(getPlayer()))return;
		if (deleteOnClose) {
			Bukkit.getScheduler().cancelTask(this.id);
			Core.getInstance().getManagerHandler().getProfileManager().getProfile(getPlayer()).setMenuOpened(false);
			getPlayer().closeInventory();
			super.removeMenu();
		}
	}
	
	public abstract int updateRate();
}
