package me.commonsenze.core.Objects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import net.md_5.bungee.api.ChatColor;

public class ItemCreation implements Cloneable {

	private ItemStack item;

	public ItemCreation(PotionType type, int level) {
		this.item = new Potion(type, level).toItemStack(1);
	}
	
	public ItemCreation(Material material) {
		this.item = new ItemStack(material);
	}

	public ItemCreation(ItemStack item) {
		this.item = new ItemStack(item);
	}

	public ItemCreation setDisplayName(String name) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return this;
	}

	public ItemCreation setOwner(String name) {
		if (!(item.getItemMeta() instanceof SkullMeta))return this;
		item.setDurability((short)SkullType.PLAYER.ordinal());
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.setOwner(name);
		item.setItemMeta(meta);
		return this;
	}
	
	public ItemCreation setHasExtendedDuration(boolean extendedDuration) {
		if (item.getType() != Material.POTION)return this;
		Potion potion = Potion.fromItemStack(item);
		potion.setHasExtendedDuration(extendedDuration);
		ItemMeta meta = item.getItemMeta();
		item = potion.toItemStack(1);
		item.setItemMeta(meta);
		return this;
	}
	
	public ItemCreation setPotionType(PotionType type) {
		if (item.getType() != Material.POTION)return this;
		Potion potion = Potion.fromItemStack(item);
		potion.setType(type);
		ItemMeta meta = item.getItemMeta();
		item = potion.toItemStack(1);
		item.setItemMeta(meta);
		return this;
	}
	
	public ItemCreation setPotionLevel(int level) {
		if (item.getType() != Material.POTION)return this;
		Potion potion = Potion.fromItemStack(item);
		potion.setLevel(level);
		ItemMeta meta = item.getItemMeta();
		item = potion.toItemStack(1);
		item.setItemMeta(meta);
		return this;
	}
	
	public ItemCreation setSplash(boolean splash) {
		if (item.getType() != Material.POTION)return this;
		Potion potion = Potion.fromItemStack(item);
		potion.setSplash(splash);
		ItemMeta meta = item.getItemMeta();
		item = potion.toItemStack(1);
		item.setItemMeta(meta);
		return this;
	}

	public ItemCreation addLore(String message) {
		if (message.contains("-n")) {
			for (String msg : message.split("-n")) 
				addLore(msg);
			return this;
		}
		if (message.isEmpty())return this;
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		if (meta.getLore() != null) lore.addAll(meta.getLore());
		lore.add(ChatColor.GRAY + message);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return this;
	}
	
	public ItemCreation replaceLoreLine(int line, String message) {
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		if (meta.getLore() != null) lore.addAll(meta.getLore());
		if (lore.size() < line)return this;
		else if (0 > line)return this;
		else if (lore.size() == line)
			lore.add(ChatColor.GRAY + message);
		else lore.set(line, ChatColor.GRAY + message);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return this;
	}

	public ItemCreation insertLoreLine(int line, String message) {
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		if (meta.getLore() != null) lore.addAll(meta.getLore());
		if (lore.size() < line)return this;
		if (0 > line)return this;
		if (lore.size() == line)
			lore.add(ChatColor.GRAY + message);
		else lore.add(line, ChatColor.GRAY + message);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return this;
	}

	public ItemCreation setAmount(int amount) {
		item.setAmount(amount);
		return this;
	}

	public ItemCreation setDurability(short durability) {
		item.setDurability(durability);
		return this;
	}
	
	public ItemCreation hideEnchants() {
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		return this;
	}
	
	public ItemCreation hidePotionLore() {
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		item.setItemMeta(meta);
		return this;
	}

	public ItemStack toItemStack() {
		return item;
	}

	public ItemCreation addLores(List<String> lore) {
		for (String string: lore)
			addLore(string);
		return this;
	}

	public ItemCreation addEnchantment(Enchantment ench, int level) {
		item.addUnsafeEnchantment(ench, level);
		return this;
	}
	
	public ItemCreation clone() {
		return new ItemCreation(item);
	}
}
