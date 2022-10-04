package com.realting.net.packet.impl;

import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.model.entity.character.player.Player;

/**
 * This packet listener is called when a player's region has been loaded.
 * 
 * @author relex lawl
 */

public class FinalizedMapRegionChangePacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
	}
}