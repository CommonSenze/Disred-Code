package me.commonsenze.minigames.Managers;

import lombok.Getter;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Managers.impl.GameManager;
import me.commonsenze.minigames.Managers.impl.MongoManager;
import me.commonsenze.minigames.Managers.impl.ServerManager;
import me.commonsenze.minigames.Managers.impl.TeleporterManager;
import me.commonsenze.minigames.Managers.impl.UserManager;
import me.commonsenze.minigames.Managers.impl.VentManager;

@Getter
public class ManagerHandler {

	private Minigames plugin;
	private VentManager ventManager;
	private UserManager userManager;
	private TeleporterManager teleporterManager;
	private MongoManager mongoManager;
	private ServerManager serverManager;
	private GameManager gameManager;

	public ManagerHandler(Minigames plugin) {
		this.plugin = plugin;
		this.loadManagers();
	}

	private void loadManagers() {
		this.mongoManager = new MongoManager(this);
		this.userManager = new UserManager(this);
		this.ventManager = new VentManager(this);
		this.teleporterManager = new TeleporterManager(this);
		this.serverManager = new ServerManager(this);
		this.gameManager = new GameManager(this);
	}

	public void save() {
		this.userManager.unload();
	}
}
