package me.commonsenze.lobby.Managers;

import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

import com.mongodb.client.MongoClient;

import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Util.CC;
import me.commonsenze.lobby.Lobby;

public class Manager implements Listener {

	protected ManagerHandler managerHandler;

    protected Manager(ManagerHandler managerHandler) {
        this.managerHandler = managerHandler;
    }

    protected Lobby getPlugin() {
        return managerHandler.getPlugin();
    }

    protected void print(String string) {
        getPlugin().getServer().getConsoleSender().sendMessage(CC.translate(string));
    }

    protected MongoClient getConnection() {
        return managerHandler.getMongoManager().getConnection();
    }
    
    protected Editor getEditor(String name) {
    	return getPlugin().getConfig(name);
    }

    protected BukkitScheduler getScheduler() {
        return getPlugin().getServer().getScheduler();
    }
    
    protected void registerAsListener() {
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }
}
