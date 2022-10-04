package com.realting.net.packet.impl;

import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.webhooks.discord.DiscordMessager;
import com.realting.world.World;
import com.realting.world.content.PlayerLogs;
import com.realting.world.content.PlayerPunishment.Jail;
import com.realting.model.entity.character.player.Player;

public class BadPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		player.getSkillManager().stopSkilling();
		World.sendMessage(player.getUsername()+" sent packet 109! Please report this to a Developer.");
		DiscordMessager.sendStaffMessage("PACKET 109 was sent by "+player.getUsername()+", badlistener jailed & kicked.");
		PlayerLogs.log(player.getUsername(), "Sent PACKET 109 and was jailed/kicked by badlistener.");
		if (!Jail.isJailed(player)) {
			Jail.jailPlayer(player);
		}
		World.deregister(player);
		return;
	}

}
