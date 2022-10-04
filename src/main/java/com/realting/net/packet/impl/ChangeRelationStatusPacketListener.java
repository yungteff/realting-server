package com.realting.net.packet.impl;

import com.realting.model.PlayerRelations.PrivateChatStatus;
import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.model.entity.character.player.Player;

public class ChangeRelationStatusPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int actionId = packet.readInt();
		player.getRelations().setStatus(PrivateChatStatus.forActionId(actionId), true);
	}

}
