package me.commonsenze.minigames.Games.AmongUs.Objects;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import me.commonsenze.core.Abstracts.Menu;
import me.commonsenze.core.Enums.ParticleEffect;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Objects.Cuboid;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.AmongUs.AmongUs;
import me.commonsenze.minigames.Games.AmongUs.AmongUs.WinType;
import me.commonsenze.wrapper.Packets.WrapperPlayServerTitle;

@Getter
public class Sabatoge implements Listener {

	private AmongUs amongUs;
	private Type type;
	private BukkitTask task;
	private Set<UUID> completing;
	private Menu menu;
	@Getter private boolean inProgress;
	@Getter private static Editor editor;
	@Getter private static long sabatogeCooldown;

	static {
		editor = Minigames.getInstance().getConfig(AmongUs.FOLDER+"Sabatoge");
	}

	public Sabatoge(AmongUs amongUs, Type type) {
		this.amongUs = amongUs;
		this.type = type;
		this.completing = new HashSet<>();
	}

	public static void setTaskRegion(Type type, Cuboid cuboid) {
		editor.getConfig().set(type.name() +".area", Minigames.getGson().toJson(cuboid));
		editor.saveConfig();
	}

	public boolean inTaskRegion(Player player) {
		Cuboid cube = Minigames.getGson().fromJson(editor.getConfig().getString(type.name() +".area"), new TypeToken<Cuboid>() {}.getType());
		return cube.contains(player.getLocation());
	}

	public void createOutlines() {
		new BukkitRunnable() {
			public void run() {
				if (!inProgress) {
					cancel();
					return;
				}
				Cuboid cube = Minigames.getGson().fromJson(editor.getConfig().getString(type.name() +".area"), new TypeToken<Cuboid>() {}.getType());
				for (double x = cube.getLowerX(); x < cube.getUpperX(); x+=0.1) {
					double y = cube.getLowerY();
					double z = cube.getLowerZ();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
					y = cube.getUpperY();
					z = cube.getLowerZ();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
					y = cube.getUpperY();
					z = cube.getUpperZ();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
					y = cube.getLowerY();
					z = cube.getUpperZ();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
				}
				for (double y = cube.getLowerY(); y < cube.getUpperY(); y+=0.1) {
					double x = cube.getLowerX();
					double z = cube.getLowerZ();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
					x = cube.getUpperX();
					z = cube.getLowerZ();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
					x = cube.getUpperX();
					z = cube.getUpperZ();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
					x = cube.getLowerX();
					z = cube.getUpperZ();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
				}
				for (double z = cube.getLowerZ(); z < cube.getUpperZ(); z+=0.1) {
					double y = cube.getLowerY();
					double x = cube.getLowerX();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
					y = cube.getUpperY();
					x = cube.getLowerX();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
					y = cube.getUpperY();
					x = cube.getUpperX();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
					y = cube.getLowerY();
					x = cube.getUpperX();
					spawnParticle(new Location(cube.getWorld(), x, y, z), ParticleEffect.CRIT);
				}
			}
		}.runTaskTimer(Minigames.getInstance(), 10, 10);
	}

	private void spawnParticle(Location loc, ParticleEffect effect) {
		effect.display(0, 0, 0, 0, 1, loc,20);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event) {
		if (!isInProgress())return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)return;
		if (event.getClickedBlock().getType() != Material.STONE_BUTTON)return;
		if (inTaskRegion(event.getPlayer())) {
			switch (type) {
			case LIGHTS:
				if (this.menu == null) {
					this.menu = new Menu("Lights", event.getPlayer(), 36) {

						@Override
						public void update() {}

						@Override
						public Menu create() {
							setMultipleInputs(true);
							List<Integer> list = Arrays.asList(5, 14); 


							setItem(10, new ItemCreation(Material.STONE_BUTTON)
									.toItemStack());
							setItem(12, new ItemCreation(Material.STONE_BUTTON)
									.toItemStack());
							setItem(14, new ItemCreation(Material.STONE_BUTTON)
									.toItemStack());
							setItem(16, new ItemCreation(Material.STONE_BUTTON)
									.toItemStack());

							Collections.shuffle(list);

							setItem(19, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability(list.get(0).shortValue())
									.toItemStack());

							Collections.shuffle(list);

							setItem(21, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability(list.get(0).shortValue())
									.toItemStack());

							Collections.shuffle(list);

							setItem(23, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability(list.get(0).shortValue())
									.toItemStack());

							Collections.shuffle(list);

							setItem(25, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability(list.get(0).shortValue())
									.toItemStack());
							fill(8);
							return this;
						}

						@Override
						public void click(InventoryClickEvent e) {
							if (type != Type.LIGHTS)return;
							if (!inTaskRegion((Player)e.getWhoClicked()))return;
							ItemStack item = e.getCurrentItem();

							if (item == null)return;
							if (item.getType() == Material.STONE_BUTTON) {
								ItemStack itemBelow = e.getInventory().getItem(e.getSlot()+9);
								if (itemBelow.getDurability() == 5)
									e.getInventory().setItem(e.getSlot()+9, new ItemCreation(itemBelow).setAmount((short)14).toItemStack());
								else e.getInventory().setItem(e.getSlot()+9, new ItemCreation(itemBelow).setDurability((short)5).toItemStack());
								Bukkit.getScheduler().runTaskLater(Minigames.getInstance(), () -> {
									for (int i = 19; i < 25; i+=2) {
										if (e.getInventory().getItem(i).getDurability() == 14)
											return;
									}
									delete();
									completed();
								}, 30);
							}
						}
					}.create();
					menu.open();
				} else {
					menu.addViewer(event.getPlayer());
				}
				break;
			case OXYGEN:
				new Menu("Oxygen Filtration", event.getPlayer(), 45) {

					private String code, typed = "";

					@Override
					public void update() {
						setItem(16, new ItemCreation(Material.PAPER).setDisplayName("Pin Code: "+code).toItemStack());

						setItem(12, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"1").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(13, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"2").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(14, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"3").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(21, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"4").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(22, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"5").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(23, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"6").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(30, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"7").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(31, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"8").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(32, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"9").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(40, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"0").addLore("Current Typed Code: ["+typed+"]").toItemStack());

						setItem(28, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short)4).setDisplayName("Reset").toItemStack());
						setItem(34, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short)5).setDisplayName("Submit").toItemStack());
					}

					@Override
					public Menu create() {
						Random random = new Random();

						String code = random.nextInt(10)+""+random.nextInt(10)+""+random.nextInt(10)+""+random.nextInt(10)+""+random.nextInt(10);

						this.code = code;

						setItem(16, new ItemCreation(Material.PAPER).setDisplayName("Pin Code: "+code).toItemStack());

						setItem(12, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"1").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(13, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"2").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(14, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"3").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(21, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"4").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(22, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"5").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(23, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"6").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(30, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"7").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(31, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"8").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(32, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"9").addLore("Current Typed Code: ["+typed+"]").toItemStack());
						setItem(40, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName(CC.WHITE +"0").addLore("Current Typed Code: ["+typed+"]").toItemStack());

						setItem(28, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short)4).setDisplayName("Reset").toItemStack());
						setItem(34, new ItemCreation(Material.STAINED_GLASS_PANE).setDurability((short)5).setDisplayName("Submit").toItemStack());

						fill(8);
						return this;
					}

					@Override
					public void click(InventoryClickEvent e) {
						if (type != Type.OXYGEN)return;
						if (!inTaskRegion((Player)e.getWhoClicked()))return;
						ItemStack item = e.getCurrentItem();

						if (item == null)return;
						if (item.getType() == Material.STAINED_GLASS_PANE) {
							if (item.getDurability() == 5) {
								setItem(e.getSlot(), new ItemCreation(item).setDisplayName(" ").toItemStack());
								if (code.equals(typed)) {
									new BukkitRunnable() {
										private int count = 4;
										@Override
										public void run() {
											count--;
											if (getItem(e.getSlot()).getItemMeta().getDisplayName().equals(" "))
												setItem(e.getSlot(), new ItemCreation(item).setDisplayName(CC.GREEN + "Correct").toItemStack());
											else setItem(e.getSlot(), new ItemCreation(item).setDisplayName(" ").toItemStack());
											if (count == 0) {
												cancel();
												Bukkit.getScheduler().runTaskLater(Minigames.getInstance(), () -> {
													delete();
													completed();
												}, 20);
											}
										}
									}.runTaskTimer(Minigames.getInstance(), 10, 10);
								} else {
									typed = "";
									update();
									new BukkitRunnable() {
										private int count = 5;
										@Override
										public void run() {
											count--;
											if (getItem(e.getSlot()).getItemMeta().getDisplayName().equals(" "))
												setItem(e.getSlot(), new ItemCreation(item).setDisplayName(CC.RED + "Incorrect").toItemStack());
											else setItem(e.getSlot(), new ItemCreation(item).setDisplayName(" ").toItemStack());
											if (count == 0) {
												cancel();
											}
										}
									}.runTaskTimer(Minigames.getInstance(), 10, 10);
								}
							} else if (item.getDurability() == 0&&typed.length() < 5) {
								typed += ChatColor.stripColor(item.getItemMeta().getDisplayName());
								update();
							} else if (item.getDurability() == 4) {
								typed = "";
								update();
							}
						}
					}
				}.create().open();
				break;
			default:
				return;
			}
		}
	}

	public void sabatoge() {
		switch (type) {
		case LIGHTS:
			this.getAmongUs().forEachUser(user -> {
				if (!this.amongUs.isImposter(user))
					user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 9999*20, 0));
			});
			break;
		case OXYGEN:
			task = Bukkit.getScheduler().runTaskTimer(Minigames.getInstance(), new Runnable() {

				private int timer = 25;

				@Override
				public void run() {
					if (timer == 0) {
						Sabatoge.this.getAmongUs().finish(WinType.IMPOSTER);
						Bukkit.getScheduler().cancelTask(task.getTaskId());
						return;
					}
					Sabatoge.this.getAmongUs().forEachUser(user -> {

						WrapperPlayServerTitle title = new WrapperPlayServerTitle();
						title.setAction(TitleAction.TITLE);
						title.setFadeIn(5);
						title.setStay(10);
						title.setFadeOut(5);
						title.setTitle(WrappedChatComponent.fromText(CC.DARKRED + "Oxygen Malfunction"));
						WrapperPlayServerTitle subtitle = new WrapperPlayServerTitle();
						subtitle.setAction(TitleAction.SUBTITLE);
						subtitle.setFadeIn(5);
						subtitle.setStay(10);
						subtitle.setFadeOut(5);
						subtitle.setTitle(WrappedChatComponent.fromText(CC.GRAY + "Time Remaining: "+ timer));
						title.sendPacket(user.getPlayer());
						subtitle.sendPacket(user.getPlayer());
					});
					timer--;
				}
			}, 0, 20);
			break;
		default:
			break;
		}
		this.inProgress = true;
		Bukkit.getPluginManager().registerEvents(this, Minigames.getInstance());
		createOutlines();
	}

	public void completed() {
		inProgress = false;
		HandlerList.unregisterAll(this);
		Sabatoge.sabatogeCooldown = System.currentTimeMillis()+25000;
		this.getAmongUs().setSabatoge(null);
		switch (type) {
		case LIGHTS:
			this.getAmongUs().forEachUser(user -> {
				if (!this.amongUs.isImposter(user))
					user.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
			});
			break;
		case OXYGEN:
			this.getAmongUs().forEachUser(user -> {
				if (user.getPlayer().getOpenInventory() != null) {
					if (user.getPlayer().getOpenInventory().getTopInventory().getTitle().equals("Oxygen Filtration")) {
						user.getPlayer().closeInventory();
					}
				}
			});
			if (task != null)
				Bukkit.getScheduler().cancelTask(this.task.getTaskId());
			this.task = null;
			break;
		default:
			break;
		}
	}

	public enum Type {
		LIGHTS, OXYGEN;
	}
}
