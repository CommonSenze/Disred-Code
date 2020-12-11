package me.commonsenze.core.Managers.impl;

import java.util.Date;
import java.util.UUID;

import org.bson.Document;

import me.commonsenze.core.Managers.Manager;
import me.commonsenze.core.Managers.ManagerHandler;

public class LogManager extends Manager {

	public enum LogType {
		RANK, BAN, MUTE;
	}
	
	public LogManager(ManagerHandler managerHandler) {
		super(managerHandler);
	}
	
	public void log(LogType type, UUID uuid, String message) {
		Document document = new Document("type", type.name());
		document.append("uuid", uuid.toString());
		document.append("message", message);
		document.append("date", new Date().toString());
		getConnection().getDatabase(MongoManager.DATABASE).getCollection(MongoManager.LOG_COLLECTION).insertOne(document);
	}
}
