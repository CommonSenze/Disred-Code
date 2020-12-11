package me.commonsenze.project.Commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.Getter;
import me.commonsenze.project.Core;
import me.commonsenze.project.Lang;
import me.commonsenze.project.Abstracts.Executor;
import me.commonsenze.project.Managers.impl.TriangleManager;
import me.commonsenze.project.Objects.Triangle;
import me.commonsenze.project.Util.CC;

@Getter
public class TriangleCommand extends Executor {

	private TriangleManager triangleManager;
	
	public TriangleCommand(String command, String description, String...aliases) {
		super(command, description, aliases);
		setPlayersOnly(true);
		this.triangleManager = Core.getInstance().getManagerHandler().getTriangleManager();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;

		if (player == null)return true;
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("create")) {
				if (!player.getPlayer().hasPermission(this.getPermission())) {
					player.getPlayer().sendMessage(Lang.NO_PERMISSION);
					return true;
				}
				
				Triangle triangle = new Triangle(player.getUniqueId(), player.getWorld());
				player.sendMessage(Lang.success("-nPlease left click 3 point to draw said triangle."));
				Core.getInstance().getManagerHandler().getTriangleManager().put(player.getUniqueId(), triangle);
				return true;
			}
			if (args[0].equalsIgnoreCase("draw")) {
				if (!player.getPlayer().hasPermission(this.getPermission())) {
					player.getPlayer().sendMessage(Lang.NO_PERMISSION);
					return true;
				}
				if (!getTriangleManager().hasTriangle(player.getUniqueId())) {
					player.getPlayer().sendMessage(Lang.fail("-nYou do not have a triangle currently created. Do this with '-e/triangle create-n'."));
					return true;
				}
				Triangle triangle = getTriangleManager().getTriangle(player.getUniqueId());
				
				if (!triangle.draw()) {
					player.getPlayer().sendMessage(Lang.fail("-nPlease select all the points you want to have the triangle drawn from."));
					return true;
				}
				
				player.sendMessage(Lang.success("-nDrawing triangle..."));
				return true;
			}
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("create")) {
				if (args[1].equalsIgnoreCase("-f")) {
					if (!player.getPlayer().hasPermission(this.getPermission())) {
						player.getPlayer().sendMessage(Lang.NO_PERMISSION);
						return true;
					}
					
					Triangle triangle = new Triangle(player.getUniqueId(), player.getWorld());
					triangle.setFilled(true);
					player.sendMessage(Lang.success("-nPlease left click 3 point to draw said filled triangle."));
					Core.getInstance().getManagerHandler().getTriangleManager().put(player.getUniqueId(), triangle);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> getHelp(Player player) {
		ArrayList<String> help = new ArrayList<>();

		if (player.hasPermission(getPermission())) {
			help.add(CC.BLUE + "/"+getName()+" create [-f] - "+CC.YELLOW+"Create a triangle. [-f: Fill triangle]");
			help.add(CC.BLUE + "/"+getName()+" draw - "+CC.YELLOW+"Draw the triangle you created.");
		}
		return help;
	}
}
