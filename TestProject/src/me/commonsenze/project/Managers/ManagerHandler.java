package me.commonsenze.project.Managers;

import lombok.Getter;
import me.commonsenze.project.Core;
import me.commonsenze.project.Managers.impl.TriangleManager;

@Getter
public class ManagerHandler {

	private Core plugin;
	private TriangleManager triangleManager;

	public ManagerHandler(Core plugin) {
		this.plugin = plugin;
		this.loadManagers();
	}

	private void loadManagers() {
		this.triangleManager = new TriangleManager(this);
	}

	public void save() {
		this.triangleManager.unload();
	}
}
