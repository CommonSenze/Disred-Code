package me.commonsenze.core.Objects;

import java.lang.reflect.Type;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocationAdapter implements JsonDeserializer<Location>, JsonSerializer<Location> {

	@Override
	public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
		return serialize(src);
	}

	@Override
	public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return deserialize(json);
	}
	
	private JsonElement serialize(Location location) {
		if (location == null) 
			return new JsonObject();
		
		JsonObject object = new JsonObject();
		
		object.addProperty("world", location.getWorld().getName());
		object.addProperty("x", location.getX());
		object.addProperty("y", location.getY());
		object.addProperty("z", location.getZ());
		object.addProperty("yaw", location.getYaw());
		object.addProperty("pitch", location.getPitch());
		
		
        return object;
	}
	
	private Location deserialize(JsonElement object) {
        if (object == null || !(object instanceof JsonObject)) {
            return null;
        }
        JsonObject element = object.getAsJsonObject();
        
        World world = null;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;
        
        if (element.has("world")) {
        	world = Bukkit.getWorld(element.get("world").getAsString());
        }
        if (element.has("x")) {
        	x = element.get("x").getAsDouble();
        }
        if (element.has("y")) {
        	y = element.get("y").getAsDouble();
        }
        if (element.has("z")) {
        	z = element.get("z").getAsDouble();
        }
        if (element.has("yaw")) {
        	yaw = element.get("yaw").getAsFloat();
        }
        if (element.has("pitch")) {
        	pitch = element.get("pitch").getAsFloat();
        }

        return new Location(world, x,y,z,yaw,pitch);
    }
}
