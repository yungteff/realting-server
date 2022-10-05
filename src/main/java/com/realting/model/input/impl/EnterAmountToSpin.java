package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.player.skill.crafting.Flax;
import com.realting.model.entity.character.player.Player;

public class EnterAmountToSpin extends EnterAmount {

	@Override
	public void handleAmount(Player player, int amount) {
		Flax.spinFlax(player, amount);
	}

}
