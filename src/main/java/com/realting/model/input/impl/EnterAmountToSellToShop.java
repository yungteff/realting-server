package com.realting.model.input.impl;

import com.realting.model.container.impl.Shop;
import com.realting.model.input.EnterAmount;
import com.realting.model.entity.character.player.Player;

public class EnterAmountToSellToShop extends EnterAmount {

	public EnterAmountToSellToShop(int item, int slot) {
		super(item, slot);
	}
	
	@Override
	public void handleAmount(Player player, int amount) {
		if(player.isShopping() && getItem() > 0 && getSlot() >= 0) {
			Shop shop = player.getShop();
			if(shop != null) {
				if(getSlot() >= player.getInventory().getItems().length || player.getInventory().getItems()[getSlot()].getId() != getItem())
					return;
				player.getShop().setPlayer(player).forSlot(getSlot()).copy().setAmount(amount).copy();
				shop.sellItem(player, getSlot(), amount);
			} else
				player.getPacketSender().sendInterfaceRemoval();
		} else
			player.getPacketSender().sendInterfaceRemoval();
		
	}

}
