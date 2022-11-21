package com.realting.model.input.impl;

import com.realting.model.entity.character.player.Player;
import com.realting.model.input.EnterAmount;
import com.realting.world.content.player.skill.firemaking.Firemaking;
import com.realting.world.content.player.skill.firemaking.Logdata;
import com.realting.world.content.player.skill.firemaking.Logdata.logData;

public class EnterAmountOfLogsToAdd extends EnterAmount {

	
	@Override
	public void handleAmount(Player player, int amount) {
		Firemaking.lightFire(player, -1, true, amount);
		if(player.getInteractingObject() != null)
			player.setPositionToFace(player.getInteractingObject().getEntityPosition());
	}
	
	public static void openInterface(Player player) {
		player.getPacketSender().sendInterfaceRemoval();
		player.getSkillManager().stopSkilling();
		final logData lData = Logdata.getLogData(player, -1);
		if(lData == null) {
			player.getPacketSender().sendMessage("You do not have any logs to add to this fire.");
			return;
		}
		player.setInputHandling(new EnterAmountOfLogsToAdd());
		player.getPacketSender().sendEnterAmountPrompt("How many logs would you like to add to the fire?");
	}

}
