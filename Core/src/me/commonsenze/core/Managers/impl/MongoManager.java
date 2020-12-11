package me.commonsenze.core.Managers.impl;

import java.io.File;
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

import me.commonsenze.core.Core;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Managers.Manager;
import me.commonsenze.core.Managers.ManagerHandler;

public class MongoManager extends Manager {

	public static final String DATABASE_FILE = "Database"+File.separator+"config";
	public static final String DATABASE = "core";
	public static final String RANK_COLLECTION = "ranks";
	public static final String PLAYER_COLLECTION = "profiles";
	public static final String LOG_COLLECTION = "logs";
	public static final String BAN_COLLECTION = "bans";
	
    private MongoClient client;
    
    public MongoManager(ManagerHandler managerHandler) {
		super(managerHandler);
		this.load();
	}
    
    public void load() {
    	synchronized (this) {
    		Editor editor = Core.getInstance().getConfig(DATABASE_FILE);
        	editor.getConfig().addDefault("Mongo.user", "admin");
        	editor.getConfig().addDefault("Mongo.password", "223admin");
        	editor.getConfig().options().copyDefaults(true);
        	editor.saveConfig();
            //Connect to the specified ip and port
            //Default is localhost, 27017
            this.client = MongoClients.create(
            	    "mongodb+srv://"+editor.getConfig().getString("Mongo.user")+":"+editor.getConfig().getString("Mongo.password")+"@mc.bf6id.mongodb.net/core?retryWrites=true&w=majority");
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
