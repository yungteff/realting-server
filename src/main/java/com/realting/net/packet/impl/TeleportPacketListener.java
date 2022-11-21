package com.realting.net.packet.impl;

import com.realting.model.PlayerRights;
import com.realting.model.Position;
import com.realting.model.entity.character.player.Player;
import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;

public class TeleportPacketListener implements PacketListener {
    public static final int PACKET_OPCODE = 242;

    @Override
    public void handleMessage(Player player, Packet packet) {
        int x = packet.readByte();
        int y = packet.readByte();
        if (player.getRights() == PlayerRights.DEVELOPER) {
            Position teleport = new Position(
                    player.getEntityPosition().getX() - (player.getEntityPosition().getLocalX(player.getLastKnownRegion()) - x),
                    player.getEntityPosition().getY() - (player.getEntityPosition().getLocalY(player.getLastKnownRegion()) - y),
                    player.getEntityPosition().getZ()
            );

            player.moveTo(teleport);
            player.getPacketSender().sendMessage("Teleported to: " + teleport);
        }
    }
}
