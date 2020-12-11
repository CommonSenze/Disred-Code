package me.commonsenze.core.Menus;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Menu;
import me.commonsenze.core.Abstracts.MultiPageMenu;
import me.commonsenze.core.Managers.impl.RankManager;
import me.commonsenze.core.Menus.Ranks.RankInfoMenu;
import me.commonsenze.core.Objects.ItemCreation;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Objects.Rank;
import me.commonsenze.core.Util.CC;
import net.md_5.bungee.api.ChatColor;

public class RankListMenu extends MultiPageMenu {

	@Getter private RankManager rankManager;

	public RankListMenu(Player player) {
		super(player, "Rank List Menu");
	}

	@Override
	public ArrayList<ItemStack> getItems() {
		this.rankManager = Core.getInstance().getManagerHandler().getRankManager();

		ArrayList<ItemStack> items = new ArrayList<>();

		if (getRankManager().getSortedRanks() == null || getRankManager().getSortedRanks().isEmpty()) {
			items.add(new ItemCreation(Material.PAPER).setDisplayName(CC.RED + "No Ranks")
					.addLore("There are currenty no ranks in")
					.addLore("the database at the moment.")
					.toItemStack());
		} else 
			for (Rank rank : getRankManager().getSortedRanks()) {
				items.add(new ItemCreation(Material.CHEST).setDisplayName(CC.translate(rank.getTabColor()) + rank.getName())
						.addLore(CC.STRIKETHROUGH + "-------"+CC.GRAY+" Info "+CC.STRIKETHROUGH+"-------")
						.addLore("Prefix: "+rank.getPrefix())
						.addLore("Ranking: "+rank.getRanking())
						.addLore("Tab Color: "+CC.getColorName(ChatColor.getByChar(rank.getTabColor().charAt(1))))
						.addLore("Default: "+(rank.isDefault() ? CC.GREEN + "True" : CC.RED + "False"))
						.addLore(CC.STRIKETHROUGH + "-------------------")
						.toItemStack());
			}

		return items;
	}

	@Override
	public ArrayList<ItemStack> getSearchQuery(String name) {
		ArrayList<ItemStack> items = new ArrayList<>();

		for (Rank rank : getRankManager().getSortedRanks()) {
			if (rank.getName().toLowerCase().startsWith(name.toLowerCase()))
				items.add(new ItemCreation(Material.CHEST).setDisplayName(CC.translate(rank.getTabColor()) + rank.getName())
						.addLore(CC.STRIKETHROUGH + "-------"+CC.GRAY+" Info "+CC.STRIKETHROUGH+"-------")
						.addLore("Prefix: "+rank.getPrefix())
						.addLore("Ranking: "+rank.getRanking())
						.addLore("Tab Color: "+CC.getColorName(ChatColor.getByChar(rank.getTabColor().charAt(1))))
						.addLore("Default: "+(rank.isDefault() ? CC.GREEN + "True" : CC.RED + "False"))
						.addLore(CC.STRIKETHROUGH + "------------------")
						.toItemStack());
		}
		return items;
	}

	@Override
	public boolean updatable(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();

		if (item.getType() == Material.CHEST) {
			String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
			Rank rank = getRankManager().getRank(name);
			if (rank == null)return false;
			Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile(e.getWhoClicked().getUniqueId());
			if (profile.hasRank()&&profile.getRank().hasAuthorityOver(rank)||e.getWhoClicked().isOp()) {
				delete();
				Menu menu = new RankInfoMenu(getPlayer(), rank).create();
				menu.open();
			} else {
				e.getWhoClicked().sendMessage(Lang.fail("-nYou cannot modify a rank you're under."));
			}
		}
		return false;
	}
}
