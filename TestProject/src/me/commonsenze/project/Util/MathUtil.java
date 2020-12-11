package me.commonsenze.project.Util;

import org.bukkit.util.Vector;

public class MathUtil {

	public static double getAreaOfTriangle(double a, double b, double c) {
		return Math.abs(a*b*(Math.sin(c))/2.0);
	}
	
	public static Vector getNormal(Vector a, Vector b, Vector c) {
		Vector line1 = a.clone().subtract(b);
		Vector line2 = c.clone().subtract(b);
		return line1.crossProduct(line2);
	}
}
