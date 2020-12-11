package me.commonsenze.core.Objects;

import java.lang.reflect.Type;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import lombok.val;

public class PotionEffectAdapter implements JsonDeserializer<PotionEffect>, JsonSerializer<PotionEffect> {

	@Override
	public JsonElement serialize(PotionEffect src, Type typeOfSrc, JsonSerializationContext context) {
		// TODO Auto-generated method stub
		return toJson(src);
	}

	@Override
	public PotionEffect deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return fromJson(json);
	}

	public static JsonObject toJson(PotionEffect potionEffect) {
        if (potionEffect == null) {
            return null;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", potionEffect.getType().getName());
        jsonObject.addProperty("duration", potionEffect.getDuration());
        jsonObject.addProperty("amplifier", potionEffect.getAmplifier());
        jsonObject.addProperty("ambient", Boolean.valueOf(potionEffect.isAmbient()));
        return jsonObject;
    }

    public static PotionEffect fromJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return null;
        }

        val jsonObject = jsonElement.getAsJsonObject();
        val effectType = PotionEffectType.getByName(jsonObject.get("type").getAsString());
        val duration = jsonObject.get("duration").getAsInt();
        val amplifier = jsonObject.get("amplifier").getAsInt();
        val ambient = jsonObject.get("ambient").getAsBoolean();

        return new PotionEffect(effectType, duration, amplifier, ambient);
    }
}
