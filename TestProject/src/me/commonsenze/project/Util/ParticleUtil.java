package me.commonsenze.project.Util;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.commonsenze.project.Enums.ParticleEffect;

public class ParticleUtil {

	public static final double PARTICLE_SPACING = 0.2;
	
	public static void spawnParticle(Player player, Location loc, ParticleEffect effect) {
		effect.display(0, 0, 0, 0, 1, loc,Arrays.asList(player));
	}
	
	public static void spawnParticle(List<Player> players, Location loc, ParticleEffect effect) {
		effect.display(0, 0, 0, 0, 1, loc,players);
	}
	
	public static void spawnParticle(Location loc, ParticleEffect effect, double range) {
		effect.display(0, 0, 0, 0, 1, loc,range);
	}
}
