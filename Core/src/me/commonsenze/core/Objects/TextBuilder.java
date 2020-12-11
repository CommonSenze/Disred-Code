package me.commonsenze.core.Objects;

import java.util.ArrayList;
import java.util.HashMap;

import me.commonsenze.core.Util.TextCreationError;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TextBuilder implements Cloneable {

	private TextComponent main, sub;

	public TextBuilder() {
		main = new TextComponent("");
	}

	public TextBuilder(String text) {
		main = new TextComponent(text);
	}

	public TextBuilder(TextComponent main) {
		this.main = main;
	}

	public TextBuilder(ArrayList<HashMap<String, String>> serialized) {
		this(toBuilder(serialized));
	}

	public TextBuilder(TextBuilder builder) {
		this.main = builder.main;
		this.sub = builder.sub;
	}

	public TextBuilder append(String text) {
		if (sub != null) throw new TextCreationError("Cannot append new TextComponent while one is still being made.");

		sub = new TextComponent(text);
		return this;
	}

	public TextBuilder setColor(ChatColor color) {
		if (sub == null) throw new TextCreationError("Cannot set the color of a TextComponent that is not created.");

		sub.setColor(color);
		return this;
	}

	public TextBuilder setBold(boolean bold) {
		if (sub == null) throw new TextCreationError("Cannot set the color of a TextComponent that is not created.");

		sub.setBold(bold);
		return this;
	}

	public TextBuilder setHoverEvent(HoverEvent event) {
		if (sub == null) throw new TextCreationError("Cannot set the color of a TextComponent that is not created.");

		sub.setHoverEvent(event);
		return this;
	}

	public TextBuilder setClickEvent(ClickEvent event) {
		if (sub == null) throw new TextCreationError("Cannot set the color of a TextComponent that is not created.");

		sub.setClickEvent(event);
		return this;
	}

	public TextBuilder create() {
		if (sub == null) throw new TextCreationError("Cannot create a null TextComponent.");

		main.addExtra(sub);
		sub = null;
		return this;
	}

	public TextBuilder addExtra(TextComponent component) {
		if (sub != null) throw new TextCreationError("Cannot add extra to TextComponent while one is still being made.");

		main.addExtra(component);
		return this;
	}

	//	public TextBuilder create(int index) {
	//		if (sub == null) throw new TextCreationError("Cannot create a null TextComponent.");
	//		
	//		TextComponent first = new TextComponent(""), second = new TextComponent("");
	//		main.
	//		
	//		main.addExtra(sub);
	//		sub = null;
	//		return this;
	//	}
	
	public String toPlainText() {
		return main.toPlainText();
	}
	
	public TextBuilder createAtStart() {
		if (sub == null) throw new TextCreationError("Cannot create a null TextComponent.");

		TextComponent component = sub;
		
		component.addExtra(main);
		
		main = component;
		sub = null;
		return this;
	}

	public TextBuilder append(TextComponent component) {
		if (component == null) throw new TextCreationError("Cannot append a null TextComponent.");

		main.addExtra(component);
		return this;
	}

	public TextComponent toTextComponent() {
		if (sub != null) throw new TextCreationError("Cannot create TextComponent while one is still being made.");
		return main;
	}

	public TextBuilder clone() {
		return new TextBuilder(this);
	}

	public static TextBuilder toBuilder(ArrayList<HashMap<String, String>> builds) {
		TextBuilder main = new TextBuilder();
		for (HashMap<String, String> build : builds) {
			TextBuilder builder = new TextBuilder();
			builder.append(build.get("message"));

			if (build.containsKey("hover")) {
				String[] words = build.get("hover").split("-");
				builder.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(words[0]), new ComponentBuilder(words[1]).create()));
			}

			if (build.containsKey("click")) {
				String[] words = build.get("click").split("-");
				builder.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(words[0]), new ComponentBuilder(words[1]).create()));
			}

			if (build.containsKey("bold"))
				builder.setBold(Boolean.getBoolean(build.get("bold")));

			if (build.containsKey("color"))
				builder.setColor(getChatColor(build.get("color")));
			main.addExtra(builder.create().toTextComponent());
		}
		return main;
	}

	public ArrayList<HashMap<String, String>> serialize() {
		ArrayList<HashMap<String, String>> builds = new ArrayList<>();

		for (BaseComponent component : main.getExtra()) {
			builds.add(compile(component));
		}

		return builds;
	}

	private HashMap<String, String> compile(BaseComponent component){
		HashMap<String, String> build = new HashMap<>();
		build.put("message", component.toPlainText());
		if (component.getHoverEvent() != null)
			build.put("hover", component.getHoverEvent().getAction().name()+"-"+TextComponent.toPlainText(component.getHoverEvent().getValue()));
		if (component.getClickEvent() != null)
			build.put("click", component.getClickEvent().getAction().name()+"-"+component.getClickEvent().getValue());
		build.put("bold", ""+(component.isBoldRaw()!=null ? component.isBoldRaw() : "false"));
		build.put("color", (component.getColorRaw()!=null ? component.getColorRaw() : "")+"");
		return build;
	}

	public void split(TextComponent component, int index) {
		ArrayList<HashMap<String, String>> builds = serialize();
		for (int i = 0; i < index; i++) {
			String line = main.toPlainText().substring(i, index);
			int slot = 0;
			for (HashMap<String, String> build : new ArrayList<>(builds)) {
				if (build.get("message").startsWith(line)) {
					builds.remove(slot);
					int splitIndex = index - i;
					HashMap<String, String> copy1 = new HashMap<>(build);
					copy1.put("message", build.get("message").substring(0, splitIndex));
					if (splitIndex > 0) {
						HashMap<String, String> copy2 = new HashMap<>(build);
						copy2.put("message", build.get("message").substring(splitIndex));
						builds.add(slot, copy2);
					}
					builds.add(slot, compile(component));
					builds.add(slot, copy1);
					TextBuilder builder = toBuilder(builds);
					this.main = builder.main;
					return;
				}
				slot++;
			}
		}
	}

	public void split(TextComponent component, int index, int slot) {
		ArrayList<HashMap<String, String>> builds = serialize();
		HashMap<String, String> build = builds.get(slot);
		builds.remove(slot);
		HashMap<String, String> copy1 = new HashMap<>(build);
		copy1.put("message", build.get("message").substring(0, index));
		if (index > 0) {
			HashMap<String, String> copy2 = new HashMap<>(build);
			copy2.put("message", build.get("message").substring(index));
			builds.add(slot, copy2);
		}
		builds.add(slot, compile(component));
		builds.add(slot, copy1);
		System.out.println("CALLED !!!!!!");
		builds.stream().forEach(map -> {
			System.out.println(map.get("message"));
		});
		TextBuilder builder = toBuilder(builds);
		System.out.println("FINAL PRODUCT: "+ builder.main.toPlainText());
		this.main = builder.main;
	}

	public void replace(String string, TextComponent component) {
		ArrayList<HashMap<String, String>> builds = serialize();
		int slot = 0;
		for (HashMap<String, String> build : new ArrayList<>(builds)) {
			if (build.get("message").contains(string)) {
				int splitIndex = build.get("message").indexOf(string);
				build.put("message", build.get("message").replace(string, ""));
				TextBuilder builder = toBuilder(builds);
				this.main = builder.main;
				split(component, splitIndex, slot);
				return;
			}
			slot++;
		}
	}

	public void add(TextComponent component, int slot) {
		ArrayList<HashMap<String, String>> builds = serialize();
		builds.add(slot, compile(component));
		TextBuilder builder = toBuilder(builds);
		this.main = builder.main;
	}

	public static ChatColor getChatColor(String color) {
		for(ChatColor c : ChatColor.values()) {
			if(c.name().equalsIgnoreCase(color)) {
				return c;
			}
		}
		return ChatColor.WHITE;
	}

	@Override
	public String toString() {
		return main.toPlainText();
	}
}
