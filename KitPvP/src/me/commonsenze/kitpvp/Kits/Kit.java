package me.commonsenze.kitpvp.Kits;

import java.util.List;

import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import me.commonsenze.kitpvp.KitPvP;

public interface Kit extends Listener {

	public enum PvPType {
		SOUP, POT, SURVIVAL;
	}
	
	default String getPermission() {
		return KitPvP.getInstance().getConfig("permissions").getConfig().getString("kits","kits.%kit%").replaceAll("%kit%", getName().toLowerCase());
	}
	
	String getName();
	ItemStack getIcon(boolean hasOwnership);
	List<PotionEffect> getPotionEffects();
	ItemStack[] getArmor();
	ItemStack[] getContents(PvPType type);
	void equipped
}
