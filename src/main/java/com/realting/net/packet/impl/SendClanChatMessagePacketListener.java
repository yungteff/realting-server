package com.realting.net.packet.impl;

import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.util.Misc;
import com.realting.world.content.PlayerPunishment;
import com.realting.world.content.clan.ClanChatManager;
import com.realting.world.content.dialogue.DialogueManager;
import com.realting.model.entity.character.player.Player;

public class SendClanChatMessagePacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		String clanMessage = Misc.readString(packet.getBuffer());
		if(clanMessage == null || clanMessage.length() < 1)
			return;
		if(PlayerPunishment.muted(player.getUsername()) || PlayerPunishment.IPMuted(player.getHostAddress())) {
			player.getPacketSender().sendMessage("You are muted and cannot chat.");
			return;
		}
		if(Misc.blockedWord(clanMessage) && !(player.getRights().OwnerDeveloperOnly())) {
			DialogueManager.sendStatement(player, "A word was blocked in your sentence. Please do not repeat it!");
			return;
		}
		ClanChatManager.sendMessage(player, clanMessage);
	}

}
