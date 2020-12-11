package me.commonsenze.minigames.Games.AmongUs.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;

import lombok.Getter;
import me.commonsenze.core.API.CoreAPI;
import me.commonsenze.core.Abstracts.Menu;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Util.CC;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.Game.QuitReason;
import me.commonsenze.minigames.Games.AmongUs.AmongUs;
import me.commonsenze.minigames.Games.AmongUs.AmongUs.Imposter;
import me.commonsenze.minigames.Menu.MeetingMenu;
import me.commonsenze.minigames.Objects.User;

@Getter
public class Meeting {

	private AmongUs game;
	private User reporter, reported;
	private Reason reason;
	private Menu menu;

	public Meeting(AmongUs game, User reporter, User reported, Reason reason) {
		this.game = game;
		this.reporter = reporter;
		this.reported = reported;
		this.reason = reason;
	}

	public void call() {
		this.menu = new MeetingMenu(reporter.getPlayer(), game).create();
		menu.disableDeleteOnClose();
		this.getGame().forEachUser(user -> {
			if (reason == Reason.BODY) {
				user.sendTitle(TitleAction.TITLE, CC.AQUA + "Body Reported");
				user.sendTitle(TitleAction.SUBTITLE, "");
			} else {
				user.sendTitle(TitleAction.TITLE, CC.AQUA + "Emergency Meeting");
				user.sendTitle(TitleAction.SUBTITLE, "");
			}
			user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 9999*20, 0));
			Bukkit.getScheduler().runTaskLater(Minigames.getInstance(), () -> {
				user.sendTitle(TitleAction.TITLE, CC.GRAY + "Discuss");
				user.sendTitle(TitleAction.SUBTITLE, "");
				user.getPlayer().teleport(user.getGameSpawn());
				CoreAPI.getInstance().getProfile(user.getPlayer()).setFrozen(true);
				giveItems(user.getPlayer());
			}, 2*20);
		});
		Bukkit.getScheduler().runTaskLater(Minigames.getInstance(), () -> {
			menu.open();
			this.getGame().forEachUser(user -> {
				if (!reporter.equals(user))
					menu.addViewer(user.getPlayer());
			});
		}, 3*20);
	}

	public void end(User voted, MeetingMenu.Reason reason) {
		if (reason != MeetingMenu.Reason.CUSTOM) {
			this.getGame().forEachUser(user -> {
				user.getPlayer().closeInventory();
				user.sendTitle(TitleAction.TITLE, "");
				if (voted != null)
					user.sendTitle(TitleAction.SUBTITLE, CC.GRAY + voted.getPlayer().getName() + " was ejected.");
				else if (reason == MeetingMenu.Reason.SKIPPED||reason == MeetingMenu.Reason.TIED) user.sendTitle(TitleAction.SUBTITLE, CC.GRAY + "No one was ejected.");
				if (!user.isSpectating()) {
					user.getPlayer().getInventory().clear();
					this.getGame().giveItems(user.getPlayer());
				}
				CoreAPI.getInstance().getProfile(user.getPlayer()).setFrozen(false);
			});
			Bukkit.getScheduler().runTaskLater(Minigames.getInstance(), () -> {
				if (voted != null) {
					this.getGame().quit(voted.getPlayer(), QuitReason.CUSTOM);
				}
				this.getGame().setCurrentMeeting(null);
			}, 3*20);
		} else {
			this.getGame().forEachUser(user -> {
				user.getPlayer().closeInventory();
				CoreAPI.getInstance().getProfile(user.getPlayer()).setFrozen(false);
			});
		}
		this.getGame().forEachUser(user -> {
			if (this.getGame().isImposter(user)) {
				Imposter imposter = this.getGame().getImposter(user);
				imposter.setKillCooldown(this.getGame().getKillCooldown());
			}
			user.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
		});
		this.getGame().clearCurrentNPCs();
		this.getGame().setMeetingCooldown(System.currentTimeMillis()+15000);
		this.menu.delete();
	}

	private void giveItems(Player player) {
		player.getInventory().clear();
		player.getInventory().setItem(4, new ItemCreation(Material.CHEST).setDisplayName(CC.GOLD + "Open Voting Menu "+CC.GRAY + "(Right Click)")
				.addLore("Click this item to vote for a player.")
				.toItemStack());
	}

	public enum Reason {
		BODY, EMERGENCY;
	}
}
