package com.realting.model.input.impl;

import com.realting.model.Item;
import com.realting.model.container.impl.PriceChecker;
import com.realting.model.input.EnterAmount;
import com.realting.model.entity.character.player.Player;

public class EnterAmountToRemoveFromPriceCheck extends EnterAmount {


	public EnterAmountToRemoveFromPriceCheck(int item, int slot) {
		super(item, slot);
	}

	@Override
	public void handleAmount(Player player, int amount) {
		if(!player.getPriceChecker().isOpen() || player.getInterfaceId() != PriceChecker.INTERFACE_ID)
			return;
		if(!player.getPriceChecker().contains(getItem()))
			return;
		int invAmount = player.getPriceChecker().getAmount(getItem());
		if(amount > invAmount) 
			amount = invAmount;
		if(amount <= 0)
			return;
		player.getPriceChecker().switchItem(player.getInventory(), new Item(getItem(), amount), player.getPriceChecker().getSlot(getItem()), false, true);
	}
}
