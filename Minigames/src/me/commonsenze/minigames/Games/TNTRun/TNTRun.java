package me.commonsenze.minigames.Games.TNTRun;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.google.gson.reflect.TypeToken;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Objects.TeamBuilder;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.Game;
import me.commonsenze.minigames.Games.GameNameTagUpdateEvent;
import me.commonsenze.minigames.Games.GameNameTagUpdateEvent.GameReason;
import me.commonsenze.minigames.Games.GameState;
import me.commonsenze.minigames.Objects.User;

@Getter @Setter
public class TNTRun implements Game {

	private static World world;
	private String id;
	private GameState gameState;
	private Set<User> users;
	private Location lobbySpawn, gameSpawn;
	private Team spectatorTeam;
	@Setter(value = AccessLevel.NONE) private long startTime;

	private static boolean READY;

	public static final String FOLDER = Game.FOLDER + "TNTRun"+File.separator;

	public TNTRun() {
		this.users = new HashSet<>();
		this.id = getName().replaceAll(" ", "").toLowerCase();
		refresh();
	}
	
	public static void saveWorld() {
		world.save();
	}

	public static Editor getEditor() {
		return Minigames.getInstance().getConfig(FOLDER+"config");
	}

	public static String getName() {
		return "TNT Run";
	}

	public void finish(User user) {
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

	public void startTimer(Player player) {
		new BukkitRunnable() {
			public void run() {
				if (getGameState() != GameState.STARTED||getUser(player).isSpectating()) {
					cancel();
					return;
				}
				Location loc = player.getLocation();
				Block block = getRealLocation(loc.subtract(0, 2, 0));
				if (block.getType() == Material.TNT) {
					Bukkit.getScheduler().runTaskLater(Minigames.getInstance(), () -> {
						block.getRelative(BlockFace.UP).setType(Material.AIR);
						block.setType(Material.AIR);
					}, 5);
				}
			}
		}.runTaskTimer(Minigames.getInstance(), 3, 3);
	}

	public Block getRealLocation(Location loc) {
		if (loc.getBlock().getType() != Material.AIR)
			return loc.getBlock();
		Block b1 = loc.add(1,0,0).getBlock();
		Block b2 = loc.add(-1,0,0).getBlock();
		Block b3 = loc.add(0,0,1).getBlock();
		Block b4 = loc.add(0,0,-1).getBlock();
		Block b5 = loc.add(1,0,1).getBlock();
		Block b6 = loc.add(-1,0,1).getBlock();
		Block b7 = loc.add(-1,0,-1).getBlock();
		Block b8 = loc.add(1,0,-1).getBlock();

		Block block = b1;
		double closest = loc.distanceSquared(b1.getLocation());

		double temp = 0;

		if (b2.getType() != Material.AIR)
			if ((temp = loc.distanceSquared(b2.getLocation())) < closest) {
				closest = temp;
				block = b2;
			}

		if (b3.getType() != Material.AIR)
			if ((temp = loc.distanceSquared(b3.getLocation())) < closest) {
				closest = temp;
				block = b3;
			}

		if (b4.getType() != Material.AIR)
			if ((temp = loc.distanceSquared(b4.getLocation())) < closest) {
				closest = temp;
				block = b4;
			}
		
		if (b5.getType() != Material.AIR)
			if ((temp = loc.distanceSquared(b5.getLocation())) < closest) {
				closest = temp;
				block = b5;
			}

		if (b6.getType() != Material.AIR)
			if ((temp = loc.distanceSquared(b6.getLocation())) < closest) {
				closest = temp;
				block = b6;
			}

		if (b7.getType() != Material.AIR)
			if ((temp = loc.distanceSquared(b7.getLocation())) < closest) {
				closest = temp;
				block = b7;
			}
		
		if (b8.getType() != Material.AIR)
			if ((temp = loc.distanceSquared(b8.getLocation())) < closest) {
				closest = temp;
				block = b8;
			}

		return block;
	}

	@EventHandler
	public void damage(EntityDamageEvent event) {
		if (isGaming(event.getEntity().getUniqueId())&&!getUser(event.getEntity().getUniqueId()).isSpectating()&&event.getCause()==DamageCause.VOID) {
			quit((Player)event.getEntity(), QuitReason.DEATH);
			event.setCancelled(true);
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

			Profile profile = CoreAPI.getInstance().getProfile(user.getPlayer());
			GameNameTagUpdateEvent e = new GameNameTagUpdateEvent(profile, GameNameTagUpdateEvent.GameReason.START, this);
			Bukkit.getPluginManager().callEvent(e);

			user.setSpectating(false);
			user.getPlayer().teleport(user.getGameSpawn());


		});

		new BukkitRunnable() {
			int time = 3;
			public void run() {
				if (time == 0) {
					cancel();
					forEachUser(user -> {
						user.sendTitle(TitleAction.TITLE, CC.GREEN+"GO");
						user.sendTitle(TitleAction.SUBTITLE, "");
					});
					forEachUser(user -> startTimer(user.getPlayer()));
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
		
		this.lobbySpawn = null;
		this.gameSpawn = null;
		HandlerList.unregisterAll(this);
		if (!Bukkit.getServer().unloadWorld(world.getName(), false)) {
			System.out.println("ERROR UNLOADING WORLD");
		}
		Bukkit.getServer().createWorld(new WorldCreator(world.getName()));
		refresh();
	}

	@Override
	public String getTodoList() {
		String list = "";
		READY = world != null&&lobbySpawn != null&&gameSpawn!=null;
		if (!TNTRun.READY) {
			list += CC.DARKRED + getName() + CC.RED+ " is not ready:\n"
					+ CC.GRAY+ "Game Location Set? "+(gameSpawn!=null ? CC.GREEN +"True" : CC.RED +"False")+"\n"
					+ CC.GRAY+ "Lobby Location Set? "+(lobbySpawn!=null ? CC.GREEN +"True" : CC.RED +"False")+"\n"
					+ CC.GRAY+ "World Set? "+(world!=null ? CC.GREEN +"True" : CC.RED +"False")+"\n";
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
		this.gameState = GameState.LOBBY;
		if (getEditor().getConfig().contains("world")) {
			world = Bukkit.getWorld(getEditor().getConfig().getString("world"));
			world.setAutoSave(false);
		}
		this.lobbySpawn = (Location) Minigames.getGson().fromJson(getEditor().getConfig().getString("lobby-location"), new TypeToken<Location>() {}.getType());
		this.gameSpawn = (Location) Minigames.getGson().fromJson(getEditor().getConfig().getString("game-location"), new TypeToken<Location>() {}.getType());
		this.spectatorTeam = new TeamBuilder("spectators").setPrefix(CC.GRAY).setNameTagVisibility(NameTagVisibility.NEVER).setCanSeeFriendlyInvisibles(true).toTeam();

		READY = world != null&&lobbySpawn != null&&gameSpawn!=null;
	}

	@Override
	public void setGameSpawn(User user) {
		if (user.getGameSpawn() == null)
			user.setGameSpawn(getGameSpawn());
		// TODO ERROR
	}

	@Override
	public String toString() {
		return getName();
	}
}
