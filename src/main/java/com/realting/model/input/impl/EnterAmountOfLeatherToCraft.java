package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.skill.crafting.LeatherMaking;
import com.realting.world.content.skill.crafting.leatherData;
import com.realting.model.entity.character.player.Player;

public class EnterAmountOfLeatherToCraft extends EnterAmount {
	
	@Override
	public void handleAmount(Player player, int amount) {
		for (final leatherData l : leatherData.values()) {
			if (player.getSelectedSkillingItem() == l.getLeather()) {
				LeatherMaking.craftLeather(player, l, amount);
				break;
			}
		}
	}
}
