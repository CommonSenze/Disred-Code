package me.commonsenze.lobby.Managers;

import lombok.Getter;
import me.commonsenze.lobby.Lobby;
import me.commonsenze.lobby.Managers.impl.MongoManager;
import me.commonsenze.lobby.Managers.impl.ServerManager;
import me.commonsenze.lobby.Managers.impl.UserManager;

@Getter
public class ManagerHandler {

	private Lobby plugin;
	private MongoManager mongoManager;
	private UserManager userManager;
	private ServerManager serverManager;

    public ManagerHandler(Lobby plugin) {
        this.plugin = plugin;
        this.loadManagers();
    }

    private void loadManagers() {
       this.mongoManager = new MongoManager(this);
       this.userManager = new UserManager(this);
       this.serverManager = new ServerManager(this);
    }
}
