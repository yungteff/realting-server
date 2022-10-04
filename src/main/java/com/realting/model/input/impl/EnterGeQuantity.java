package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.grandexchange.GrandExchange;
import com.realting.model.entity.character.player.Player;

public class EnterGeQuantity extends EnterAmount {

	@Override
	public void handleAmount(Player player, int amount) {
		GrandExchange.setQuantity(player, amount);
	}

}
