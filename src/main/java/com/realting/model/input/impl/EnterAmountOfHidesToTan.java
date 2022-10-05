package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.player.skill.crafting.Tanning;
import com.realting.model.entity.character.player.Player;

public class EnterAmountOfHidesToTan extends EnterAmount {

	private int button;
	public EnterAmountOfHidesToTan(int button) {
		this.button = button;
	}
	
	@Override
	public void handleAmount(Player player, int amount) {
		Tanning.tanHide(player, button, amount);
	}

}
