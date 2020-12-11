package me.commonsenze.kitpvp.Managers;

import lombok.Getter;
import me.commonsenze.kitpvp.KitPvP;
import me.commonsenze.kitpvp.Managers.impl.MongoManager;
import me.commonsenze.kitpvp.Managers.impl.UserManager;

@Getter
public class ManagerHandler {

	private KitPvP plugin;
	private MongoManager mongoManager;
	private UserManager userManager;

	public ManagerHandler(KitPvP plugin) {
		this.plugin = plugin;
		this.loadManagers();
	}

	private void loadManagers() {
		this.mongoManager = new MongoManager(this);
		this.userManager = new UserManager(this);
	}

	public void save() {
		
	}
}
