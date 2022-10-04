package com.realting.engine.task.impl;


import com.realting.engine.task.Task;
import com.realting.model.CombatIcon;
import com.realting.model.Graphic;
import com.realting.model.Hit;
import com.realting.model.Hitmask;
import com.realting.model.Locations.Location;
import com.realting.util.Misc;
import com.realting.model.entity.character.player.Player;

/**
 * Barrows
 * @author Gabriel Hannason
 */
public class CeilingCollapseTask extends Task {

	public CeilingCollapseTask(Player player) {
		super(9, player, false);
		this.player = player;
	}

	private Player player;

	@Override
	public void execute() {
		if(player == null || !player.isRegistered() || player.getLocation() != Location.BARROWS && player.getLocation() != Location.KRAKEN || player.getLocation() != Location.ZULRAH || player.getLocation() == Location.BARROWS && player.getPosition().getY() < 8000) {
			player.getPacketSender().sendCameraNeutrality();
			stop();
			return;
		}
		player.performGraphic(new Graphic(60));
		player.getPacketSender().sendMessage("Some rocks fall from the ceiling and hit you.");
		player.forceChat("Ouch!");
		player.dealDamage(new Hit(null, 30 + Misc.getRandom(20), Hitmask.RED, CombatIcon.BLOCK));
	}
}
