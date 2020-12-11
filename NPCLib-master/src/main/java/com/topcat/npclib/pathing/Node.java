package com.topcat.npclib.pathing;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;

public class Node { // Holds data about each block we check

    static List<Material> liquids = new ArrayList<>();

    static {
        liquids.add(Material.WATER);
        liquids.add(Material.STATIONARY_WATER);
        // liquids.add(Material.LAVA); Maybe swimming in lava isn't the best
        // idea for npcs
        // liquids.add(Material.STATIONARY_LAVA);
        liquids.add(Material.LADDER); // Trust me it makes sense
    }

    int f, g = 0, h;
    int xPos, yPos, zPos;
    Node parent;
    public Block b;
    boolean notsolid, liquid;

    public Node(Block b) {
        this.b = b;
        xPos = b.getX();
        yPos = b.getY();
        zPos = b.getZ();
        update();
    }

    @SuppressWarnings("deprecation")
	public void update() {
        notsolid = true;
        if (b.getType() != Material.AIR) {
            final AxisAlignedBB box = net.minecraft.server.v1_8_R3.Block.getById(b.getTypeId()).a(((CraftWorld) b.getWorld()).getHandle(), new BlockPosition(b.getX(), b.getY(), b.getZ()), net.minecraft.server.v1_8_R3.Block.getById(b.getTypeId()).getBlockData());
            if (box != null) {
                if (Math.abs(box.e - box.b) > 0.2) {
                    notsolid = false;
                }
            }
        }
        liquid = liquids.contains(b.getType());
    }

}