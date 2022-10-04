package com.realting.net.packet.impl;

import com.realting.model.Skill;
import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.model.entity.character.player.Player;

public class PrestigeSkillPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int prestigeId = packet.readShort();
		Skill skill = Skill.forPrestigeId(prestigeId);
		if(skill == null) {
			return;
		}
		if(player.getInterfaceId() > 0) {
			player.getPacketSender().sendMessage("Please close all interfaces before doing this.");
			return;
		}
		player.getSkillManager().resetSkill(skill, true);
	}

}
