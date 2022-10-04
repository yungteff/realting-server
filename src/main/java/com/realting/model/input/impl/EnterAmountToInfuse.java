package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.skill.summoning.PouchMaking;
import com.realting.model.entity.character.player.Player;

public class EnterAmountToInfuse extends EnterAmount {

	@Override
	public void handleAmount(Player player, int amount) {
		if(player.getInterfaceId() != 63471) {
			player.getPacketSender().sendInterfaceRemoval();
			return;
		}
		PouchMaking.infusePouches(player, amount);
	}

}
