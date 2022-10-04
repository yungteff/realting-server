package com.realting.model.entity.character.npc;

import com.realting.model.Position;
import com.realting.net.packet.ByteOrder;
import com.realting.net.packet.PacketBuilder;
import com.realting.model.entity.character.player.Player;

public class NPCFacing {

	public static void updateFacing(Player p, NPC n) {
		if(p.getNpcFacesUpdated().contains(n)) {
			return;
		}
		sendFaceUpdate(p, n);
		p.getNpcFacesUpdated().add(n);
	}
	
	private static void sendFaceUpdate(Player player, NPC npc) {
		if(npc.getMovementCoordinator().getCoordinator().isCoordinate() || npc.getCombatBuilder().isBeingAttacked() || npc.getMovementQueue().isMoving())
			return;
		final Position position = npc.getPositionToFace();
		int x = position == null ? 0 : position.getX(); 
		int y = position == null ? 0 : position.getY();
		player.getSession().queueMessage(new PacketBuilder(88).putShort(x * 2 + 1).putShort(y * 2 + 1).putShort(npc.getIndex(), ByteOrder.LITTLE));
	}
}
