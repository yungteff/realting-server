package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.WellOfGoodwill;
import com.realting.model.entity.character.player.Player;

public class DonateToWell extends EnterAmount {

	@Override
	public void handleAmount(Player player, int amount) {
		WellOfGoodwill.donate(player, amount);
	}

}
