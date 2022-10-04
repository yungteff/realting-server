package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.model.entity.character.player.Player;

public class EnterAmountToRemoveFromTrade extends EnterAmount {

	public EnterAmountToRemoveFromTrade(int item) {
		super(item);
	}
	
	@Override
	public void handleAmount(Player player, int amount) {
		if(player.getTrading().inTrade() && getItem() > 0) 
			player.getTrading().removeTradedItem(getItem(), amount);
		else
			player.getPacketSender().sendInterfaceRemoval();
	}
	
	
}
