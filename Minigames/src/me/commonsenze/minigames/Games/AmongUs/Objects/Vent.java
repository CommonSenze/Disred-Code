package me.commonsenze.minigames.Games.AmongUs.Objects;

import java.io.File;

import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import lombok.Setter;
import me.commonsenze.core.Interfaces.Editor;
import me.commonsenze.core.Objects.Cuboid;
import me.commonsenze.minigames.Minigames;
import me.commonsenze.minigames.Games.AmongUs.AmongUs;

@Getter
public class Vent {

	public static final String VENT_FOLDER = AmongUs.FOLDER + "Vents"+File.separator;
	
	@Setter private Cuboid bounds;
	private Editor editor;
	private boolean deleted;
	
	public Vent(Editor editor) {
		this.editor = editor;
		this.bounds = Minigames.getGson().fromJson(editor.getConfig().getString("bounds"), new TypeToken<Cuboid>() {}.getType());
	}
	
	public Vent(Cuboid bounds) {
		this.editor = Minigames.getInstance().getConfig(VENT_FOLDER+bounds.getWorld().getName()+"-"+bounds.getLowerX()+"-"+bounds.getLowerY()+"-"+bounds.getLowerZ());
		this.bounds = bounds;
		save();
	}
	
	public void save() {
		if (deleted)return;
		editor.getConfig().set("bounds", Minigames.getGson().toJson(bounds));
		editor.saveConfig();
	}
	
	public void delete() {
		editor.getFile().delete();
		Minigames.getInstance().getManagerHandler().getVentManager().remove(this);
		this.deleted = true;
	}
}
