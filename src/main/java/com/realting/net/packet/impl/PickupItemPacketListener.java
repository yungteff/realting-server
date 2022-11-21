package com.realting.net.packet.impl;

import com.realting.engine.task.impl.WalkToTask;
import com.realting.engine.task.impl.WalkToTask.FinalizedMovementTask;
import com.realting.model.GroundItem;
import com.realting.model.Item;
import com.realting.model.Position;
import com.realting.model.container.impl.Equipment;
import com.realting.model.entity.character.GroundItemManager;
import com.realting.model.entity.character.player.Player;
import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.util.Misc;

/**
 * This packet listener is used to pick up ground items
 * that exist in the world.
 * 
 * @author relex lawl
 */

public class PickupItemPacketListener implements PacketListener {

	@Override
	public void handleMessage(final Player player, Packet packet) {
		final int y = packet.readLEShort();
		final int itemId = packet.readUnsignedShort();
		final int x = packet.readLEShort();
		if(player.isTeleporting())
			return;
		final Position position = new Position(x, y, player.getEntityPosition().getZ());
		if(!player.getLastItemPickup().elapsed(500))
			return;
		if(player.getConstitution() <= 0 || player.isTeleporting())
			return;
		player.setWalkToTask(new WalkToTask(player, position, 1, new FinalizedMovementTask() {
			@Override
			public void execute() {
				if (Math.abs(player.getEntityPosition().getX() - x) > 25 || Math.abs(player.getEntityPosition().getY() - y) > 25) {
					player.getMovementQueue().reset();
					return;
				}
				boolean canPickup = Misc.canAddItemToInventory(player, itemId);
				if(!canPickup) {
					player.getInventory().full();
					return;
				}
				GroundItem gItem = GroundItemManager.getGroundItem(player, new Item(itemId), position);
				if(itemId == 7509 && player.getEquipment().forSlot(Equipment.HANDS_SLOT).getId() != 1580) {
					player.getPacketSender().sendMessage("This rock cake is too hot. Ice gloves could help.");
					return;
				}
				if(gItem != null) {
					if(player.getInventory().getAmount(gItem.getItem().getId()) + gItem.getItem().getAmount() > Integer.MAX_VALUE || player.getInventory().getAmount(gItem.getItem().getId()) + gItem.getItem().getAmount() <= 0) {
						player.getPacketSender().sendMessage("You cannot hold that amount of this item. Clear your inventory!");
						return;
					}
					GroundItemManager.pickupGroundItem(player, new Item(itemId), new Position(x, y, player.getEntityPosition().getZ()));
				}
			}
		}));
	}
}
