package me.commonsenze.core.Commands;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bson.Document;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mongodb.client.model.Filters;

import me.commonsenze.core.Core;
import me.commonsenze.core.Lang;
import me.commonsenze.core.Abstracts.Executor;
import me.commonsenze.core.Enums.ServerColor;
import me.commonsenze.core.Managers.impl.MongoManager;
import me.commonsenze.core.Objects.Profile;

public class RegisterCommand extends Executor {

	public RegisterCommand(String command, String description, String...aliases) {
		super(command, description, aliases);
		setPlayersOnly(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 1) {
			MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			Profile profile = Core.getInstance().getManagerHandler().getProfileManager().getProfile((Player)sender);
			Document profileDocument = getConnection().getDatabase(MongoManager.DATABASE).getCollection(MongoManager.PLAYER_COLLECTION).find(Filters.eq("uuid", profile.getUniqueId().toString())).first();
			if (profileDocument.containsKey("password")) {
				sender.sendMessage(Lang.fail("-nThis account is already registed."));
				return true;
			}

			byte[] encodedhash = digest.digest(
					  args[0].getBytes(StandardCharsets.UTF_8));
			
			profileDocument.append("password", encodedhash.toString());
			
			getConnection().getDatabase(MongoManager.DATABASE).getCollection(MongoManager.PLAYER_COLLECTION).replaceOne(Filters.eq("uuid", profile.getUniqueId().toString()), profileDocument);
			
			sender.sendMessage(Lang.success("-nYou are now registered."));
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		return new ArrayList<>(Arrays.asList(
				ServerColor.PRIMARY + "/"+getName()+" <password> - "+ServerColor.SECONDARY+"Register your account to our website with <password>."
				));
	}

}
