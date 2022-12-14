package com.realting.engine.task.impl;

import com.realting.engine.task.Task;
import com.realting.world.content.combat.weapon.CombatSpecial;
import com.realting.model.entity.character.player.Player;

public class PlayerSpecialAmountTask extends Task {

	public PlayerSpecialAmountTask(Player player) {
		super(20, player, false);
		this.player = player;
		player.setRecoveringSpecialAttack(true);
	}
	
	private final Player player;
	
	@Override
	public void execute() {
		if (player == null || !player.isRegistered() || player.getSpecialPercentage() >= 100 || !player.isRecoveringSpecialAttack()) {
			player.setRecoveringSpecialAttack(false);
			stop();
			return;
		}
		int amount = player.getSpecialPercentage() + 5;
		if (amount >= 100) {
			amount = 100;
			player.setRecoveringSpecialAttack(false);
			stop();
		}
		player.setSpecialPercentage(amount);
		CombatSpecial.updateBar(player);
		if(player.getSpecialPercentage() % 50 == 0)
			player.getPacketSender().sendMessage("Your special attack energy is now " + player.getSpecialPercentage() + "%.");
	}
}