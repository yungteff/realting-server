package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.skill.prayer.BonesOnAltar;
import com.realting.model.entity.character.player.Player;

public class EnterAmountOfBonesToSacrifice extends EnterAmount {

	@Override
	public void handleAmount(Player player, int amount) {
		BonesOnAltar.offerBones(player, amount);
	}

}
