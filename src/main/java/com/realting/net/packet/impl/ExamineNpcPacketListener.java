package com.realting.net.packet.impl;

import com.realting.model.definitions.NpcDefinition;
import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.model.entity.character.player.Player;

public class ExamineNpcPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int npc = packet.readShort();
		if(npc <= 0) {
			return;
		}
		NpcDefinition npcDef = NpcDefinition.forId(npc);
		if(npcDef != null) {
			player.getPacketSender().sendMessage(npcDef.getExamine());
		}
	}

}
