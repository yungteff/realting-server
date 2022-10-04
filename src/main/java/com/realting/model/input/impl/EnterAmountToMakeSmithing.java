package com.realting.model.input.impl;

import com.realting.model.Item;
import com.realting.model.input.EnterAmount;
import com.realting.world.content.skill.smithing.EquipmentMaking;
import com.realting.world.content.skill.smithing.SmithingData;
import com.realting.model.entity.character.player.Player;

public class EnterAmountToMakeSmithing extends EnterAmount {

	public EnterAmountToMakeSmithing(int item, int slot) {
		super(item, slot);
	}

	@Override
	public void handleAmount(Player player, int amount) {
		Item item111 = new Item(getItem());
		int barsRequired = SmithingData.getBarAmount(item111);
		Item bar = new Item(player.getSelectedSkillingItem(), barsRequired);
		if(amount > (player.getInventory().getAmount(bar.getId()) / barsRequired))
			amount = (player.getInventory().getAmount(bar.getId()) / barsRequired);
		//int x = (player.getInventory().getAmount(bar.getId()) / barsRequired);
		EquipmentMaking.smithItem(player, new Item(player.getSelectedSkillingItem(), barsRequired), new Item(item111.getId(), SmithingData.getItemAmount(item111)), amount);
	}
}
