package me.commonsenze.minigames.Games.Skywars.Objects;

import java.lang.reflect.Type;

import org.bukkit.Location;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import me.commonsenze.core.Objects.Cuboid;
import me.commonsenze.minigames.Minigames;

public class IslandAdapter implements JsonDeserializer<Island>, JsonSerializer<Island> {

	@Override
	public JsonElement serialize(Island src, Type typeOfSrc, JsonSerializationContext context) {
		return serialize(src);
	}

	@Override
	public Island deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return deserialize(json);
	}
	
	private JsonElement serialize(Island island) {
		if (island == null) 
			return new JsonObject();
		
		JsonObject object = new JsonObject();
		object.addProperty("bounds", Minigames.getGson().toJson(island.getBounds()));
		object.addProperty("cage", Minigames.getGson().toJson(island.getCage()));
		object.addProperty("gameSpawn", Minigames.getGson().toJson(island.getGameSpawn()));
		
        return object;
	}
	
	private Island deserialize(JsonElement object) {
        if (object == null || !(object instanceof JsonObject)) {
            return null;
        }
        JsonObject element = object.getAsJsonObject();
        
        Cuboid bounds = null;
        Location gameSpawn = null;
        Cuboid cage = null;
        
        if (element.has("bounds")&&element.has("cage")&&element.has("gameSpawn")) {
        	bounds = Minigames.getGson().fromJson(element.get("bounds").getAsString(), new TypeToken<Cuboid>() {}.getType());
        	cage = Minigames.getGson().fromJson(element.get("cage").getAsString(), new TypeToken<Cuboid>() {}.getType());
        	gameSpawn = (Location)Minigames.getGson().fromJson(element.get("gameSpawn").getAsString(), new TypeToken<Location>() {}.getType());
        } else return null;
        
        return new Island(bounds, cage, gameSpawn);
    }
}
