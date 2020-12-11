package me.commonsenze.project.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import lombok.Getter;
import lombok.Setter;
import me.commonsenze.project.Core;
import me.commonsenze.project.Lang;
import me.commonsenze.project.Enums.ParticleEffect;
import me.commonsenze.project.Util.MathUtil;
import me.commonsenze.project.Util.ParticleUtil;

@Getter
public class Triangle implements Runnable, Cloneable {

	private World world;
	private Vertice[] vertices;
	private List<Location> spawnedParticles;
	@Setter private boolean filled;
	private BukkitTask triangleTask;
	private UUID uniqueId;
	private ParticleEffect effect;
	private Vector startVector, endVector;

	public Triangle(UUID uniqueId, World world) {
		this.world = world;
		this.uniqueId = uniqueId;
		this.vertices = new Vertice[3];
		this.spawnedParticles = new ArrayList<>();
		this.effect = ParticleEffect.FLAME;
		this.triangleTask = Bukkit.getScheduler().runTaskTimer(Core.getInstance(), this, 1, 1);
	}

	public Player getCreator() {
		return Bukkit.getPlayer(getUniqueId());
	}

	public Triangle(Triangle triangle) {
		this.world = triangle.world;
		this.vertices = triangle.vertices;
		this.spawnedParticles = new ArrayList<>();
		this.effect = ParticleEffect.WATER_SPLASH;
		this.triangleTask = Bukkit.getScheduler().runTaskTimer(Core.getInstance(), this, 1, 1);
	}

	public void addVertice(Vertice vertice) {
		for (int i = 0; i < vertices.length; i++)
			if (vertices[i] == null) {
				vertices[i] = vertice;
				Vector vector = vertice.getVector();
				spawnedParticles.add(new Location(getWorld(), vector.getX(), vector.getY(), vector.getZ()));
				return;
			}
	}

	public double getArea() {
		double a = Math.abs(vertices[0].getVector().distance(vertices[1].getVector()));
		double b = Math.abs(vertices[1].getVector().distance(vertices[2].getVector()));
		double c = Math.abs(vertices[2].getVector().distance(vertices[0].getVector()));

		return MathUtil.getAreaOfTriangle(a, b, c);
	}

	public boolean hasVerticies() {
		for (int i = 0; i < vertices.length; i++)
			if (vertices[i] == null)
				return false;
		return true;
	}

	public boolean finishedDrawing() {
		return vertices[0].isFinished()&&vertices[1].isFinished()&&vertices[2].isFinished();
	}

	public boolean draw() {
		if (!hasVerticies())
			return false;
		double startX, startY, startZ;
		double endX, endY, endZ;
		startX = startY = startZ = Double.MAX_VALUE;
		endX = endY = endZ = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < vertices.length; i++) {
			Vertice vertice = vertices[i];
			startX = Math.min(startX, vertice.getVector().getX());
			startY = Math.min(startY, vertice.getVector().getY());
			startZ = Math.min(startZ, vertice.getVector().getZ());

			endX = Math.max(endX, vertice.getVector().getX());
			endY = Math.max(endY, vertice.getVector().getY());
			endZ = Math.max(endZ, vertice.getVector().getZ());
		}
		startVector = new Vector(startX, startY, startZ);
		endVector = new Vector(endX, endY, endZ);

		for (int i = 0; i < vertices.length; i++) {
			int placement = i;
			Vertice first = vertices[i];
			Vertice second = vertices[i + 1 == vertices.length ? 0 : i + 1];

			Vector particleVec = first.getVector();
			Vector dirTo = first.directionTo(second);

			double distance = first.getVector().distance(second.getVector());

			new BukkitRunnable() {
				private double distanceTraveled = 0;

				public void run() {
					particleVec.add(dirTo);
					if (distanceTraveled >= distance) {
						vertices[placement].setFinished(true);
						if (finishedDrawing()) {
							new BukkitRunnable() {
								public void run() {
									fill();
								}
							}.runTaskLater(Core.getInstance(), 20);
						}
						cancel();
						return;
					}
					Location loc = new Location(world, particleVec.getX(), particleVec.getY(), particleVec.getZ());
					spawnedParticles.add(loc);
					distanceTraveled += ParticleUtil.PARTICLE_SPACING;
				}
			}.runTaskTimer(Core.getInstance(), 10, 10);
		}
		return true;
	}

	public void fill() {
		if (filled) {
			Vector start = startVector, end = endVector;
			getCreator().sendMessage(Lang.success("-nFilling the now created triangle..."));
			double startX = start.getX(), startY = start.getY(), startZ = start.getZ();
			double endX = end.getX(), endY = end.getY(), endZ = end.getZ();
			Triangle p0 = new Triangle(getUniqueId(), world);
			p0.addVertice(vertices[0]);
			p0.addVertice(vertices[1]);
			Triangle p1 = new Triangle(getUniqueId(), world);
			p1.addVertice(vertices[1]);
			p1.addVertice(vertices[2]);
			Triangle p2 = new Triangle(getUniqueId(), world);
			p2.addVertice(vertices[2]);
			p2.addVertice(vertices[0]);
			for (double x = startX; x < endX; x += ParticleUtil.PARTICLE_SPACING)
				for (double y = startY; y < endY; y += ParticleUtil.PARTICLE_SPACING)
					for (double z = startZ; z < endZ; z += ParticleUtil.PARTICLE_SPACING) {
						
						Vector vector = new Vector(x,y,z);
						double first = Triangle.getArea(vertices[0].getVector(), vertices[1].getVector(), vector);
						double second = Triangle.getArea(vertices[1].getVector(), vertices[2].getVector(), vector);
						double third = Triangle.getArea(vertices[2].getVector(), vertices[0].getVector(), vector);
						
						double area = first+second+third;
//						if (Math.abs(area - getArea()) < 3)
							System.out.println(area + " "+ getArea());
							//						if (area == getArea())
//							spawnedParticles.add(new Location(world, x,y,z));
					}
		}
	}

	@Override
	public Triangle clone() {
		return new Triangle(this);
	}

	@Override
	public void run() {
		for (Location loc : spawnedParticles) {
			ParticleUtil.spawnParticle(loc, effect, 15);
		}
	}
	
	public static double getArea(Vector first, Vector second, Vector third) {
		double a = first.distance(second);
		double b = second.distance(third);
		double c = third.distance(first);
		return MathUtil.getAreaOfTriangle(a, b, c);
	}
}
