package me.commonsenze.core.Objects;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.ServerOperator;

public class PermissionBase extends PermissibleBase {

	public PermissionBase(UUID uuid) {
		super(new ServerOperator() {
			
			@Override
			public void setOp(boolean op) {
				if (Bukkit.getPlayer(uuid) == null)return;
				Bukkit.getPlayer(uuid).setOp(op);
			}
			
			@Override
			public boolean isOp() {
				if (Bukkit.getPlayer(uuid) == null)return false;
				return Bukkit.getPlayer(uuid).isOp();
			}
		});
	}
	
	@Override
	public boolean hasPermission(String inName) {
		if (super.hasPermission("'*'"))return true;
		String s = inName.split("\\.")[0];
		String[] words = inName.split("\\.");
		for (int i = 1; i < words.length; i++) {
			if (super.hasPermission(s+".*")) {
				for (PermissionAttachmentInfo perm : super.getEffectivePermissions()){
					if (perm.getPermission().equalsIgnoreCase("!"+inName)) {
						return false;
					}
				}
				return true;
			}
			s += "."+words[i];
		}
		return super.hasPermission(inName);
	}
}
