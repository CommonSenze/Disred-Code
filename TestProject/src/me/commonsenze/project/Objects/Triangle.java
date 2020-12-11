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
import me.commonsenze.project.Interfaces.Callback;
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
		double a = vertices[0].getVector().distance(vertices[1].getVector());
		double b = vertices[1].getVector().distance(vertices[2].getVector());
		double c = vertices[2].getVector().distance(vertices[0].getVector());

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

			Vector dirTo = first.directionTo(second);

			drawLine(first.getVector(), second.getVector(), dirTo, () -> {
				vertices[placement].setFinished(true);
				if (finishedDrawing()) {
					new BukkitRunnable() {
						public void run() {
							fill();
						}
					}.runTaskLater(Core.getInstance(), 20);
				}
			});
		}
		return true;
	}

	public void drawLine(Vector start, Vector end, Vector space, Callback callback) {
		Vector particleVec = start.clone();
		double distance = start.distance(end);

		new BukkitRunnable() {
			private double distanceTraveled = 0;

			public void run() {
				particleVec.add(space);
				if (distanceTraveled >= distance) {
					callback.callback();
					cancel();
					return;
				}
				Location loc = new Location(world, particleVec.getX(), particleVec.getY(), particleVec.getZ());
				spawnedParticles.add(loc);
				distanceTraveled += ParticleUtil.PARTICLE_SPACING;
			}
		}.runTaskTimer(Core.getInstance(), 3, 3);
	}

	public void fill() {
		if (filled) {
			Vector start = startVector, end = endVector;
			getCreator().sendMessage(Lang.success("-nFilling the now created triangle..."));
			double startX = start.getX(), startY = start.getY(), startZ = start.getZ();
			double endX = end.getX(), endY = end.getY(), endZ = end.getZ();
			Vector anchor = vertices[1].getVector();
			Vector normal = MathUtil.getNormal(vertices[0].getVector(), vertices[1].getVector(), vertices[2].getVector());
			for (double x = startX; x < endX; x += 0.02)
				for (double y = startY; y < endY; y += 0.02)
					for (double z = startZ; z < endZ; z += 0.02) {

						// A·(x4-x1) + B·(y4-y1) + C·z4 - A·x1 - B·y1 - C·z1
						Vector point = new Vector(x,y,z);
						Vector plane = anchor.clone();
						Vector norm = normal.clone();

						double independent = -(norm.getX() * plane.getX() + norm.getY() * plane.getY() + norm.getZ() * plane.getZ());

						double planeEqu = norm.getX() * point.getX() + norm.getY() * point.getY() + norm.getZ() * point.getZ()+independent;
						boolean samePlane = Math.abs(planeEqu) < 0.01;

						if (samePlane) {
							Vector vector = new Vector(x,y,z);
							if (inTriangle(vector))
								spawnedParticles.add(new Location(world, x,y,z));
						}
					}
		}
	}
	
	public boolean inTriangle(Vector point) {
		Vector a = vertices[0].getVector();
		Vector b = vertices[1].getVector();
		Vector c = vertices[2].getVector();
		Vector p = point.clone();
		// Compute vectors        
		Vector v0 = c.subtract(a);
		Vector v1 = b.subtract(a);
		Vector v2 = p.subtract(a);

		// Compute dot products
		double dot00 = v0.dot(v0);
		double dot01 = v0.dot(v1);
		double dot02 = v0.dot(v2);
		double dot11 = v1.dot(v1);
		double dot12 = v1.dot(v2);

		// Compute barycentric coordinates
		double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

		// Check if point is in triangle
		return (u >= 0) && (v >= 0) && (u + v < 1);
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
