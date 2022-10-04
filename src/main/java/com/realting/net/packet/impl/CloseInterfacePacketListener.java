package com.realting.net.packet.impl;

import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.model.entity.character.player.Player;

public class CloseInterfacePacketListener implements PacketListener {
	
	@Override
	public void handleMessage(Player player, Packet packet) {
		player.getPacketSender().sendClientRightClickRemoval();
		player.getPacketSender().sendInterfaceRemoval();
	//	player.getPacketSender().sendTabInterface(Constants.CLAN_CHAT_TAB, 29328); //Clan chat tab
		//player.getAttributes().setSkillGuideInterfaceData(null);
	}
}
