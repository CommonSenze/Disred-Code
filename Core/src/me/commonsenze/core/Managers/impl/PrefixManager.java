package me.commonsenze.core.Managers.impl;

import me.commonsenze.core.Managers.Manager;
import me.commonsenze.core.Managers.ManagerHandler;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Util.CC;

public class PrefixManager extends Manager {

	public PrefixManager(ManagerHandler managerHandler) {
		super(managerHandler);
	}

	public String getRankPrefix(Profile profile) {
		return profile.hasRank() ? CC.translate(profile.getRank().getTabColor()) : "";
	}
	
	public String getVanishPrefix(Profile profile) {
		return profile.isVanish() ? CC.ITALIC : "";
	}

	public String getPrefix(Profile profile) {
		return getRankPrefix(profile)+getVanishPrefix(profile);
	}
}
