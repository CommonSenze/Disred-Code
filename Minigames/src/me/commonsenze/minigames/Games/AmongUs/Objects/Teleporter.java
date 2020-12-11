package me.commonsenze.minigames.Games.AmongUs.Objects;

import java.io.File;

import org.bukkit.Location;

import com.google.gson.reflect.TypeToken;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.AmongUs.AmongUs;

@Getter @Setter
public class Teleporter {

	@Setter(value = AccessLevel.NONE) private Editor editor;
	public static final String TELEPORTER_FOLDER = AmongUs.FOLDER + "Teleporters"+File.separator;
	private Location first, second;
	@Setter(value = AccessLevel.NONE) private boolean deleted;
	
	public Teleporter(Editor editor) {
		this.editor = editor;
		this.first = Minigames.getGson().fromJson(editor.getConfig().getString("locations.first"), new TypeToken<Location>() {}.getType());
		this.second = Minigames.getGson().fromJson(editor.getConfig().getString("locations.second"), new TypeToken<Location>() {}.getType());
	}
	
	public Teleporter(Location first, Location second) {
		this.editor = Minigames.getInstance().getConfig(TELEPORTER_FOLDER+first.getWorld().getName()+"-"+first.getBlockX()+"-"+first.getBlockY()+"-"+first.getBlockZ());
		this.first = first;
		this.second = second;
		save();
	}
	
	public void save() {
		if (deleted)return;
		editor.getConfig().set("locations.first", Minigames.getGson().toJson(first));
		editor.getConfig().set("locations.second", Minigames.getGson().toJson(second));
		editor.saveConfig();
	}
	
	public void delete() {
		editor.getFile().delete();
		Minigames.getInstance().getManagerHandler().getTeleporterManager().remove(this);
		this.deleted = true;
	}
}
