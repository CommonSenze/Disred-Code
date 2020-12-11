package com.topcat.npclib.nms;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import com.topcat.npclib.NPCManager;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_8_R3.PacketPlayInChat;
import net.minecraft.server.v1_8_R3.PacketPlayInCloseWindow;
import net.minecraft.server.v1_8_R3.PacketPlayInEntityAction;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_8_R3.PacketPlayInKeepAlive;
import net.minecraft.server.v1_8_R3.PacketPlayInTransaction;
import net.minecraft.server.v1_8_R3.PacketPlayInUpdateSign;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayInWindowClick;
import net.minecraft.server.v1_8_R3.PlayerConnection;

/**
 * 
 * @author martin
 */
public class NPCPlayerConnection extends PlayerConnection {

    public NPCPlayerConnection(NPCManager npcManager, EntityPlayer entityplayer) {
        super(npcManager.getNpcHandler().getServer().getMCServer(), npcManager.getNPCNetworkManager(), entityplayer);
    }

    @Override
    public CraftPlayer getPlayer() {
        return new CraftPlayer((CraftServer) Bukkit.getServer(), player); // Fake player prevents spout NPEs
    }

    @Override
    public void a(PacketPlayInFlying packet10flying) {
    }

    @Override
    public void a(double d0, double d1, double d2, float f, float f1) {
    }

    @Override
    public void a(PacketPlayInBlockDig packet14blockdig) {
    }

    @Override
    public void a(PacketPlayInBlockPlace packet15place) {
    }

    
    @Override
    public void a(PacketPlayInHeldItemSlot packet16blockitemswitch) {
    }

    @Override
    public void a(PacketPlayInChat packet3chat) {
    }

    @Override
    public void a(PacketPlayInArmAnimation packet18armanimation) {
    }

    @Override
    public void a(PacketPlayInEntityAction packet19entityaction) {
    }
    
    @SuppressWarnings("rawtypes")
	@Override
    public void sendPacket(Packet packet) {
    }

    @Override
    public void a(PacketPlayInUseEntity packet7useentity) {
    }

    @Override
    public void a(PacketPlayInKeepAlive packet9respawn) {
    }
    
    @Override
    public void a(PacketPlayInCloseWindow packet101closewindow) {
    }

    @Override
    public void a(PacketPlayInWindowClick packet102windowclick) {
    }

    @Override
    public void a(PacketPlayInTransaction packet106transaction) {
    }
    
    @Override
    public void a(PacketPlayInUpdateSign packet130updatesign) {
    }

}