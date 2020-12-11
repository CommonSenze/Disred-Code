package me.commonsenze.minigames.Games.Skywars;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Objects.TeamBuilder;
import me.commonsenze.core.Util.CC;
import me.commonsenze.core.Util.WorldEditUtils;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.Game;
import me.commonsenze.minigames.Games.GameNameTagUpdateEvent;
import me.commonsenze.minigames.Games.GameNameTagUpdateEvent.GameReason;
import me.commonsenze.minigames.Games.GameState;
import me.commonsenze.minigames.Games.Skywars.Objects.ChestItems;
import me.commonsenze.minigames.Games.Skywars.Objects.Island;
import me.commonsenze.minigames.Games.Skywars.Objects.ChestItems.Type;
import me.commonsenze.minigames.Objects.User;

@Getter @Setter
public class Skywars implements Game {

	private Set<User> users;
	private Set<Island> islands;
	private Island mainIsland;
	private String id;
	private long startTime;
	private GameState gameState;
	private Location lobbySpawn;
	private Team spectatorTeam;
	private int nextLocationSlot;
	private ChestItems chestItems;
	
	private static boolean READY;
	private static World world;

	public static final String FOLDER = Game.FOLDER + "Skywars" +File.separator;

	public Skywars() {
		this.id = getName().replaceAll(" ", "").toLowerCase();
		this.users = new HashSet<>();
		this.chestItems = new ChestItems();
		this.chestItems.customInput(Type.WEAPON, Arrays.asList(
				new ItemCreation(Material.DIAMOND_AXE).setAmount(1).toItemStack(),
				new ItemCreation(Material.STICK).addEnchantment(Enchantment.KNOCKBACK, 2).setAmount(1).toItemStack(),
				new ItemCreation(Material.GOLD_SWORD).addEnchantment(Enchantment.FIRE_ASPECT, 1).setAmount(1).toItemStack(),
				new ItemCreation(Material.IRON_SWORD).setAmount(1).toItemStack()
				));
		this.chestItems.customInput(Type.SUPPORT, Arrays.asList(
				new ItemCreation(Material.ENDER_PEARL).setAmount(2).toItemStack(),
				new ItemCreation(Material.TNT).setDisplayName(CC.RED + "Auto-Ignitable TNT").setAmount(3).toItemStack(),
				new ItemCreation(Material.GOLD_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).setAmount(1).toItemStack(),
				new ItemCreation(Material.IRON_LEGGINGS).setAmount(1).toItemStack(),
				new ItemCreation(Material.IRON_CHESTPLATE).setAmount(1).toItemStack(),
				new ItemCreation(Material.SNOW_BALL).setAmount(14).toItemStack(),
				new ItemCreation(Material.FISHING_ROD).addEnchantment(Enchantment.KNOCKBACK, 1).setAmount(1).toItemStack(),
				new ItemCreation(Material.ARROW).setAmount(7).toItemStack()
				));
		mainIsland.setChestItems(chestItems);
		refresh();
	}
	
	public static void saveWorld() {
		world.save();
	}

	public static Editor getEditor() {
		return Minigames.getInstance().getConfig(FOLDER+"config");
	}

	public static String getName() {
		return "Skywars";
	}
	
	public Island getIsland(User owner) {
		return islands.stream().filter(island -> island.isOwner(owner)).findAny().orElse(null);
	}

	@EventHandler
	public void respawn(PlayerRespawnEvent event) {
		if (isGaming(event.getPlayer())) {
			event.setRespawnLocation(getUser(event.getPlayer()).getGameSpawn());
		}
	}

	@EventHandler
	public void death(EntityDamageEvent event) {
		if (isGaming(event.getEntity().getUniqueId())) {
			if (!getUser(event.getEntity().getUniqueId()).isSpectating()&&event.getCause()==DamageCause.VOID) {
				quit((Player)event.getEntity(), QuitReason.DEATH);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void death(PlayerDeathEvent event) {
		if (isGaming(event.getEntity())) {
			quit(event.getEntity(), QuitReason.DEATH);
		}
	}

	@EventHandler
	public void nameTagGameChange(GameNameTagUpdateEvent event) {
		if (!event.getCurrentGame().equals(this))return;
		if (event.getGameReason() == GameReason.END) {
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
	public int getMaxPlayers() {
		return 12;
	}

	@Override
	public int getRequiredPlayers() {
		return 2;
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
			long count = filter(this::isPlaying).count();
			if (player.getKiller() != null) {
				User killer = getUser(player.getKiller());

				broadcast(CC.RED + player.getName() + " was killed by "+killer.getPlayer().getName()+". "+CC.GRAY + count + " player"+(count == 1 ? "" : "s")+" remain.");
			} else {
				broadcast(CC.RED + player.getName() + " died. "+CC.GRAY + count + " player"+(count == 1 ? "" : "s")+" remain.");
			}
			player.setAllowFlight(true);
			player.setFlying(true);
			user.setSpectating(true);
			player.teleport(user.getGameSpawn());
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
			if (getUsers().stream().filter(this::isPlaying).count() == 1) {
				finish(getUsers().stream().filter(this::isPlaying).findAny().orElse(null));
			}
		}
	}

	private void finish(User user) {
		if (user == null) {
			// TODO ERROR
			throw new NullPointerException("null player "+getUsers().size());
		}
		broadcast(user.getPlayer().getName() + " has won TNT Run.");
		user.getPlayer().getInventory().clear();
		user.getPlayer().setAllowFlight(true);
		user.setSpectating(true);
		forEachUser(u -> {
			if (u.isSpectating())user.getPlayer().showPlayer(u.getPlayer());
		});

		GameNameTagUpdateEvent e = new GameNameTagUpdateEvent(CoreAPI.getInstance().getProfile(user.getPlayer()), GameNameTagUpdateEvent.GameReason.END, this);
		Bukkit.getPluginManager().callEvent(e);

		Bukkit.getScheduler().runTaskLater(Minigames.getInstance(), () -> {
			end();
		}, 5*20);
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
		
		

		forEachUser(user -> {
			setGameSpawn(user);
			
			getIsland(user).spawnItems();
			
			Profile profile = CoreAPI.getInstance().getProfile(user.getPlayer());
			GameNameTagUpdateEvent e = new GameNameTagUpdateEvent(profile, GameNameTagUpdateEvent.GameReason.START, this);
			Bukkit.getPluginManager().callEvent(e);

			user.setSpectating(false);
			user.getPlayer().teleport(user.getGameSpawn());
		});
		
		mainIsland.spawnItems();

		new BukkitRunnable() {
			int time = 3;
			public void run() {
				if (time == 0) {
					cancel();
					forEachUser(user -> {
						user.sendTitle(TitleAction.TITLE, CC.GREEN+"GO");
						user.sendTitle(TitleAction.SUBTITLE, "");
						WorldEditUtils.clear(getIsland(user).getCage());
						new BukkitRunnable() {
							public void run() {
								CoreAPI.getInstance().getProfile(user.getPlayer()).setInvincible(false);
							}
						}.runTaskLater(Minigames.getInstance(), 30);
					});
					return;
				} else {
					forEachUser(user -> {
						user.sendTitle(TitleAction.TITLE, CC.YELLOW+time);
						user.sendTitle(TitleAction.SUBTITLE, "");
					});
				}
				time--;
			}
		}.runTaskTimer(Minigames.getInstance(), 10, 20);
		
		new BukkitRunnable() {
			private long upgradeTime = startTime+(5*60*1000);
			public void run() {
				if (upgradeTime <= System.currentTimeMillis()) {
					cancel();
					islands.stream().forEach(island -> {
						island.refillChest();
					});
					mainIsland.refillChest();
					forEachUser(user -> {
						user.sendTitle(TitleAction.TITLE, CC.GREEN+"Chest have been refilled.");
						user.sendTitle(TitleAction.SUBTITLE, "");
					});
					return;
				}
			}
		}.runTaskTimer(Minigames.getInstance(), 1, 1);
	}

	@Override
	public void end() {
		setGameState(GameState.LOBBY);
		world.getPlayers().stream().filter(player -> !isGaming(player)).forEach(Minigames.getInstance().getManagerHandler().getUserManager()::sendToSpawn);
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

		this.users.clear();
		this.islands.clear();
		this.lobbySpawn = null;
		HandlerList.unregisterAll(this);
		if (!Bukkit.getServer().unloadWorld(world.getName(), false)) {
			System.out.println("ERROR UNLOADING WORLD");
		}
		Bukkit.getServer().createWorld(new WorldCreator(world.getName()));
		refresh();
	}

	@Override
	public void setGameSpawn(User user) {
		if (user.getGameSpawn() == null) {
			Island island = this.islands.stream().filter(isl -> !isl.isInUse()).findAny().orElse(null);
			if (island == null) {
				throw new IndexOutOfBoundsException("Size of Islands: "+islands.size() + ". Players in game: "+getUsers().size());
			}
			island.setOwner(user);
		}
		// TODO ERROR
	}

	@Override
	public String getTodoList() {
		String list = "";
		READY = world!=null&&lobbySpawn != null&&islands!=null;
		if (!Skywars.READY) {
			list += CC.DARKRED + getName() + CC.RED+ " is not ready:\n"
					+ CC.GRAY+ "Lobby Location Set? "+(lobbySpawn!=null ? CC.GREEN +"True" : CC.RED +"False")+"\n"
					+ CC.GRAY+ "World Set? "+(world!=null ? CC.GREEN +"True" : CC.RED +"False")+"\n"
					+ CC.GRAY+ "Islands Set? "+(islands!=null ? CC.GREEN +"True" : CC.RED +"False")+"\n";
		} else {
			list += CC.GREEN+ getName() + " is up and ready to go.";
		}
		return list;
	}

	@Override
	public boolean isReady() {
		return READY;
	}

	@Override
	public void refresh() {
		if (getEditor().getConfig().contains("world")) {
			world = Bukkit.getWorld(getEditor().getConfig().getString("world"));
			world.setAutoSave(false);
		}
		
		this.lobbySpawn = (Location) Minigames.getGson().fromJson(getEditor().getConfig().getString("lobby-location"), new TypeToken<Location>() {}.getType());
		this.islands = (Set<Island>) Sets.newHashSet(((Set<?>)Minigames.getGson().fromJson(getEditor().getConfig().getString("islands"), new TypeToken<Set<?>>() {}.getType())).toArray(new Island[0]));
		READY = world != null &&lobbySpawn !=null&&islands!=null;
	}

	@Override
	public String toString() {
		return getName();
	}
}
