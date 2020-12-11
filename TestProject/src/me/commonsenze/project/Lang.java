package me.commonsenze.project;

import me.commonsenze.project.Util.CC;

public class Lang {

	public static final String D_ANGLED_R = "»";
	public static final String D_ANGLED_L = "«";
	public static final String VERT_DIVIDER = "┃";
	public static final String NO_PERMISSION = CC.RED + "You do not have permission to do this command.";
	
	public static String fail(String message) {
		return CC.RED + CC.BOLD.toString() + "✖ "+message.replaceAll("-n", CC.RED.toString()).replaceAll("-e", CC.DARKRED.toString());
	}

	public static String success(String message) {
		return CC.GREEN + CC.BOLD.toString() + "✔ "+message.replaceAll("-n", CC.GREEN.toString()).replaceAll("-e", CC.DARKGREEN.toString());
	}
}
