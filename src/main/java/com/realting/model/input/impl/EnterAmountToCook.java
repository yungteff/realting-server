package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.skill.cooking.Cooking;
import com.realting.model.entity.character.player.Player;

public class EnterAmountToCook extends EnterAmount {

	@Override
	public void handleAmount(Player player, int amount) {
		if(player.getSelectedSkillingItem() > 0)
			Cooking.cook(player, player.getSelectedSkillingItem(), amount);
	}

}
