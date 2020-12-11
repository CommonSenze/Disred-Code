package com.topcat.npclib.nms;

import java.io.IOException;
import java.lang.reflect.Field;

import net.minecraft.server.v1_8_R3.EnumProtocol;
import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.Packet;

/**
 * 
 * @author martin
 */
public class NPCNetworkManager extends NetworkManager {

    public NPCNetworkManager() throws IOException {
        super(EnumProtocolDirection.SERVERBOUND);
        
        try {
            final Field f = NetworkManager.class.getDeclaredField("p");
            f.setAccessible(true);
            f.set(this, false);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
	@Override
    public void handle(Packet packet) {
    }
    
    @Override
    public void a(EnumProtocol protocol) {
    }

    @Override
    public void a() {
    }

}