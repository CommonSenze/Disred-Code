package me.commonsenze.minigames.Games.Skywars.Objects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Util.CC;

public class ChestItems {

	private Map<Type, List<ItemStack>> items;

	public ChestItems() {
		this.items = new HashMap<>();
		load();
	}

	private void load() {
		this.items.put(Type.BUILD, Arrays.asList(
				new ItemCreation(Material.WOOD).setAmount(24).toItemStack(),
				new ItemCreation(Material.STONE).setAmount(16).toItemStack(),
				new ItemCreation(Material.LOG).setAmount(4).toItemStack()
				));
		this.items.put(Type.WEAPON, Arrays.asList(
				new ItemCreation(Material.STONE_SWORD).setAmount(1).toItemStack(),
				new ItemCreation(Material.BOW).setAmount(1).toItemStack(),
				new ItemCreation(Material.WOOD_SWORD).setAmount(1).toItemStack()
				));
		this.items.put(Type.SUPPORT, Arrays.asList(
				new ItemCreation(Material.ENDER_PEARL).setAmount(1).toItemStack(),
				new ItemCreation(Material.GOLD_LEGGINGS).setAmount(1).toItemStack(),
				new ItemCreation(Material.CHAINMAIL_CHESTPLATE).setAmount(1).toItemStack(),
				new ItemCreation(Material.DIAMOND_HELMET).setAmount(1).toItemStack(),
				new ItemCreation(Material.LEATHER_CHESTPLATE).setAmount(1).toItemStack(),
				new ItemCreation(Material.IRON_BOOTS).setAmount(1).toItemStack(),
				new ItemCreation(Material.SNOW_BALL).setAmount(10).toItemStack(),
				new ItemCreation(Material.ARROW).setAmount(7).toItemStack()
				));
	}

	public void upgradeLoot() {
		this.items.put(Type.BUILD, Arrays.asList(
				new ItemCreation(Material.WOOD).setAmount(24).toItemStack(),
				new ItemCreation(Material.STONE).setAmount(16).toItemStack(),
				new ItemCreation(Material.EGG).setDisplayName(CC.GOLD + "Bridge Builder Egg").setAmount(3).toItemStack(),
				new ItemCreation(Material.LOG).setAmount(4).toItemStack()
				));
		this.items.put(Type.WEAPON, Arrays.asList(
				new ItemCreation(Material.IRON_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 1).setAmount(1).toItemStack(),
				new ItemCreation(Material.BOW).addEnchantment(Enchantment.ARROW_DAMAGE, 1).setAmount(1).toItemStack(),
				new ItemCreation(Material.DIAMOND_SWORD).setAmount(1).toItemStack()
				));
		this.items.put(Type.SUPPORT, Arrays.asList(
				new ItemCreation(Material.ENDER_PEARL).setAmount(2).toItemStack(),
				new ItemCreation(Material.DIAMOND_LEGGINGS).setAmount(1).toItemStack(),
				new ItemCreation(Material.CHAINMAIL_LEGGINGS).setAmount(1).toItemStack(),
				new ItemCreation(Material.IRON_CHESTPLATE).setAmount(1).toItemStack(),
				new ItemCreation(Material.EGG).setAmount(6).toItemStack(),
				new ItemCreation(Material.CHAINMAIL_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).setAmount(1).toItemStack(),
				new ItemCreation(Material.SNOW_BALL).setAmount(10).toItemStack(),
				new ItemCreation(Material.COMPASS).setDisplayName(CC.GREEN + "Player Finder "+CC.GRAY + "(Right Click)").setAmount(1).toItemStack(),
				new ItemCreation(Material.ARROW).setAmount(16).toItemStack()
				));
	}

	public void customInput(Type type, List<ItemStack> stack) {
		this.items.put(type, stack);
	}

	public ItemStack byType(Type type) {
		Random random = new Random();
		ItemStack item = this.items.get(type).get(random.nextInt(this.items.get(type).size()));
		item.setAmount(random.nextInt(item.getAmount())+1);
		return item;
	}

	@Getter
	public enum Type {
		BUILD(4), WEAPON(3), SUPPORT(2);

		private int maxAllowed;
		Type(int maxAllowed){
			this.maxAllowed = maxAllowed;
		}
	}
}
