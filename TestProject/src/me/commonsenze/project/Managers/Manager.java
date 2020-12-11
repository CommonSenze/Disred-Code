package me.commonsenze.project.Managers;

import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

import me.commonsenze.project.Core;
import me.commonsenze.project.Util.CC;

public class Manager implements Listener {

	protected ManagerHandler managerHandler;

    protected Manager(ManagerHandler managerHandler) {
        this.managerHandler = managerHandler;
    }

    protected Core getPlugin() {
        return managerHandler.getPlugin();
    }

    protected void print(String string) {
        getPlugin().getServer().getConsoleSender().sendMessage(CC.translate(string));
    }

    protected BukkitScheduler getScheduler() {
        return getPlugin().getServer().getScheduler();
    }
    
    protected void registerAsListener() {
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }
}
