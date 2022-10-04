package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.skill.crafting.Gems;
import com.realting.model.entity.character.player.Player;

public class EnterAmountOfGemsToCut extends EnterAmount {

	@Override
	public void handleAmount(Player player, int amount) {
		if(player.getSelectedSkillingItem() > 0)
			Gems.cutGem(player, amount, player.getSelectedSkillingItem());
	}

}
