package me.commonsenze.core.Util;

import org.bukkit.World;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import me.commonsenze.core.Objects.Cuboid;

public final class WorldEditUtils {

	private static EditSession editSession;
	private static World currentWorld;
    private static com.sk89q.worldedit.world.World worldEditWorld;

    public static void primeWorldEditApi(World world) {
    	if (worldEditWorld != null&&currentWorld.equals(world)) return;

    	EditSessionFactory esFactory = WorldEdit.getInstance().getEditSessionFactory();	
    	
        worldEditWorld = new BukkitWorld(world);
        currentWorld = world;
        editSession = esFactory.getEditSession(worldEditWorld, -1);
    }

    public static void clear(Cuboid bounds) {
        clear(bounds.getWorld(),
        		new Vector(bounds.getLowerX(), bounds.getLowerY(), bounds.getLowerZ()),
        		new Vector(bounds.getUpperX(), bounds.getUpperY(), bounds.getUpperZ())
        );
    }

	public static void clear(World world, Vector lower, Vector upper) {
		primeWorldEditApi(world);
		
        Region region = new CuboidRegion(worldEditWorld, lower, upper);
        try {
            editSession.setBlocks(region, new BaseBlock(BlockID.AIR, 0));
            editSession.flushQueue();
        } catch (MaxChangedBlocksException ex) {
            // our block change limit is Integer.MAX_VALUE, so will never
            // have to worry about this happening
            throw new RuntimeException(ex);
        }
    }
}