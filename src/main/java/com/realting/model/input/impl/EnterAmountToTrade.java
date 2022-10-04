package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.model.entity.character.player.Player;

public class EnterAmountToTrade extends EnterAmount {

	public EnterAmountToTrade(int item, int slot) {
		super(item, slot);
	}
	
	@Override
	public void handleAmount(Player player, int amount) {
		if(player.getTrading().inTrade() && getItem() > 0 && getSlot() >= 0 && getSlot() < 28)
			player.getTrading().tradeItem(getItem(), amount, getSlot());
		else
			player.getPacketSender().sendInterfaceRemoval();
	}

}
