package me.commonsenze.core.Managers.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Setter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Managers.Manager;
import me.commonsenze.core.Managers.ManagerHandler;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Objects.TextBuilder;
import me.commonsenze.core.Util.CC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@Setter
public class ServerManager extends Manager {

	public static final String SERVER_FILE = "config";

	private Location spawn;

	public ServerManager(ManagerHandler managerHandler) {
		super(managerHandler);
		registerAsListener();
	}

	public void save() {
		Editor editor = getEditor(SERVER_FILE);
		if (spawn != null) {
			editor.getConfig().set("spawn.location.world", spawn.getWorld().getName());
			editor.getConfig().set("spawn.location.x", spawn.getX());
			editor.getConfig().set("spawn.location.y", spawn.getY());
			editor.getConfig().set("spawn.location.z", spawn.getZ());
			editor.getConfig().set("spawn.location.yaw", spawn.getYaw());
			editor.getConfig().set("spawn.location.pitch", spawn.getPitch());
			editor.saveConfig();
		}
	}

	public Location getSpawn() {
		if (spawn == null) {
			FileConfiguration config = getEditor(SERVER_FILE).getConfig();
			Location def = Bukkit.getWorlds().get(0).getSpawnLocation();
			this.spawn = new Location(
					getPlugin().getServer().getWorld(config.getString("spawn.location.world", def.getWorld().getName())),
					config.getDouble("spawn.location.x", def.getBlockX()+0.5),
					config.getDouble("spawn.location.y", def.getBlockY()),
					config.getDouble("spawn.location.z", def.getBlockZ()+0.5),
					(float)config.getDouble("spawn.location.yaw", def.getYaw()),
					(float)config.getDouble("spawn.location.pitch", def.getPitch())
					);
		}
		return spawn;
	}

	@EventHandler
	public void onWeather(WeatherChangeEvent e){
		if (!getEditor(SERVER_FILE).getConfig().getBoolean("weather-enabled", false)) {
			e.setCancelled(e.toWeatherState());
		}
	}

	@EventHandler
	public void onWeather(PlayerKickEvent e){
		if (e.getReason().toLowerCase().contains("disconnect.spam")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void command(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().startsWith("/minecraft"))e.setCancelled(true);
		if (e.getMessage().startsWith("/me"))e.setCancelled(true);
		if (e.getMessage().startsWith("/?"))e.setCancelled(true);
		if (e.getMessage().startsWith("/say"))e.setCancelled(true);
		if (e.getMessage().startsWith("/bukkit"))e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void messaging(AsyncPlayerChatEvent e) {
		Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(e.getPlayer());
		if (e.isCancelled())return;

		if (!e.isCancelled()&&!e.getPlayer().hasPermission("chat.cooldown")) {
			if (profile.getTimerManager().inTimer("Chat Cooldown")) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(Lang.fail("-nChat Cooldown: -e"+new BigDecimal(profile.getTimerManager().getTime("Chat Cooldown")).setScale(1, RoundingMode.HALF_UP).doubleValue()+" -nseconds"));
				return;
			}
			profile.getTimerManager().put("Chat Cooldown", 2);
		}

		e.setCancelled(true);

		if (e.getPlayer().hasPermission("chat.color"))
			e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
		
		profile.updateDisplayName();

		if (referringToPlayer(e.getMessage()))sendPersonalMessages(e);

		TextBuilder format = new TextBuilder();

		format.append(CC.translate(e.getPlayer().getDisplayName()))
		.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to message "+e.getPlayer().getName()).create()))
		.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/message "+e.getPlayer().getName() + " <message...>")).create();

		if (!referringToCommand(e.getMessage()))
		format.append(ChatColor.GRAY + " » "+ChatColor.WHITE+e.getMessage()).create();
		else format.append(ChatColor.GRAY + " » "+ChatColor.WHITE).create().addExtra(getCommandMessage(e.getMessage()));

		Bukkit.getLogger().info(ChatColor.stripColor(format.toPlainText().replaceAll("»", ">")));

		for (Player p : e.getRecipients()){
			p.spigot().sendMessage(format.toTextComponent());
		}
	}

	private boolean referringToPlayer(String message){
		for (Player p : Bukkit.getOnlinePlayers()){
			if (message.toLowerCase().contains(p.getName().toLowerCase())&&!inCommandMessage(message, p.getName()))return true;
		}
		return false;
	}
	
	private boolean inCommandMessage(String message, String name) {
		int playerIndex = message.indexOf(name);
		for (int start = playerIndex-1; start >= 0; start--) {
			if (message.charAt(start) == '"') {
				for (int end = playerIndex+name.length(); end < message.length(); end++) {
					if (message.charAt(end) == '"') return true;
				}
			}
		}
		return false;
	}

	private ChatColor findColor(String message, String substring){
		ChatColor chat = ChatColor.WHITE;
		for (int i = 0; i < message.indexOf(substring)-1; i++){
			if (message.charAt(i) == '§'){
				if (Character.isDigit(message.charAt(i+1))){
					chat = ChatColor.getByChar(message.charAt(i+1));
				} else if (Character.isAlphabetic(message.charAt(i+1))){
					if (getAlphabeticColors().contains(message.charAt(i+1)+""))chat = ChatColor.getByChar(message.charAt(i+1));
				}
			}
		}
		return chat;
	}

	private ArrayList<String> getAlphabeticColors(){
		ArrayList<String> list = new ArrayList<>();
		for (ChatColor cc : ChatColor.values()){
			if (Character.isAlphabetic(cc.toString().charAt(1)))list.add(cc.toString().charAt(1)+"");
		}
		return list;
	}
	
	private boolean referringToCommand(String message){
		int start = message.indexOf('"')+1;
		int end = message.indexOf('"', start);
		if (start == -1 || end == -1)return false;
		return message.substring(start, end).contains("/");
	}
	
	private TextComponent getCommandMessage(String message) {
		int beginning = 0;
		TextBuilder builder = new TextBuilder();
		while (message.indexOf('"',beginning) != -1) {
			int start = message.indexOf('"', beginning);
			int end = message.indexOf('"', start+1);
			
			if (beginning < start) {
				builder.append(message.substring(beginning, start)).create();
			}
			
			String command = message.substring(start+1, end);
			builder.append(command)
			.setColor(ChatColor.AQUA)
			.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to run this command.").create()))
			.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
			.create();
			beginning = end + 1;
		}
		if (beginning < message.length()) {
			builder.append(message.substring(beginning, message.length())).create();
		}
		return builder.toTextComponent();
	}

	private void sendPersonalMessages(AsyncPlayerChatEvent e){
		ArrayList<Player> remove = new ArrayList<>();
		for (Player p : e.getRecipients()){
			if (e.getMessage().contains(p.getName()))remove.add(p);
		}
		e.getRecipients().removeAll(remove);
		for (Player p : remove){
			String message = e.getMessage().replace(p.getName(), ChatColor.GOLD + ChatColor.ITALIC.toString() + p.getName() + findColor(e.getMessage(), p.getName()));
			p.sendMessage(e.getPlayer().getDisplayName() +ChatColor.GRAY+" » "+ChatColor.WHITE+message);
			p.playSound(p.getLocation(), Sound.VILLAGER_YES, 10F, 1F);
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent event) {
		for (Profile profile : Core.getInstance().getManagerHandler().getProfileManager().getProfileSet())
			profile.getPlayer().hidePlayer(event.getPlayer());
		
		new BukkitRunnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers())
					p.showPlayer(event.getPlayer());
			}
		}.runTaskLater(Core.getInstance(), 5);
	}
}
