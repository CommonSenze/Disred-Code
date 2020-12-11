package me.commonsenze.minigames.Menu;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import me.commonsenze.core.Lang;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Abstracts.Menu;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.AmongUs.AmongUs;
import me.commonsenze.minigames.Games.AmongUs.Objects.Meeting;
import me.commonsenze.minigames.Objects.User;
import net.md_5.bungee.api.ChatColor;

@Getter
public class MeetingMenu extends Menu {

	private AmongUs game;
	private Map<VotingOption, Set<User>> votes;
	private Map<User, Integer> voted;
	private VotingOption skipVote;
	private boolean tallyingVotes;
	private int timer;
	private Reason reason;


	public MeetingMenu(Player player, AmongUs game) {
		super("Meeting Menu", player, 54);
		this.game = game;
		this.votes = new HashMap<>();
		this.voted = new HashMap<>();
		this.skipVote = new VotingOption(null);
		this.timer = 120;
		this.setMultipleInputs(true);
	}

	private void startTimer() {
		new BukkitRunnable() {
			public void run() {
				if (isTallyingVotes()||!exists()) {
					cancel();
					return;
				}
				if (timer == 0) {
					cancel();
					tallyVotes();
					return;
				}
				timer--;
				update();
			}
		}.runTaskTimer(Minigames.getInstance(), 0, 20);
	}

	public List<Integer> getSlots(){
		return Arrays.asList(1, 5, 10, 14, 19, 23, 28, 32, 37, 41);
	}

	public VotingOption getOption(User user) {
		return this.votes.keySet().stream().filter(option -> option.isUser(user)).findFirst().orElse(null);
	}

	public boolean isOption(User user) {
		return getOption(user) != null;
	}

	public User getMostVoted() {
		if (voted.isEmpty()) {
			reason = Reason.SKIPPED;
			return null;
		}
		int highestVotes = 0;
		User user = null;
		boolean tie = false, skip = false;
		for (VotingOption option : votes.keySet()) {
			if ((user == null&&!skip)||votes.get(option).size() > highestVotes) {
				user = option.getUser();
				skip = option.isSkip();
				highestVotes = votes.get(option).size();
				tie = false;
				continue;
			}
			if (votes.get(option).size() == highestVotes) {
				tie = true;
				continue;
			}
		}
		if (tie) user = null;
		reason = (tie ? Reason.TIED : (user == null ? Reason.SKIPPED : Reason.VOTED));
		return user;
	}
	
	private boolean canVote(User user) {
		return !voted.containsKey(user)&&!user.isSpectating();
	}

	private void tallyVotes() {
		User user = getMostVoted();
		game.forEachUser(u -> {
			addViewer(u.getPlayer());
		});
		setUncloseable(true);
		
		new BukkitRunnable() {
			public void run() {
				for (VotingOption option : new HashSet<>(votes.keySet())) {
					for (User u : new HashSet<>(votes.get(option))) {
						if (getItem(voted.get(u)) == null||getItem(voted.get(u)).getType() != Material.SKULL_ITEM)
							setItem(voted.get(u), new ItemCreation(Material.SKULL_ITEM).setDurability((short)SkullType.WITHER.ordinal()).setDisplayName(CC.YELLOW + "Voters:")
									.addLore("- "+u.getPlayer().getName())
									.toItemStack());
						else {
							ItemCreation creation = new ItemCreation(getItem(voted.get(u)));
							creation.setAmount(creation.toItemStack().getAmount()+1);
							setItem(voted.get(u), creation
									.addLore("- "+u.getPlayer().getName())
									.toItemStack());
						}
						votes.get(option).remove(u);
						break;
					}
					if (votes.get(option).isEmpty()) {
						votes.remove(option);
					}
				}
				if (votes.isEmpty()) {
					setUncloseable(false);
					Bukkit.getScheduler().runTaskLater(Minigames.getInstance(), () -> {
						MeetingMenu.this.getGame().getCurrentMeeting().end(user, reason);
					}, 20);
					cancel();
					return;
				}
			}
		}.runTaskTimer(Minigames.getInstance(), 0, 20);
	}

	@Override
	public void open() {
		super.open();
		startTimer();
	}
	
	@Override
	public Menu create() {
		int slot = 0;
		List<Integer> slots = getSlots();
		for (User user : this.getGame().getUsers().stream().sorted(new Comparator<User>() {

			@Override
			public int compare(User u1, User u2) {
				int comp = 0;
				if ((comp = Boolean.compare(u2.isOnline(), u1.isOnline())) != 0) {
					return comp;
				}
				return Boolean.compare(!u2.isSpectating(), !u1.isSpectating()) ;
			}
		}).collect(Collectors.toList())) {
			setItem(slots.get(slot), new ItemCreation(Material.SKULL_ITEM).setDisplayName((voted.containsKey(user) ? CC.GREEN + CC.BOLD + "VOTED " :"") +CC.BLUE + user.getPlayer().getName())
					.addLore("Click to vote out, "+user.getPlayer().getName()+".").setOwner(user.getPlayer().getName())
					.toItemStack());
			setItem(slots.get(slot)+1, new ItemCreation(Material.STAINED_GLASS_PANE).setDisplayName((getGame().isPlaying(user) ? CC.GREEN + "ALIVE" : CC.RED + "DEAD"))
					.setDurability((short)(getGame().isPlaying(user) ? 5 : 14))
					.toItemStack());
			if (getGame().isPlaying(user))
				votes.put(new VotingOption(user), new HashSet<>());
			slot++;
		}
		this.votes.put(skipVote, new HashSet<>());
		setItem(45, new ItemCreation(Material.WATCH)
				.setDisplayName(CC.GRAY + "Time Remaining: "+ CC.GOLD + timer)
				.addLore("Vote before time is up.")
				.toItemStack());
		setItem(49, new ItemCreation(Material.SKULL_ITEM)
				.setDisplayName(CC.GRAY + "Skip Vote")
				.addLore("Click to skip voting.")
				.toItemStack());
		if (getGame().getCurrentMeeting().getReason() == Meeting.Reason.BODY)
			setItem(53, new ItemCreation(Material.SKULL_ITEM)
					.setDisplayName(CC.GRAY + "Body Reported: "+CC.RED+getGame().getCurrentMeeting().getReported().getPlayer().getName())
					.addLore("The player that was killed.")
					.toItemStack());
		fill(7);
		return this;
	}

	@Override
	public void update() {
		setItem(45, new ItemCreation(Material.WATCH)
				.setDisplayName(CC.GRAY + "Time Remaining: "+ CC.GOLD + timer)
				.addLore("Vote before time is up.")
				.toItemStack());
	}

	@Override
	public void click(InventoryClickEvent e) {
		User user = Minigames.getInstance().getManagerHandler().getUserManager().getUser(e.getWhoClicked().getUniqueId());
		ItemStack item = e.getCurrentItem();
		String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

		if (!isTallyingVotes()&&canVote(user)) {
			if (item.getType() == Material.SKULL_ITEM) {
				SkullMeta meta = (SkullMeta) item.getItemMeta();
				if (meta.hasOwner()) {
					User vote = Minigames.getInstance().getManagerHandler().getUserManager().getUser(CoreAPI.getInstance().getCache().getUUID(meta.getOwner()));
					if (vote != null&&isOption(vote)) {
						confirm(user.getPlayer(), confirm -> {
							if (confirm) {
								System.out.println(user.getPlayer().getName() + " voted for "+vote.getPlayer().getName());
								this.votes.get(getOption(vote)).add(user);
								this.voted.put(user, e.getSlot()+2);
								user.getPlayer().sendMessage(Lang.success("-nYou voted for "+vote.getPlayer().getName()));
								this.getGame().broadcast(Lang.success("-e"+user.getPlayer().getName()+  " -nhas voted."));
								if (this.getGame().getUsers().stream().filter(this.getGame()::isPlaying).count() == voted.size()) {
									tallyVotes();
								} else {
									user.getPlayer().openInventory(MeetingMenu.this.getInventory());
								}
							} else {
								user.getPlayer().openInventory(MeetingMenu.this.getInventory());
							}
						}, "Vote "+vote.getPlayer().getName(), "Undo Vote");
					}
				}
				if (name.contains("Skip Vote")) {
					confirm(user.getPlayer(), confirm -> {
						if (confirm) {
							System.out.println(user.getPlayer().getName() + " voted to skip");
							this.getGame().broadcast(Lang.success("-e"+user.getPlayer().getName()+  " -nhas voted."));
							this.votes.get(skipVote).add(user);
							this.voted.put(user, e.getSlot()+2);

							if (this.getGame().getUsers().stream().filter(this.getGame()::isPlaying).count() == voted.size()) {
								tallyVotes();
							} else {
								user.getPlayer().openInventory(MeetingMenu.this.getInventory());
							}
						} else {
							user.getPlayer().openInventory(MeetingMenu.this.getInventory());
						}
					}, "Confirm Skip", "Undo Skip");
				}
			}
		}
	}

	public enum Reason {
		VOTED, SKIPPED, TIED, CUSTOM;
	}

	private class VotingOption {
		@Getter private User user;

		public VotingOption(User user) {
			this.user = user;
		}

		public boolean isUser(User user) {
			return user.equals(this.user);
		}

		public boolean isSkip() {
			return user == null;
		}
	}
}
