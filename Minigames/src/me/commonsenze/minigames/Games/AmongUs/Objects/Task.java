package me.commonsenze.minigames.Games.AmongUs.Objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Abstracts.Menu;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Objects.Cuboid;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.AmongUs.AmongUs;
import me.commonsenze.minigames.Objects.User;
import me.commonsenze.minigames.Util.EntityHider;
import me.commonsenze.minigames.Util.EntityHider.Policy;

@Getter
public class Task implements Listener {

	private UUID uuid;
	private Type type;
	private boolean completed, completing, partedTask;
	private int amount, finishedAmount;
	private Set<Entity> spawnedMobs;
	private Set<Item> items;
	private EntityHider entityHider;

	@Getter private static Editor editor;

	static {
		editor = Minigames.getInstance().getConfig(AmongUs.FOLDER+"Task");
	}

	public Task(UUID uuid, Difficulty difficulty) {
		List<Type> list = Type.byDifficulty(difficulty);
		this.uuid = uuid;
		this.type = list.get(new Random().nextInt(list.size()));
		this.spawnedMobs = new HashSet<>();
		this.items = new HashSet<>();
		this.entityHider = new EntityHider(Minigames.getInstance(), Policy.BLACKLIST);
		Bukkit.getPluginManager().registerEvents(this, Minigames.getInstance());

		switch (type) {
		case COLLECT_IRON:
		case SHOOT_MOBS:
			this.partedTask = true;
		default:
			break;
		}
	}

	public boolean inTaskRegion(Player player) {
		return getBounds().contains(player.getLocation());
	}

	public Cuboid getBounds() {
		return Minigames.getGson().fromJson(editor.getConfig().getString(type.name() +".area"), new TypeToken<Cuboid>() {}.getType());
	}

	public static void setTaskRegion(Type type, Cuboid cuboid) {
		editor.getConfig().set(type.name() +".area", Minigames.getGson().toJson(cuboid));
		editor.saveConfig();
	}

	public void markCompleted() {
		this.completed = true;
		
		UserTaskCompleteEvent event = new UserTaskCompleteEvent(Minigames.getInstance().getManagerHandler().getUserManager().getUser(uuid), this);
		Bukkit.getPluginManager().callEvent(event);
		
		end();
	}
	
	public void end() {
		removeItems(Bukkit.getPlayer(uuid));
		HandlerList.unregisterAll(this);
	}

	private boolean giveItems(Player player) {
		switch (type) {
		case THROW_TRASH:
			player.getInventory().setItem(4, new ItemCreation(Material.EMERALD).toItemStack());
			return true;
		case COLLECT_IRON: {
			finishedAmount = 10;
			Random random = new Random();
			Cuboid cube = Minigames.getGson().fromJson(editor.getConfig().getString(type.name() +".area"), new TypeToken<Cuboid>() {}.getType());
			for (int i = 0; i < finishedAmount-amount; i++) {
				Location location = null;
				long surrounding = 0;
				do {
					int x = random.nextInt(cube.getSizeX())+cube.getLowerX();
					int y = cube.getLowerY()+2;
					int z = random.nextInt(cube.getSizeZ())+cube.getLowerZ();
					location = new Location(cube.getWorld(),x,y,z);
					Location loc = location;
					surrounding = items.stream().filter(item -> item.getLocation().distanceSquared(loc)<1).count();
				} while (surrounding>0);
				items.add(cube.getWorld().dropItemNaturally(location, new ItemCreation(Material.IRON_INGOT).toItemStack()));
			}
		}
		case BREAK_BLOCK:
			return true;
		case SHOOT_MOBS:
			finishedAmount = 10;
			Random random = new Random();
			Cuboid cube = Minigames.getGson().fromJson(editor.getConfig().getString(type.name() +".mobarea"), new TypeToken<Cuboid>() {}.getType());
			for (int i = 0; i < finishedAmount-amount; i++) {
				int x = random.nextInt(cube.getSizeX()-1)+cube.getLowerX()+1;
				int y = cube.getLowerY()+2;
				int z = random.nextInt(cube.getSizeZ()-1)+cube.getLowerZ()+1;
				Creeper entity = (Creeper)cube.getWorld().spawnEntity(new Location(cube.getWorld(),x,y,z), EntityType.CREEPER);
				entity.setCustomName(player.getName()+"'s Mob");
				entity.setCustomNameVisible(true);
				entity.setTarget(player);
				spawnedMobs.add(entity);
				Minigames.getInstance().getManagerHandler().getUserManager().getUser(player).getCurrentGame().forEachUser(user -> {
					if (!player.equals(user.getPlayer()))
						getEntityHider().hideEntity(user.getPlayer(), entity);
				});
			}

			player.getInventory().setItem(4, new ItemCreation(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE, 1).toItemStack());
			player.getInventory().setItem(9, new ItemCreation(Material.ARROW).toItemStack());
			return true;
		default:
			return false;
		}
	}

	private void removeItems(Player player) {
		switch (type) {
		case THROW_TRASH:
			player.getInventory().setItem(4, null);
			break;
		case SHOOT_MOBS:
			player.getInventory().setItem(4, null);
			player.getInventory().setItem(9, null);
			for (Entity entity : spawnedMobs) {
				if (!entity.isDead())
					entity.remove();
			}
			spawnedMobs.clear();
			break;
		case COLLECT_IRON:
			for (Item entity : items) {
				if (!entity.isDead())
					entity.remove();
			}
			items.clear();
			player.getInventory().remove(Material.IRON_INGOT);
			break;
		default:
			break;
		}
	}

	@EventHandler
	public void move(PlayerMoveEvent event) {
		if (!uuid.equals(CoreAPI.getInstance().getCache().getUUID(event.getPlayer().getName())))return;
		if (isCompleted())return;
		if (inTaskRegion(event.getPlayer())&&!isCompleting()&&giveItems(event.getPlayer())) {
			this.completing = true;
		} else if (!inTaskRegion(event.getPlayer())&&isCompleting()) {
			this.completing = false;
			removeItems(event.getPlayer());
		}
	}

	@EventHandler
	public void interact(PlayerInteractEvent event) {
		if (!uuid.equals(CoreAPI.getInstance().getCache().getUUID(event.getPlayer().getName())))return;
		if (isCompleted())return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)return;
		if (event.getClickedBlock().getType() != Material.STONE_BUTTON)return;
		if (inTaskRegion(event.getPlayer())&&!isCompleting()) {
			switch (type) {
			case CLICK_MENU:
				new Menu("Click Items", event.getPlayer(), 36) {

					@Override
					public void update() {}

					@Override
					public Menu create() {
						Random random = new Random();

						List<Integer> slots = new ArrayList<>();

						for (int i = 0; i < 5; i++) {
							int slot = random.nextInt(this.getInventory().getSize());
							while (slots.contains(slot)) {
								slot = random.nextInt(this.getInventory().getSize());
							}
							slots.add(slot);
						}

						for (int i = 0; i < slots.size(); i++) {
							setItem(slots.get(i), new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short)5)
									.setAmount(random.nextInt(5)+1).toItemStack());
						}
						return this;
					}

					@Override
					public void click(InventoryClickEvent e) {
						Task.this.clickMenu(e);
					}
				}.create().open();
				break;
			case MAKE_STACK:
				new Menu("Make Stack", event.getPlayer(), 36) {

					@Override
					public void update() {}

					@Override
					public Menu create() {
						setEditable(true);
						Random random = new Random();

						for (int i = 0; i < this.getInventory().getMaxStackSize(); i++) {
							int slot = random.nextInt(this.getInventory().getSize());
							ItemStack item = getItem(slot);
							int amount = 1;
							if (item != null) {
								amount += item.getAmount();
							}
							setItem(slot, new ItemCreation(Material.STAINED_GLASS_PANE).setAmount(amount).setDurability((short)5).toItemStack());
						}
						return this;
					}

					@Override
					public void click(InventoryClickEvent e) {
						Task.this.makeStack(e);
					}
				}.create().open();
				break;
			default:
				return;
			}
			this.completing = true;
		}
	}

	@EventHandler
	public void collectIron(PlayerPickupItemEvent event) {
		if (!uuid.equals(CoreAPI.getInstance().getCache().getUUID(event.getPlayer().getName()))) {
			if (items.contains(event.getItem()))event.setCancelled(true);
			return;
		}
		if (isCompleted())return;
		if (type != Type.COLLECT_IRON)return;
		if (!inTaskRegion(event.getPlayer())) {
			if (amount != 0)amount = 0;
			return;
		}
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(event.getPlayer());
		ItemStack item = event.getItem().getItemStack();
		if (item.getType() == Material.IRON_INGOT) {
			amount+=item.getAmount();
			if (amount == finishedAmount) {
				markCompleted();
				user.sendTitle(TitleAction.TITLE, CC.GREEN +"Task Completed");
				user.sendTitle(TitleAction.SUBTITLE, CC.GRAY +"You finished the Collect Iron Task");
			}
		}
	}

	@EventHandler
	public void throwTrash(PlayerDropItemEvent event) {
		if (!uuid.equals(CoreAPI.getInstance().getCache().getUUID(event.getPlayer().getName())))return;
		if (isCompleted())return;
		if (type != Type.THROW_TRASH)return;
		if (!inTaskRegion(event.getPlayer()))return;
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(event.getPlayer());
		Item item = event.getItemDrop();
		Cuboid cube = Minigames.getGson().fromJson(getEditor().getConfig().getString(Type.THROW_TRASH.name() +".trash-shoot"), new TypeToken<Cuboid>() {}.getType());

		new BukkitRunnable() {
			int timer = 0;
			public void run() {
				if (item.getItemStack().getType() == Material.EMERALD) {
					if (cube.contains(item.getLocation())) {
						if (!event.getPlayer().getInventory().contains(Material.EMERALD)) {
							markCompleted();
							user.sendTitle(TitleAction.TITLE, CC.GREEN +"Task Completed");
							user.sendTitle(TitleAction.SUBTITLE, CC.GRAY +"You finished the Throw Trash Task");
							cancel();
							return;
						}
					} else if (item.isOnGround()) {
						timer++;
						if (timer == 10) {
							item.remove();
							user.getPlayer().getInventory().addItem(item.getItemStack());
							user.getPlayer().updateInventory();
							timer = 0;
						}
					}
				}
			}
		}.runTaskTimer(Minigames.getInstance(), 1, 1);
	}

	@EventHandler
	public void shootMobs(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Monster))return;
		if (!(event.getDamager() instanceof Arrow))return;
		Arrow arrow = (Arrow)event.getDamager();
		if (!(arrow.getShooter() instanceof Player))return;
		if (!uuid.equals(((Player)arrow.getShooter()).getUniqueId()))return;
		if (type != Type.SHOOT_MOBS)return;
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser((Player)arrow.getShooter());
		if (!inTaskRegion(user.getPlayer()))return;

		if (!spawnedMobs.contains(event.getEntity())) {
			event.setCancelled(true);
		} else {
			event.setDamage(10);
		}
	}

	@EventHandler
	public void shootMobs(EntityTargetEvent event) {
		if (!spawnedMobs.contains(event.getEntity()))return;
		if (event.getTarget() == null)return;
		if (!uuid.equals(event.getTarget().getUniqueId())) {
			event.setCancelled(true);
			event.setTarget(Bukkit.getPlayer(uuid));
		}
	}

	@EventHandler
	public void shootMobs(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Monster))return;
		if (event.getEntity().getKiller() == null)return;
		if (!uuid.equals(event.getEntity().getKiller().getUniqueId()))return;
		if (type != Type.SHOOT_MOBS)return;
		if (!inTaskRegion(event.getEntity().getKiller()))return;
		event.getDrops().clear();
		event.setDroppedExp(0);
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(event.getEntity().getKiller());

		amount++;
		user.sendTitle(TitleAction.TITLE, CC.AQUA+"You killed "+CC.BLUE+amount+CC.AQUA+"/"+finishedAmount+" mobs");

		if (amount == finishedAmount) {
			markCompleted();
			user.sendTitle(TitleAction.TITLE, CC.GREEN +"Task Completed");
			user.sendTitle(TitleAction.SUBTITLE, CC.GRAY +"You finished the Shoot Mobs Task");
		}
	}

	public void clickMenu(InventoryClickEvent event) {
		if (!uuid.equals(event.getWhoClicked().getUniqueId()))return;
		if (type != Type.CLICK_MENU)return;
		if (!inTaskRegion((Player)event.getWhoClicked()))return;
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser((Player)event.getWhoClicked());
		ItemStack item = event.getCurrentItem();

		if (item == null)return;
		if (event.getClick() == ClickType.LEFT) {
			if (item.getType() == Material.STAINED_GLASS_PANE) {
				ItemCreation creation = new ItemCreation(item).setDisplayName(CC.GREEN +"Click Stack Once Done to Complete").setAmount(item.getAmount()-1);
				if (item.getAmount() == 1)
					event.getInventory().setItem(event.getSlot(), null);
				else event.getInventory().setItem(event.getSlot(), creation.toItemStack());
				if (!event.getView().getTopInventory().contains(Material.STAINED_GLASS_PANE)) {
					user.getPlayer().closeInventory();
					markCompleted();
					user.sendTitle(TitleAction.TITLE, CC.GREEN +"Task Completed");
					user.sendTitle(TitleAction.SUBTITLE, CC.GRAY +"You finished the Click Menu Task");
				}
			}
		}
	}

	public void makeStack(InventoryClickEvent event) {
		if (!uuid.equals(event.getWhoClicked().getUniqueId()))return;
		if (type != Type.MAKE_STACK)return;
		if (!inTaskRegion((Player)event.getWhoClicked()))return;
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser((Player)event.getWhoClicked());
		ItemStack item = event.getCurrentItem();

		if (item == null)return;
		if (item.getType() == Material.STAINED_GLASS_PANE) {
			if (event.getInventory().getItem(event.getRawSlot()).getAmount() == event.getInventory().getMaxStackSize()) {
				user.getPlayer().closeInventory();
				markCompleted();
				user.sendTitle(TitleAction.TITLE, CC.GREEN +"Task Completed");
				user.sendTitle(TitleAction.SUBTITLE, CC.GRAY +"You finished the Make Stack Task");
			}
		}
	}

	@EventHandler
	public void breakBlock(BlockBreakEvent event) {
		if (!uuid.equals(CoreAPI.getInstance().getCache().getUUID(event.getPlayer().getName())))return;
		if (type != Type.BREAK_BLOCK)return;
		if (!inTaskRegion(event.getPlayer()))return;
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(event.getPlayer());

		if (event.getBlock().getType() == Material.IRON_BLOCK) {
			markCompleted();
			user.sendTitle(TitleAction.TITLE, CC.GREEN +"Task Completed");
			user.sendTitle(TitleAction.SUBTITLE, CC.GRAY +"You finished the Break Block Task");
		}
	}

	public enum Difficulty {
		SHORT, COMMON, LONG;
	}

	public enum Type {
		COLLECT_IRON("Collect Iron", Difficulty.COMMON), THROW_TRASH("Throw Trash", Difficulty.SHORT), SHOOT_MOBS("Shoot Mobs", Difficulty.LONG),
		CLICK_MENU("Click Menu", Difficulty.COMMON),MAKE_STACK("Make Stack", Difficulty.SHORT), BREAK_BLOCK("Break Block", Difficulty.LONG);

		@Getter private Difficulty difficulty;
		@Getter private String name;

		Type(String name, Difficulty difficulty){
			this.difficulty = difficulty;
			this.name = name;
		}

		public static List<Type> byDifficulty(Difficulty difficulty) {
			return Stream.of(values()).filter(type -> type.getDifficulty() == difficulty).collect(Collectors.toList());
		}
	}
}
