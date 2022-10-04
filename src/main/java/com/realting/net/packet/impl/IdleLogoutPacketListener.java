package com.realting.net.packet.impl;

import com.realting.model.PlayerRights;
import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.model.entity.character.player.Player;

//CALLED EVERY 3 MINUTES OF INACTIVITY

public class IdleLogoutPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		if(player.getRights() == PlayerRights.MODERATOR || player.getRights() == PlayerRights.ADMINISTRATOR || player.getRights() == PlayerRights.OWNER || player.getRights() == PlayerRights.DEVELOPER)
			return;
		/*if(player.logout() && (player.getSkillManager().getSkillAttributes().getCurrentTask() == null || !player.getSkillManager().getSkillAttributes().getCurrentTask().isRunning())) {
			World.getPlayers().remove(player);
		}*/
		player.setInactive(true);
	}
}
