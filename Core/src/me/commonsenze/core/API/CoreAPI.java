package me.commonsenze.core.API;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.topcat.npclib.NPCManager;

import lombok.Getter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Interfaces.Cache;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Interfaces.ScoreboardAdapter;
import me.commonsenze.core.Managers.ManagerHandler;
import me.commonsenze.core.Objects.Profile;
import me.commonsenze.core.Objects.Rank;

public class CoreAPI {

	private Core plugin;
    @Getter private static CoreAPI instance;

    public CoreAPI(Core plugin) {
        CoreAPI.instance = this;
        this.plugin = plugin;
    }
    
    public Cache getCache() {
        return plugin.getCache();
    }
    
    public NPCManager getNPCManager() {
    	return plugin.getNpcManager();
    }

    public Profile getProfile(Player player) {
        return plugin.getManagerHandler().getProfileManager().getProfile(player);
    }

    public Set<Profile> getProfiles() {
        return plugin.getManagerHandler().getProfileManager().getProfileSet();
    }

    public List<Profile> getSortedProfiles() {
        return plugin.getManagerHandler().getProfileManager().getSortedProfiles();
    }
    
    public List<Profile> getPermissionedProfiles(String permission) {
        return plugin.getManagerHandler().getProfileManager().getPermissionedProfiles(permission);
    }
    
    public ManagerHandler getManagerHandler() {
    	return plugin.getManagerHandler();
    }

    public Rank getRank(String name) {
        return plugin.getManagerHandler().getRankManager().getRank(name);
    }

    public Set<Rank> getRanks() {
        return plugin.getManagerHandler().getRankManager().getRanks();
    }

    public List<Rank> getSortedRanks() {
        return plugin.getManagerHandler().getRankManager().getSortedRanks();
    }

    public Rank getOfflineUserRank(UUID uuid) {
        return plugin.getManagerHandler().getProfileManager().getRank(uuid);
    }

    public List<String> getOfflineUserPermissions(UUID uuid) {
        return plugin.getManagerHandler().getProfileManager().getOfflinePermissions(uuid);
    }

    public Location getSpawnLocation() {
        return plugin.getManagerHandler().getServerManager().getSpawn();
    }
    
    public void registerScoreboard(ScoreboardAdapter scoreboard) {
    	plugin.getManagerHandler().getScoreboardAPI().addScoreboard(scoreboard);
    }

	public Editor getConfig(String name) {
		return plugin.getConfig(name);
	}

	public void unregisterScoreboard(ScoreboardAdapter scoreboard) {
		plugin.getManagerHandler().getScoreboardAPI().removeScoreboard(scoreboard);
	}
}
