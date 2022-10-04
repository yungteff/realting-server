package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.minigames.Dueling;
import com.realting.model.entity.character.player.Player;

public class EnterAmountToRemoveFromStake extends EnterAmount {

	public EnterAmountToRemoveFromStake(int item) {
		super(item);
	}
	
	@Override
	public void handleAmount(Player player, int amount) {
		if((Dueling.checkDuel(player, 1) || Dueling.checkDuel(player, 2)) && getItem() > 0) {
			player.getDueling().removeStakedItem(getItem(), amount);
		} else
			player.getPacketSender().sendInterfaceRemoval();
	}

}
