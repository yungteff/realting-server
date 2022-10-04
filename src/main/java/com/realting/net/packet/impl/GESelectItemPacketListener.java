package com.realting.net.packet.impl;

import com.realting.model.Item;
import com.realting.model.definitions.ItemDefinition;
import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.world.content.grandexchange.GrandExchange;
import com.realting.model.entity.character.player.Player;

public class GESelectItemPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int item = packet.readShort();
		if(item <= 0)
			return;
		ItemDefinition def = ItemDefinition.forId(item);
		if(def != null) {
			if(def.getValue() <= 0 || !Item.tradeable(item) || item == 995) {
				player.getPacketSender().sendMessage("This item can currently not be purchased or sold in the Grand Exchange.");
				return;
			}
			GrandExchange.setSelectedItem(player, item);
		}
	}

}
