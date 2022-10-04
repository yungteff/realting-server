package com.realting.net.packet.impl;

import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.model.entity.character.player.Player;

public class WithdrawMoneyFromPouchPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int amount = packet.readInt();
	//	MoneyPouch.withdrawMoney(player, amount);
	}

}
