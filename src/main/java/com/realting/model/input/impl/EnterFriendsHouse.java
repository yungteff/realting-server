package com.realting.model.input.impl;

import com.realting.model.input.Input;
import com.realting.model.entity.character.player.Player;

public class EnterFriendsHouse extends Input {

	@Override
	public void handleSyntax(Player player, String syntax) {
		player.getPacketSender().sendInterfaceRemoval();
		if(syntax.length() < 3 || player.getUsername().equalsIgnoreCase(syntax))
			return;
		//Construction.enterFriendsHouse(player, syntax);
	}
}
