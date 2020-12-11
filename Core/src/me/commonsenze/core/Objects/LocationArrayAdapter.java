package me.commonsenze.core.Objects;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocationArrayAdapter implements JsonDeserializer<Location[]>, JsonSerializer<Location[]> {

	@Override
	public JsonElement serialize(Location[] src, Type typeOfSrc, JsonSerializationContext context) {
		return serialize(src);
	}

	@Override
	public Location[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return deserialize(json);
	}

	private JsonElement serialize(Location[] locations) {
		if (locations == null) 
			return new JsonArray();

		JsonArray array = new JsonArray();

		for (Location loc : locations) {
			JsonObject object = new JsonObject();

			object.addProperty("null", loc == null);
			if (!object.get("null").getAsBoolean()) {
				object.addProperty("world", loc.getWorld().getName());
				object.addProperty("x", loc.getX());
				object.addProperty("y", loc.getY());
				object.addProperty("z", loc.getZ());
				object.addProperty("yaw", loc.getYaw());
				object.addProperty("pitch", loc.getPitch());
			}
			array.add(object);
		}

		return array;
	}

	private Location[] deserialize(JsonElement object) {
		if (object == null || !(object instanceof JsonArray)) {
			return null;
		}
		JsonArray array = object.getAsJsonArray();

		Set<Location> locations = new HashSet<>();
		
		for (JsonElement element : array) {
			JsonObject obj = element.getAsJsonObject();

			if (obj.has("null")&&obj.get("null").getAsBoolean()) continue;
			
			World world = null;
			double x = 0;
			double y = 0;
			double z = 0;
			float yaw = 0;
			float pitch = 0;

			if (obj.has("world")) {
				world = Bukkit.getWorld(obj.get("world").getAsString());
			}
			if (obj.has("x")) {
				x = obj.get("x").getAsDouble();
			}
			if (obj.has("y")) {
				y = obj.get("y").getAsDouble();
			}
			if (obj.has("z")) {
				z = obj.get("z").getAsDouble();
			}
			if (obj.has("yaw")) {
				yaw = obj.get("yaw").getAsFloat();
			}
			if (obj.has("pitch")) {
				pitch = obj.get("pitch").getAsFloat();
			}
			locations.add(new Location(world, x,y,z,yaw,pitch));
		}

		return locations.toArray(new Location[array.size()]);
	}
}
