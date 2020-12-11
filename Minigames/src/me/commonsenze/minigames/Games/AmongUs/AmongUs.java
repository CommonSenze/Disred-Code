package me.commonsenze.minigames.Games.AmongUs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.google.gson.reflect.TypeToken;
import com.topcat.npclib.entity.HumanNPC;
import com.topcat.npclib.entity.NPC;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.Lang;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Abstracts.Menu;
import me.commonsenze.core.Enums.ParticleEffect;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Objects.TeamBuilder;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.Game;
import me.commonsenze.minigames.Games.GameNameTagUpdateEvent;
import me.commonsenze.minigames.Games.GameNameTagUpdateEvent.GameReason;
import me.commonsenze.minigames.Games.GameState;
import me.commonsenze.minigames.Games.AmongUs.Objects.Meeting;
import me.commonsenze.minigames.Games.AmongUs.Objects.Meeting.Reason;
import me.commonsenze.minigames.Games.AmongUs.Objects.Sabatoge;
import me.commonsenze.minigames.Games.AmongUs.Objects.Task;
import me.commonsenze.minigames.Games.AmongUs.Objects.Task.Difficulty;
import me.commonsenze.minigames.Games.AmongUs.Objects.Teleporter;
import me.commonsenze.minigames.Games.AmongUs.Objects.UserTaskCompleteEvent;
import me.commonsenze.minigames.Games.AmongUs.Objects.Vent;
import me.commonsenze.minigames.Menu.MeetingMenu;
import me.commonsenze.minigames.Objects.User;

@Getter @Setter
public class AmongUs implements Game {

	private String id;
	private Set<User> users;
	private Imposter[] imposters;
	private Map<Task, UUID> totalTask;
	private GameState gameState;
	private Sabatoge sabatoge;
	private Location lobbySpawn, bedLocation, emergencyMeetingButton;
	@Setter(value = AccessLevel.NONE) private Location[] locations;
	private int nextLocationSlot, killCooldown;
	private long meetingCooldown;
	private Map<UUID, Integer> emergencyMeetings;
	private List<NPC> npcs;
	private Meeting currentMeeting;
	private long startTime;
	private Team imposterTeam,spectatorTeam;

	private static boolean READY;

	public static final String FOLDER = Game.FOLDER + "AmongUs"+File.separator;

	public AmongUs() {
		this.id = getName().replaceAll(" ", "").toLowerCase();
		this.emergencyMeetings = new HashMap<>();
		this.users = new HashSet<>();
		this.totalTask = new HashMap<>();
		this.npcs = new ArrayList<>();
		this.locations = new Location[10];
		this.imposters = new Imposter[2];
		refresh();
	}

	public static Editor getEditor() {
		return Minigames.getInstance().getConfig(FOLDER+"config");
	}

	public static String getName() {
		return "Among Us";
	}

	public int addLocation(Location location) {
		int slot = 0;
		for (; slot < locations.length; slot++)
			if (locations[slot] == null)break;

		if (slot == locations.length-1)return -1;

		this.locations[slot] = location;
		return slot;
	}

	public void setGameSpawn(User user) {
		if (user.getGameSpawn() == null)
			if (nextLocationSlot != locations.length)
				user.setGameSpawn(locations[nextLocationSlot++]);
		// TODO ERROR
	}

	public boolean useEmergencyMeeting(UUID uuid) {
		emergencyMeetings.put(uuid, getRemainingEmergencyMeetings(uuid)-1);
		return getRemainingEmergencyMeetings(uuid) >= 0;
	}

	public int getRemainingEmergencyMeetings(UUID uuid) {
		return emergencyMeetings.get(uuid);
	}

	public boolean isImposter(User user) {
		return imposters[0].getUser().equals(user)||imposters[1].getUser().equals(user);
	}

	public void clearCurrentNPCs() {
		npcs.forEach(CoreAPI.getInstance().getNPCManager().getNpcHandler()::despawn);
		npcs.clear();
	}

	public void giveItems(Player player) {
		CoreAPI.getInstance().getProfile(player).clearPlayer();
		if (isImposter(getUser(player)))
			player.getInventory().setItem(7, new ItemCreation(Material.BOOK).setDisplayName(CC.RED + "Sabatoge "+CC.GRAY + "(Right Click)")
					.toItemStack());
		player.getInventory().setItem(8, new ItemCreation(Material.FIREWORK).setDisplayName(CC.GOLD + "Report Body "+CC.GRAY + "(Right Click)")
				.toItemStack());
	}

	public void createOutlines(Player player) {
		new BukkitRunnable() {
			User user = getUser(player);
			public void run() {
				if (getGameState() != GameState.STARTED||!user.isOnline()||user.isSpectating()) {
					cancel();
					return;
				}
				totalTask.entrySet().stream().filter(entry -> 
				entry.getValue().equals(CoreAPI.getInstance().getCache().getUUID(player.getName()))
				&&!entry.getKey().inTaskRegion(player)&&!entry.getKey().isCompleted()
				&&entry.getKey().getBounds().getCenter().distanceSquared(player.getLocation())<100).forEach(entry -> {
					for (double x = entry.getKey().getBounds().getLowerX(); x < entry.getKey().getBounds().getUpperX(); x+=0.1) {
						double y = entry.getKey().getBounds().getLowerY();
						double z = entry.getKey().getBounds().getLowerZ();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
						y = entry.getKey().getBounds().getUpperY();
						z = entry.getKey().getBounds().getLowerZ();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
						y = entry.getKey().getBounds().getUpperY();
						z = entry.getKey().getBounds().getUpperZ();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
						y = entry.getKey().getBounds().getLowerY();
						z = entry.getKey().getBounds().getUpperZ();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
					}
					for (double y = entry.getKey().getBounds().getLowerY(); y < entry.getKey().getBounds().getUpperY(); y+=0.1) {
						double x = entry.getKey().getBounds().getLowerX();
						double z = entry.getKey().getBounds().getLowerZ();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
						x = entry.getKey().getBounds().getUpperX();
						z = entry.getKey().getBounds().getLowerZ();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
						x = entry.getKey().getBounds().getUpperX();
						z = entry.getKey().getBounds().getUpperZ();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
						x = entry.getKey().getBounds().getLowerX();
						z = entry.getKey().getBounds().getUpperZ();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
					}
					for (double z = entry.getKey().getBounds().getLowerZ(); z < entry.getKey().getBounds().getUpperZ(); z+=0.1) {
						double y = entry.getKey().getBounds().getLowerY();
						double x = entry.getKey().getBounds().getLowerX();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
						y = entry.getKey().getBounds().getUpperY();
						x = entry.getKey().getBounds().getLowerX();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
						y = entry.getKey().getBounds().getUpperY();
						x = entry.getKey().getBounds().getUpperX();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
						y = entry.getKey().getBounds().getLowerY();
						x = entry.getKey().getBounds().getUpperX();
						spawnParticle(player,new Location(player.getWorld(), x, y, z), ParticleEffect.CRIT_MAGIC);
					}
				});
			}
		}.runTaskTimer(Minigames.getInstance(), 10, 10);
	}

	private void spawnParticle(Player player, Location loc, ParticleEffect effect) {
		effect.display(0, 0, 0, 0, 1, loc,Arrays.asList(player));
	}

	public void finish(WinType type) {
		setGameState(GameState.ENDING);
		if (type == WinType.CREWMATE) {
			forEachUser(user -> {
				if (isImposter(user))
					user.sendTitle(TitleAction.TITLE, CC.RED + "Defeat");
				else user.sendTitle(TitleAction.TITLE, CC.AQUA + "Victory");
				user.sendTitle(TitleAction.SUBTITLE, "");
				user.getPlayer().getInventory().clear();
				user.getPlayer().setAllowFlight(true);
				user.setSpectating(true);
				forEachUser(u -> {
					if (u.isSpectating())user.getPlayer().showPlayer(u.getPlayer());
				});
				GameNameTagUpdateEvent e = new GameNameTagUpdateEvent(CoreAPI.getInstance().getProfile(user.getPlayer()), GameNameTagUpdateEvent.GameReason.END, this);
				Bukkit.getPluginManager().callEvent(e);
			});
		} else if (type == WinType.IMPOSTER) {
			forEachUser(user -> {
				if (!isImposter(user))
					user.sendTitle(TitleAction.TITLE, CC.RED + "Defeat");
				else user.sendTitle(TitleAction.TITLE, CC.AQUA + "Victory");
				user.sendTitle(TitleAction.SUBTITLE, "");
				user.getPlayer().getInventory().clear();
				user.getPlayer().setAllowFlight(true);
				user.setSpectating(true);
				forEachUser(u -> {
					if (u.isSpectating())user.getPlayer().showPlayer(u.getPlayer());
				});
				GameNameTagUpdateEvent e = new GameNameTagUpdateEvent(CoreAPI.getInstance().getProfile(user.getPlayer()), GameNameTagUpdateEvent.GameReason.END, this);
				Bukkit.getPluginManager().callEvent(e);
			});
		}
		
		Bukkit.getScheduler().runTaskLater(Minigames.getInstance(), () -> {
			end();
		}, 5*20);
	}

	public Imposter getImposter(User user) {
		if (!isImposter(user))return null;
		return imposters[0].getUser().equals(user) ? imposters[0] : imposters[1];
	}

	@EventHandler
	public void switchItems(InventoryClickEvent event) {
		if (isGaming(event.getWhoClicked().getUniqueId())) {
			if (event.getClickedInventory() == null)return;
			if (event.getClickedInventory().equals(event.getView().getBottomInventory()))event.setCancelled(true);
		}
	}

	@EventHandler
	public void damage(EntityDamageEvent event) {
		if (isGaming(CoreAPI.getInstance().getCache().getUUID(event.getEntity().getName()))&&event.getCause() != DamageCause.CUSTOM) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void rightClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR&&event.getAction() != Action.RIGHT_CLICK_BLOCK)return;
		if (event.getMaterial() == null)return;
		if (!event.hasItem())return;
		if (!event.getItem().hasItemMeta())return;
		if (!event.getItem().getItemMeta().hasDisplayName())return;
		String name = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
		if (isGaming(event.getPlayer())&&isGaming(event.getPlayer())) {
			event.setCancelled(true);
			switch (event.getMaterial()) {
			case BOOK:
				if (name.contains("Sabatoge")) {
					if (getGameState() == GameState.ENDING) {
						event.getPlayer().sendMessage(Lang.fail("-nThe game is already over."));
						return;
					} else if (getGameState() != GameState.STARTED)return;
					new Menu("Sabatoge Selector", event.getPlayer(), 9) {

						@Override
						public Menu create() {
							for (Sabatoge.Type type : Sabatoge.Type.values()) {
								addItem(new ItemCreation(Material.CHEST).setDisplayName(CC.RED + type.name()).toItemStack());
							}
							return this;
						}

						@Override
						public void update() {}

						@Override
						public void click(InventoryClickEvent e) {
							ItemStack item = e.getCurrentItem();
							String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
							if (item == null)return;
							if (item.getType() == Material.CHEST) {
								delete();
								if (getSabatoge() != null&&getSabatoge().isInProgress()) {
									event.getPlayer().sendMessage(Lang.fail("-nThere is currently a sabatoge in progress."));
									return;
								}
								Sabatoge.Type type = Sabatoge.Type.valueOf(name);
								AmongUs.this.setSabatoge(new Sabatoge(AmongUs.this, type));
								AmongUs.this.getSabatoge().sabatoge();
							}
						}
					}.create().open();
				}
				break;
			case FIREWORK:
				if (name.contains("Report")) {
					if (getGameState() == GameState.ENDING) {
						event.getPlayer().sendMessage(Lang.fail("-nThe game is already over."));
						return;
					} else if (getGameState() != GameState.STARTED)return;
					if (getCurrentMeeting() != null) {
						event.getPlayer().sendMessage(Lang.fail("-nThere is currently a meeting in progress."));
						return;
					}
					for (NPC npc : npcs) {
						if (npc.getBukkitEntity().getNearbyEntities(npc.getVisibility(), npc.getVisibility(), npc.getVisibility()).contains(event.getPlayer())) {
							setCurrentMeeting(new Meeting(this, getUser(event.getPlayer()), 
									getUser(CoreAPI.getInstance().getCache().getUUID(((HumanNPC)npc).getNpcEntity().getSkinName())), Reason.BODY));
							getCurrentMeeting().call();
						}
					}
				}
				break;
			case CHEST:
				if (name.contains("Voting Menu")) {
					if (getGameState() == GameState.ENDING) {
						event.getPlayer().sendMessage(Lang.fail("-nThe game is already over."));
						return;
					} else if (getGameState() != GameState.STARTED)return;
					event.getPlayer().openInventory(getCurrentMeeting().getMenu().getInventory());
				}
				break;
			default:
				break;
			}
		}
	}

	@EventHandler
	public void rightClickBlock(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)return;
		if (event.getClickedBlock() == null)return;
		Block block = event.getClickedBlock();
		if (isGaming(event.getPlayer())&&isGaming(event.getPlayer())) {
			if (emergencyMeetingButton.getBlock().equals(block)) {
				if (getCurrentMeeting() != null) {
					event.getPlayer().sendMessage(Lang.fail("-nThere is currently a meeting in progress."));
					return;
				}
				if (getMeetingCooldown() > System.currentTimeMillis()) {
					event.getPlayer().sendMessage(Lang.fail("-nYou cannot call another meeting for "+((getMeetingCooldown()-System.currentTimeMillis())/1000)+"s."));
					return;
				}
				if (!useEmergencyMeeting(CoreAPI.getInstance().getCache().getUUID(event.getPlayer().getName()))) {
					event.getPlayer().sendMessage(Lang.fail("-nYou have no more emergency meetings left."));
					return;
				}
				event.getPlayer().sendMessage(Lang.success("-nYou have "+getRemainingEmergencyMeetings(CoreAPI.getInstance().getCache().getUUID(event.getPlayer().getName()))+" emergency meetings left."));
				setCurrentMeeting(new Meeting(this, getUser(event.getPlayer()), 
						null, Reason.EMERGENCY));
				getCurrentMeeting().call();
			}
		}
	}

	@EventHandler
	public void attack(EntityDamageByEntityEvent event) {
		if (isGaming(CoreAPI.getInstance().getCache().getUUID(event.getEntity().getName()))&&isGaming(CoreAPI.getInstance().getCache().getUUID(event.getDamager().getName()))&&!getUser(CoreAPI.getInstance().getCache().getUUID(event.getEntity().getName())).isSpectating()
				&&!getUser(CoreAPI.getInstance().getCache().getUUID(event.getDamager().getName())).isSpectating()) {
			User user = null;
			if (isImposter(user = getUser(event.getDamager().getUniqueId()))) {
				if (getImposter(user).canKill()) {
					if (getCurrentMeeting() != null) {
						user.getPlayer().sendMessage(Lang.fail("-nYou cannot kill while in a meeting."));
						return;
					}
					if (isImposter(getUser(((Player)event.getEntity())))) {
						return;
					}
					getImposter(user).setKillCooldown(getKillCooldown());
					((Player)event.getEntity()).damage(0);
					quit(((Player)event.getEntity()), QuitReason.DEATH);
				}
			}
		}
	}

	@EventHandler
	public void taskComplete(UserTaskCompleteEvent event) {
		if (isGaming(event.getPlayer())) {
			if (getTotalTask().keySet().stream().filter(task -> !task.isCompleted()).count() == 0) {
				finish(WinType.CREWMATE);
			}
		}
	}

	@EventHandler
	public void venting(PlayerToggleSneakEvent event) {
		if (isGaming(event.getPlayer())&&!getUser(event.getPlayer()).isSpectating()) {
			if (isImposter(getUser(event.getPlayer()))) {
				if (event.isSneaking()) {
					for (Vent vent : Minigames.getInstance().getManagerHandler().getVentManager().getVents()) {
						if (vent.getBounds().contains(event.getPlayer().getLocation())) {
							Imposter imposter = getImposter(getUser(event.getPlayer()));
							if (!imposter.isPaused())
								imposter.pauseKillCooldown();
							event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999*20, 0));
							break;
						}
					}
				} else if (event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
					event.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
					Imposter imposter = getImposter(getUser(event.getPlayer()));
					if (imposter.isPaused())
						imposter.unpauseKillCooldown();
				}
			}
		}
	}

	@EventHandler
	public void venting(PlayerMoveEvent event) {
		if (isGaming(event.getPlayer())&&!getUser(event.getPlayer()).isSpectating()) {
			if (isImposter(getUser(event.getPlayer()))) {
				if (Minigames.getInstance().getManagerHandler().getVentManager().getVents().stream().filter(vent -> vent.getBounds().contains(event.getTo())).count() == 0) {
					event.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
					Imposter imposter = getImposter(getUser(event.getPlayer()));
					if (imposter.isPaused())
						imposter.unpauseKillCooldown();
				}
			}
		}
	}

	@EventHandler
	public void teleporting(PlayerMoveEvent event) {
		if (isGaming(event.getPlayer())&&!getUser(event.getPlayer()).isSpectating()) {
			if (isImposter(getUser(event.getPlayer()))) {
				if (event.getPlayer().isSneaking()) {
					for (Teleporter teleporter : Minigames.getInstance().getManagerHandler().getTeleporterManager().getTeleporters()) {
						if (teleporter.getFirst().distanceSquared(event.getPlayer().getLocation()) < 0.25) {
							event.getPlayer().teleport(teleporter.getSecond());
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void nameTagGameChange(GameNameTagUpdateEvent event) {
		if (!event.getCurrentGame().equals(this))return;
		if (event.getGameReason() == GameReason.START) {
			if (isImposter(getUser(event.getProfile().getUniqueId())))
				event.getProfile().setTeam(imposterTeam);
		} else if (event.getGameReason() == GameReason.END) {
			event.getProfile().setTeam(spectatorTeam);
		} else if (event.getGameReason() == GameReason.LEAVE) {
			event.getProfile().getPlayer().setPlayerListName(null);
			spectatorTeam.removeEntry(event.getProfile().getName());
			event.getProfile().setTeam(new TeamBuilder(event.getProfile().getPersonalTeamName())
					.setPrefix(CoreAPI.getInstance().getManagerHandler().getPrefixManager().getPrefix(event.getProfile()))
					.toTeam());
		} else if (event.getGameReason() == GameReason.SPECTATE) {
			event.getProfile().getPlayer().setPlayerListName(null);
			spectatorTeam.addEntry(event.getProfile().getName());
			event.getProfile().setTeam(spectatorTeam);
		}
	}
	
	@Override
	public int getRequiredPlayers() {
		return imposters.length*2+1;
	}

	@Override
	public void start() {
		if (getUsers().size() < getRequiredPlayers()) {
			Minigames.getInstance().getManagerHandler().getGameManager().reset(this, 60);
			return;
		}

		Minigames.getInstance().getManagerHandler().getGameManager().stopCountdown(this);
		this.startTime = System.currentTimeMillis();

		setGameState(GameState.STARTED);
		Bukkit.getPluginManager().registerEvents(this, Minigames.getInstance());

		Random random = new Random();

		List<User> list = new ArrayList<>(getUsers());

		int first = random.nextInt(list.size());

		this.imposters[0] = new Imposter(list.get(first));
		list.remove(first);

		int second = random.nextInt(list.size());
		this.imposters[1] = new Imposter(list.get(second));

		this.imposters[0].setKillCooldown(killCooldown);
		this.imposters[1].setKillCooldown(killCooldown);

		this.meetingCooldown = System.currentTimeMillis()+15000;

		list.clear();

		forEachUser(user -> {
			setGameSpawn(user);

			Profile profile = CoreAPI.getInstance().getProfile(user.getPlayer());
			GameNameTagUpdateEvent e = new GameNameTagUpdateEvent(profile, GameNameTagUpdateEvent.GameReason.START, this);
			Bukkit.getPluginManager().callEvent(e);

			profile.setInvincible(false);
			user.setSpectating(false);
			user.getPlayer().teleport(user.getGameSpawn());

			if (!isImposter(user)) {
				totalTask.put(new Task(user.getUniqueId(), Difficulty.COMMON), user.getUniqueId());
				totalTask.put(new Task(user.getUniqueId(), Difficulty.SHORT), user.getUniqueId());
				totalTask.put(new Task(user.getUniqueId(), Difficulty.LONG), user.getUniqueId());
				createOutlines(user.getPlayer());
				user.sendTitle(TitleAction.TITLE, CC.AQUA + "Crewmate");
				user.sendTitle(TitleAction.SUBTITLE, CC.GRAY + "There are "+CC.RED+"2 "+CC.GRAY+"imposters among us.");
			} else {
				user.sendTitle(TitleAction.TITLE, CC.RED + "Imposter");
				user.sendTitle(TitleAction.SUBTITLE, CC.GRAY + "Kill all crewmates.");
			}
			emergencyMeetings.put(user.getUniqueId(), 1);
			giveItems(user.getPlayer());
		});
	}

	@Override
	public void end() {
		setGameState(GameState.LOBBY);
		forEachUser(user -> {
			Profile profile = CoreAPI.getInstance().getProfile(user.getPlayer());
			user.setSpectating(false);
			user.setCurrentGame(null);
			user.setGameSpawn(null);
			profile.clearPlayer();
			profile.setInvincible(true);
			GameNameTagUpdateEvent e = new GameNameTagUpdateEvent(profile, GameReason.LEAVE, this);
			Bukkit.getPluginManager().callEvent(e);
			Minigames.getInstance().getManagerHandler().getUserManager().sendToSpawn(user.getPlayer());
			Minigames.getInstance().getManagerHandler().getUserManager().getUserSet().stream().filter(u -> !u.inGame()).forEach(u -> {
				user.getPlayer().showPlayer(u.getPlayer());
				u.getPlayer().showPlayer(user.getPlayer());
			});
		});
		clearCurrentNPCs();

		if (currentMeeting != null) {
			currentMeeting.end(null, MeetingMenu.Reason.CUSTOM);
		}
		if (this.sabatoge != null) {
			sabatoge.completed();
		}
		
		this.totalTask.keySet().forEach(task -> task.end());
		
		this.emergencyMeetings.clear();
		this.users.clear();
		this.totalTask.clear();
		this.npcs.clear();
		
		this.imposters[0] = null;
		this.imposters[1] = null;
		
		for (int i = 0; i < locations.length; i++) {
			locations[i] = null;
		}

		this.sabatoge = null;
		this.currentMeeting = null;
		this.emergencyMeetingButton = null;
		HandlerList.unregisterAll(this);
		refresh();
	}

	@Override
	public void join(Player player) {
		User user = getUser(player);

		getUsers().add(user);
		Minigames.getInstance().getManagerHandler().getUserManager().getUserSet().stream().filter(u -> !u.inGame()).forEach(u -> {
			u.getPlayer().hidePlayer(user.getPlayer());
			user.getPlayer().hidePlayer(u.getPlayer());
		});
		forEachUser(u -> {
			u.getPlayer().showPlayer(user.getPlayer());
			user.getPlayer().showPlayer(u.getPlayer());
		});
		user.getPlayer().teleport(getLobbySpawn());
		user.setCurrentGame(this);
		Profile profile = CoreAPI.getInstance().getProfile(user.getPlayer());
		profile.clearPlayer();
		
		GameNameTagUpdateEvent e = new GameNameTagUpdateEvent(profile, GameReason.JOIN, this);
		Bukkit.getPluginManager().callEvent(e);

		if (getUsers().size() >= getRequiredPlayers()&&!Minigames.getInstance().getManagerHandler().getGameManager().hasCountdown(this)) {
			Minigames.getInstance().getManagerHandler().getGameManager().startCountdown(this, 120);
		}
	}

	@Override
	public void quit(Player player, QuitReason reason) {
		User user = getUser(player);
		switch (reason) {
		case DEATH:
			Location loc = player.getLocation();
			NPC npc = CoreAPI.getInstance().getNPCManager().getNpcHandler().spawnHumanNPC(player.getName(), loc);
			Bukkit.getScheduler().runTaskAsynchronously(Minigames.getInstance(), () -> {
				CoreAPI.getInstance().getNPCManager().getNpcHandler().setSkin(((HumanNPC)npc).getNpcEntity(), player.getName());
				((HumanNPC)npc).putInBed(bedLocation, loc);
				npc.setVisibility(10);
				npcs.add(npc);
			});
		case CUSTOM:
			player.setAllowFlight(true);
			player.setFlying(true);
			user.setSpectating(true);
			forEachUser(u -> {
				if (!u.isSpectating())u.getPlayer().hidePlayer(player);
				else if (!player.canSee(u.getPlayer()))player.showPlayer(u.getPlayer());
			});
			{
				GameNameTagUpdateEvent e = new GameNameTagUpdateEvent(CoreAPI.getInstance().getProfile(player), GameNameTagUpdateEvent.GameReason.SPECTATE, this);
				Bukkit.getPluginManager().callEvent(e);
			}
			break;
		case LEAVE:
			user.getPlayer().teleport(CoreAPI.getInstance().getSpawnLocation());
			Minigames.getInstance().getManagerHandler().getUserManager().getUserSet().stream().filter(u -> !u.inGame()).forEach(u -> {
				user.getPlayer().showPlayer(u.getPlayer());
				u.getPlayer().showPlayer(user.getPlayer());
			});
			if (getUsers().size() < getRequiredPlayers()&&getGameState()==GameState.LOBBY&&Minigames.getInstance().getManagerHandler().getGameManager().hasCountdown(this)) {
				Minigames.getInstance().getManagerHandler().getGameManager().stopCountdown(this);
			}
		case DISCONNECT:
			user.setCurrentGame(null);
			forEachUser(u -> {
				u.getPlayer().sendMessage(CC.RED + player.getName() + " has left the game.");
			});
			{
				GameNameTagUpdateEvent e = new GameNameTagUpdateEvent(CoreAPI.getInstance().getProfile(player), GameNameTagUpdateEvent.GameReason.LEAVE, this);
				Bukkit.getPluginManager().callEvent(e);
			}
			break;
		default:
			break;
		}
		if (getGameState() == GameState.STARTED) {
			int impostersAlive = 0, crewmatesAlive = 0;
			if (imposters[0].getUser().isOnline()&&!imposters[0].getUser().isSpectating()&&!user.equals(imposters[0].getUser()))impostersAlive++;
			if (imposters[1].getUser().isOnline()&&!imposters[1].getUser().isSpectating()&&!user.equals(imposters[1].getUser()))impostersAlive++;

			for (User u : getUsers()) {
				if (!isImposter(u)&&!user.equals(u)&&isPlaying(u))crewmatesAlive++;
			}

			if (impostersAlive >= crewmatesAlive) {
				finish(WinType.IMPOSTER);
			} else if (impostersAlive == 0) {
				finish(WinType.CREWMATE);
			}
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int getMaxPlayers() {
		return 10;
	}

	@Override
	public String getTodoList() {
		String list = "";
		READY = bedLocation != null && locations!=null&&lobbySpawn !=null&&emergencyMeetingButton != null;
		if (!AmongUs.READY) {
			list += CC.DARKRED + getName() + CC.RED+ " is not ready:\n"
					+ CC.GRAY+ "Bed Location Set? "+(bedLocation!=null ? CC.GREEN +"True" : CC.RED +"False")+"\n"
					+ CC.GRAY+ "Lobby Location Set? "+(lobbySpawn!=null ? CC.GREEN +"True" : CC.RED +"False")+"\n"
					+ CC.GRAY+ "Game Locations Set? "+(locations!=null ? CC.GREEN +"True" : CC.RED +"False")+"\n"
					+ CC.GRAY+ "Emergency Meeting Button Set? "+(emergencyMeetingButton!=null ? CC.GREEN +"True" : CC.RED +"False");
		} else {
			list += CC.GREEN+ getName() + " is up and ready to go.";
		}
		return list;
	}

	@Override
	public void refresh() {
		this.gameState = GameState.LOBBY;
		this.killCooldown = 25;
		this.nextLocationSlot = 0;
		this.imposterTeam = new TeamBuilder("imposters").setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS).setPrefix(CC.RED).toTeam();
		this.spectatorTeam = new TeamBuilder("spectators").setPrefix(CC.GRAY).setNameTagVisibility(NameTagVisibility.NEVER).setCanSeeFriendlyInvisibles(true).toTeam();
		this.emergencyMeetingButton = (Location) Minigames.getGson().fromJson(getEditor().getConfig().getString("emergency-meeting-button"), new TypeToken<Location>() {}.getType());
		this.bedLocation = (Location) Minigames.getGson().fromJson(getEditor().getConfig().getString("bed-location"), new TypeToken<Location>() {}.getType());

		if (getEditor().getConfig().contains("locations"))
			this.locations = (Location[]) Minigames.getGson().fromJson(getEditor().getConfig().getString("locations"), new TypeToken<Location[]>() {}.getType());

		this.lobbySpawn = (Location) Minigames.getGson().fromJson(getEditor().getConfig().getString("lobby-location"), new TypeToken<Location>() {}.getType());
		READY = bedLocation != null && locations!=null&&lobbySpawn !=null&&emergencyMeetingButton != null;
	}

	@Override
	public boolean isReady() {
		return READY;
	}

	public enum WinType {
		IMPOSTER, CREWMATE;
	}

	@Getter
	public class Imposter {

		private User user;
		private long killCooldown, pausedCooldownLeft;
		private boolean paused;

		private Imposter(User user) {
			this.user = user;
		}

		public void setKillCooldown(int seconds) {
			this.killCooldown = System.currentTimeMillis()+(seconds*1000);
		}

		public boolean canKill() {
			return this.killCooldown < System.currentTimeMillis();
		}

		public void pauseKillCooldown() {
			this.paused = true;
			this.pausedCooldownLeft = (killCooldown - System.currentTimeMillis());
		}

		public void unpauseKillCooldown() {
			this.paused = false;
			this.killCooldown = System.currentTimeMillis()+this.pausedCooldownLeft;
		}
	}
}