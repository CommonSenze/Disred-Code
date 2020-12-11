package me.commonsenze.minigames.Managers.impl;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Managers.Manager;
import me.commonsenze.minigames.Managers.ManagerHandler;


public class MongoManager extends Manager {

	public static final String DATABASE = "lobby";
	public static final String PLAYER_COLLECTION = "users";
	
    private MongoClient client;
    
    public MongoManager(ManagerHandler managerHandler) {
		super(managerHandler);
		this.load();
	}
    
    public void load() {
    	synchronized (this) {
    		Editor editor = Minigames.getInstance().getConfig("config");
        	editor.getConfig().addDefault("Mongo.user", "admin");
        	editor.getConfig().addDefault("Mongo.password", "223admin");
        	editor.getConfig().options().copyDefaults(true);
        	editor.saveConfig();
            this.client = MongoClients.create(
            	    "mongodb+srv://"+editor.getConfig().getString("Mongo.user")+":"+editor.getConfig().getString("Mongo.password")+"@mc.bf6id.mongodb.net/?retryWrites=true&w=majority");
            Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
            mongoLogger.setLevel(Level.OFF);
		}
    }
    
    public boolean hasCollection(MongoDatabase database, String string){
    	return database.listCollectionNames().into(new ArrayList<String>()).contains(string);
    }
    
    public MongoClient getConnection() {
    	return client;
    }
    
    public void insertOrReplaceOne(String database, String collection, Document obj) throws ConnectException {
        MongoDatabase mongoDatabase = client.getDatabase(database);
        
        
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collection);

        Entry<String, Object> entry = obj.entrySet().iterator().next();
        
        if (mongoCollection.find(Filters.eq(entry.getKey(), entry.getValue())).first() != null)
        	mongoCollection.replaceOne(Filters.eq(entry.getKey(), entry.getValue()), obj);
		else
			mongoCollection.insertOne(obj);
    }
}
