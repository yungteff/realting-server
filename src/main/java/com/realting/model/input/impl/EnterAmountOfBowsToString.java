package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.player.skill.fletching.Fletching;
import com.realting.model.entity.character.player.Player;

public class EnterAmountOfBowsToString extends EnterAmount {

	@Override
	public void handleAmount(Player player, int amount) {
		Fletching.stringBow(player, amount);
	}

}
