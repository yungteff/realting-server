package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.player.skill.smithing.Smelting;
import com.realting.world.content.player.skill.smithing.SmithingData;
import com.realting.model.entity.character.player.Player;

public class EnterAmountOfBarsToSmelt extends EnterAmount {

	public EnterAmountOfBarsToSmelt(int bar) {
		this.bar = bar;
	}
	
	@Override
	public void handleAmount(Player player, int amount) {
		for(int barId : SmithingData.SMELT_BARS) {
			if(barId == bar) {
				Smelting.smeltBar(player, barId, amount);
				break;
			}
		}
	}
	
	private int bar;
	
	public int getBar() {
		return bar;
	}
	
}
