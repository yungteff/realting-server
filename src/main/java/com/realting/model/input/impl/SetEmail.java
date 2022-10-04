package com.realting.model.input.impl;

import com.realting.model.input.Input;
import com.realting.model.entity.character.player.Player;

public class SetEmail extends Input {

	@Override
	public void handleSyntax(Player player, String syntax) {
		player.getPacketSender().sendInterfaceRemoval();
		if(true) {
			player.getPacketSender().sendMessage("This service is currently unavailable.");
			return;
		}
	}
}
