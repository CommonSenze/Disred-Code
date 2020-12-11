package me.commonsenze.core.Objects;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import lombok.val;

public class ItemStackAdapter implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

	@Override
	public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
		return serialize(src);
	}

	@Override
	public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return deserialize(json);
	}
	
	private JsonElement serialize(ItemStack item) {
		if (item == null) 
			item = new ItemStack(Material.AIR);
		
		JsonObject object = new JsonObject();
		
		object.addProperty("type", item.getType().name());
		object.addProperty(getDataKey(item), item.getDurability());
		object.addProperty("count", item.getAmount());
		
		if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                object.addProperty("name", meta.getDisplayName());
            }

            if (meta.hasLore()) {
            	object.add("lore", convertStringList(meta.getLore()));
            }

            if (meta instanceof LeatherArmorMeta) {
                object.addProperty("color", ((LeatherArmorMeta)meta).getColor().asRGB());
            } else if (meta instanceof SkullMeta) {
                object.addProperty("skull", ((SkullMeta)meta).getOwner());
            } else if (meta instanceof BookMeta) {
                object.addProperty("title", ((BookMeta)meta).getTitle());
                object.addProperty("author", ((BookMeta)meta).getAuthor());
                object.add("pages", convertStringList(((BookMeta)meta).getPages()));
            } else if (meta instanceof PotionMeta) {
                if (!((PotionMeta)meta).getCustomEffects().isEmpty()) {
                    object.add("potion-effects", convertPotionEffectList(((PotionMeta)meta).getCustomEffects()));
                }
            } else if (meta instanceof MapMeta) {
                object.addProperty("scaling", java.lang.Boolean.valueOf(((MapMeta)meta).isScaling()));
            } else if (meta instanceof EnchantmentStorageMeta) {
                JsonObject storedEnchantments = new JsonObject();
                Map<Enchantment, Integer> map = ((EnchantmentStorageMeta)meta).getStoredEnchants();
                for (Enchantment enchant : map.keySet()) {
                    storedEnchantments.addProperty(enchant.getName(), map.get(enchant));
                }
                object.add("stored-enchants", storedEnchantments);
            }
        }
		
		if (!item.getEnchantments().isEmpty()) {
			JsonObject enchantments = new JsonObject();
            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                enchantments.addProperty(entry.getKey().getName(), entry.getValue());
            }
			object.add("enchants", enchantments);
        }

        return object;
	}
	
	private ItemStack deserialize(JsonElement object) {
        if (object == null || !(object instanceof JsonObject)) {
            return new ItemStack(Material.AIR);
        }
        JsonObject element = object.getAsJsonObject();	
        Material type = Material.getMaterial(element.get("type").getAsString());
        short data = (element.has("damage") ? element.get("damage").getAsShort() : element.has("data") ? element.get("data").getAsShort() : 0);
        int count = element.get("count").getAsInt();
        ItemStack item = new ItemStack(type, count, data);
        ItemMeta meta = item.getItemMeta();

        if (element.has("name")) {
            meta.setDisplayName(element.get("name").getAsString());
        }

        if (element.has("lore")) {
            meta.setLore(convertStringList(element.get("lore")));
        }

        if (element.has("color")) {
            ((LeatherArmorMeta)meta).setColor(Color.fromRGB(element.get("color").getAsInt()));
        } else if (element.has("skull")) {
            ((SkullMeta)meta).setOwner(element.get("skull").getAsString());
        } else if (element.has("title")) {
        	((BookMeta)meta).setTitle(element.get("title").getAsString());
        	((BookMeta)meta).setAuthor(element.get("author").getAsString());
        	((BookMeta)meta).setPages(convertStringList(element.get("pages")));
        } else if (element.has("potion-effects")) {
        	PotionMeta potionMeta = (PotionMeta) meta;
            for (PotionEffect effect : convertPotionEffectList(element.get("potion-effects"))) {
                potionMeta.addCustomEffect(effect, false);
            }
        } else if (element.has("scaling")) {
            ((MapMeta)meta).setScaling(element.get("scaling").getAsBoolean());
        } else if (element.has("stored-enchants")) {
        	JsonObject enchantments = element.get("stored-enchants").getAsJsonObject();
            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantments.has(enchantment.getName())) {
                    ((EnchantmentStorageMeta)meta).addStoredEnchant(enchantment, enchantments.get(enchantment.getName()).getAsInt(), true);
                }
            }
        }

        item.setItemMeta(meta);

        if (element.has("enchants")) {
            val enchantments = element.get("enchants").getAsJsonObject();
            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantments.has(enchantment.getName())) {
                    item.addUnsafeEnchantment(enchantment, enchantments.get(enchantment.getName()).getAsInt());
                }
            }
        }

        return item;
    }
	
	private String getDataKey(ItemStack item) {
        if (item.getType() == Material.AIR) {
            return "data";
        }
        if (Enchantment.DURABILITY.canEnchantItem(item)) {
        	return "damage";
        } else return "data";
    }
	
	private JsonArray convertStringList(Collection<String> strings) {
		JsonArray array = new JsonArray();
		for (String string : strings) {
			array.add(new JsonPrimitive(string));
		}
		return array;
	}
	
	private List<String> convertStringList(JsonElement jsonElement) {
        JsonArray array = jsonElement.getAsJsonArray();
        ArrayList<String> ret = new ArrayList<>();
        for (JsonElement element : array) {
            ret.add(element.getAsString());
        }
        return ret;
    }

	private JsonArray convertPotionEffectList(Collection<PotionEffect> potionEffects) {
        JsonArray ret = new JsonArray();
        for (PotionEffect element : potionEffects) {
            ret.add(PotionEffectAdapter.toJson(element));
        }
        return ret;
    }

    private List<PotionEffect> convertPotionEffectList(JsonElement jsonElement) {
        if (jsonElement == null) {
            return null;
        }

        if (!jsonElement.isJsonArray()) {
            return null;
        }

        JsonArray array = jsonElement.getAsJsonArray();
        ArrayList<PotionEffect> ret = new ArrayList<PotionEffect>();
        for (JsonElement element : array) {
            PotionEffect e = PotionEffectAdapter.fromJson(element);
            ret.add(e);
        }

        return ret;
    }
}
