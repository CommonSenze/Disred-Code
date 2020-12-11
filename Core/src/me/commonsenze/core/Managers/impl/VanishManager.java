package me.commonsenze.core.Managers.impl;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.commonsenze.core.Managers.Manager;
import me.commonsenze.core.Managers.ManagerHandler;
import me.commonsenze.core.Objects.Profile;

public class VanishManager extends Manager {
	
	public VanishManager(ManagerHandler managerHandler) {
		super(managerHandler);
		registerAsListener();
	}
	
	public void vanish(Profile profile) {
		for (Profile prof : managerHandler.getProfileManager().getProfileSet()) {
			if (profile.getUniqueId().equals(prof.getUniqueId()))continue;
			if (!prof.hasRank()||(profile.hasRank()&&profile.getRank().hasAuthorityOver(prof.getRank())))
				prof.getPlayer().hidePlayer(profile.getPlayer());
		}
	}
	
	public void unvanish(Profile profile) {
		for (Profile prof : managerHandler.getProfileManager().getProfileSet()) {
			prof.getPlayer().showPlayer(profile.getPlayer());
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent event) {
		Profile profile = managerHandler.getProfileManager().getProfile(event.getPlayer());
		for (Profile prof : managerHandler.getProfileManager().getProfileSet()) {
			if (profile.getUniqueId().equals(prof.getUniqueId()))continue;
			if (!profile.isVanish())continue;
			if (!profile.hasRank()||(prof.hasRank()&&prof.getRank().hasAuthorityOver(profile.getRank())))
				profile.getPlayer().hidePlayer(prof.getPlayer());
		}
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent event) {
		Profile profile = managerHandler.getProfileManager().getProfile(event.getPlayer());
		if (profile.isVanish())profile.setVanish(false);
	}
}
