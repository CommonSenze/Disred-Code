package me.commonsenze.project.Objects;

import org.bukkit.util.Vector;

import lombok.Getter;
import lombok.Setter;
import me.commonsenze.project.Util.ParticleUtil;

@Getter
public class Vertice {

	private Vector vector;
	@Setter private boolean finished;
	
	public Vertice(Vector vector) {
		this.vector = vector;
	}
	
	public Vector directionTo(Vertice vertice) {
		return vertice.getVector().clone().subtract(getVector()).normalize().multiply(ParticleUtil.PARTICLE_SPACING);
	}
	
	public Vector getVector() {
		return vector.clone();
	}
}
