package com.realting.model.input.impl;

import com.realting.model.input.Input;
import com.realting.world.content.clan.ClanChatManager;
import com.realting.model.entity.character.player.Player;

public class EnterClanChatToJoin extends Input {

	@Override
	public void handleSyntax(Player player, String syntax) {
		if(syntax.length() <= 1) {
			player.getPacketSender().sendMessage("Invalid syntax entered.");
			return;
		}
		ClanChatManager.join(player, syntax);
	}
}
