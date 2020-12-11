package me.commonsenze.core.Interfaces;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

public interface Editor {

	FileConfiguration getConfig();
	
	void reloadConfig();
	void saveConfig();
	
	String getMessage(String key);
	List<String> getMessages(String key);

	File getFile();

	void delete();
}
