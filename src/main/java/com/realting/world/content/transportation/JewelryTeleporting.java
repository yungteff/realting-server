package com.realting.world.content.transportation;

import com.realting.model.Item;
import com.realting.model.Position;
import com.realting.model.container.impl.Equipment;
import com.realting.world.content.dialogue.DialogueManager;
import com.realting.model.entity.character.player.Player;

public class JewelryTeleporting {

	public static void rub(Player player, int item) {
		if(player.getInterfaceId() > 0)
			player.getPacketSender().sendInterfaceRemoval();
		player.setDialogueActionId(195);
		DialogueManager.start(player, 88);
		player.setSelectedSkillingItem(item);
	}

	public static void teleport(Player player, Position location) {
		if(!TeleportHandler.checkReqs(player, location)) {
			return;
		}
		if(!player.getClickDelay().elapsed(4500) || player.getMovementQueue().isLockMovement())
			return;
		int pItem = player.getSelectedSkillingItem();
		if(!player.getInventory().contains(pItem) && !player.getEquipment().contains(pItem)) {
			if (player.getRights().OwnerDeveloperOnly()) {
				player.getPacketSender().sendMessage("You do not have an "+pItem+"!");
			}
			return;
		}
		boolean inventory = !player.getEquipment().contains(pItem);
		if(pItem >= 1706 && pItem <= 1712 || pItem >= 11118 && pItem <= 11124) {
			boolean bracelet = pItem >= 11118 && pItem <= 11124;
			int newItem = bracelet ? (pItem + 2) : (pItem - 2);
			if(inventory) {
				player.getInventory().delete(pItem, 1).add(newItem, 1).refreshItems();
			} else {
				player.getEquipment().delete(player.getEquipment().getItems()[bracelet ? Equipment.HANDS_SLOT : Equipment.AMULET_SLOT]);
				player.getEquipment().setItem(bracelet ? Equipment.HANDS_SLOT : Equipment.AMULET_SLOT, new Item(newItem, 1));
				player.getEquipment().refreshItems();
			}
			if(newItem == 1704 || newItem == 11126) {
				player.getPacketSender().sendMessage("Your "+(bracelet ? "bracelet" : "amulet")+" has run out of charges.");
			}
		}
		player.setSelectedSkillingItem(-1);
		player.getPacketSender().sendInterfaceRemoval();
		TeleportHandler.teleportPlayer(player, location, TeleportType.RING_TELE);
	}
}
