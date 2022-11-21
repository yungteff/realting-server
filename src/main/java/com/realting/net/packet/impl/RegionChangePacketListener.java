package com.realting.net.packet.impl;

import com.realting.model.RegionInstance.RegionInstanceType;
import com.realting.model.entity.character.GroundItemManager;
import com.realting.model.entity.character.player.Player;
import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.world.clip.region.RegionClipping;
import com.realting.world.content.CustomObjects;
import com.realting.world.content.Sounds;
import com.realting.world.content.player.skill.hunter.Hunter;


public class RegionChangePacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		if(player.isAllowRegionChangePacket()) {
			RegionClipping.loadRegion(player.getEntityPosition().getX(), player.getEntityPosition().getY());
			player.getPacketSender().sendMapRegion();
			CustomObjects.handleRegionChange(player);
			GroundItemManager.handleRegionChange(player);
			Sounds.handleRegionChange(player);
			player.getTolerance().reset();
			Hunter.handleRegionChange(player);
			if(player.getRegionInstance() != null && player.getEntityPosition().getX() != 1 && player.getEntityPosition().getY() != 1) {
				if(player.getRegionInstance().equals(RegionInstanceType.BARROWS) || player.getRegionInstance().equals(RegionInstanceType.WARRIORS_GUILD))
					player.getRegionInstance().destruct();
			}
			player.getNpcFacesUpdated().clear();
			player.setRegionChange(false).setAllowRegionChangePacket(false);
		}
	}
}
