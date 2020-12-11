package me.commonsenze.core.Managers;

import lombok.Getter;
import me.commonsenze.core.Core;
import me.commonsenze.core.Managers.impl.LogManager;
import me.commonsenze.core.Managers.impl.MongoManager;
import me.commonsenze.core.Managers.impl.PrefixManager;
import me.commonsenze.core.Managers.impl.ProfileManager;
import me.commonsenze.core.Managers.impl.RankManager;
import me.commonsenze.core.Managers.impl.ScoreboardTeamManager;
import me.commonsenze.core.Managers.impl.ServerManager;
import me.commonsenze.core.Managers.impl.VanishManager;
import me.commonsenze.core.Scoreboard.ScoreboardAPI;

@Getter
public class ManagerHandler {

    private Core plugin;
    private MongoManager mongoManager;
    private ProfileManager profileManager;
    private RankManager rankManager;
    private ServerManager serverManager;
    private ScoreboardAPI scoreboardAPI;
    private PrefixManager prefixManager;
    private ScoreboardTeamManager scoreboardTeamManager;
    private LogManager logManager;
    private VanishManager vanishManager;

    public ManagerHandler(Core plugin) {
        this.plugin = plugin;
        this.loadManagers();
    }

    private void loadManagers() {
       this.mongoManager = new MongoManager(this);
       this.rankManager = new RankManager(this);
       this.serverManager = new ServerManager(this);
       this.scoreboardAPI = new ScoreboardAPI(plugin);
       this.prefixManager = new PrefixManager(this);
       this.logManager = new LogManager(this);
       this.scoreboardTeamManager = new ScoreboardTeamManager(this);
       this.vanishManager = new VanishManager(this);
       this.profileManager = new ProfileManager(this);
    }
    
    public void save() {
    	profileManager.unload();
    	serverManager.save();
    	rankManager.save();
    }
}
