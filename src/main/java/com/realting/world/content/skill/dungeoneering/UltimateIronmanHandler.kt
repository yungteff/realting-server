package com.realting.world.content.skill.dungeoneering

import com.realting.model.Flag
import com.realting.world.content.PlayerLogs
import com.realting.model.container.impl.Equipment
import com.realting.model.definitions.WeaponInterfaces
import com.realting.model.entity.character.player.Player

object UltimateIronmanHandler {
    @JvmStatic
    fun hasItemsStored(player: Player): Boolean {
        return player.dungeoneeringIronEquipment.validItems.size + player.dungeoneeringIronInventory.validItems.size != 0
    }

    @JvmStatic
    fun handleQuickStore(player: Player) {
        if (player.busy()) {
            player.packetSender.sendMessage("You are far too busy to do that.")
            return
        }
        if (!player.clickDelay.elapsed(100)) {
            return
        }
        if (player.dungeoneeringIronEquipment.validItems.size > 0 || player.dungeoneeringIronInventory.validItems.size > 0) {
            player.packetSender.sendMessage("You must claim your gear first.")
            return
        }
        /*if (player.getEquipment().getValidItems().size() > 0 || player.getInventory().getValidItems().size() > 0) {
			player.getPacketSender().sendMessage("You cannot be wearing any gear, or have anything in your inventory.");
		}*/
        /**
         * @TODO put this on other check, but check against valid items already stored.
         */
        var items = player.inventory.validItems.size
        if (player.inventory.contains(15707)) {
            items -= 1
        }
        val equip = player.equipment.validItems.size
        if (items + equip <= 0) {
            player.packetSender.sendMessage("<shad=0>@gre@Found no valid items for storage.")
            return
        }
        for (i in player.inventory.validItems) {
            if (i.id == 15707) {
                continue
            }
            player.dungeoneeringIronInventory.add(i)
            PlayerLogs.log(player.username, "Stored " + i.amount + "x " + i.definition.name + " in dung HCIM inv")
            player.inventory.delete(i)
            //System.out.println("Added "+i.getAmount()+"x "+i.getDefinition().getName() +" to inventory list.");
        }
        for (i in player.equipment.validItems) {
            player.dungeoneeringIronEquipment.add(i)
            //System.out.println("Added "+i.getAmount()+"x "+i.getDefinition().getName() +" to equipment list.");
            PlayerLogs.log(player.username, "Stored " + i.amount + "x " + i.definition.name + " in dung HCIM equip")
            player.equipment.delete(i)
        }
        player.packetSender.sendMessage("<shad=0>@gre@Successfully stored " + equip + " equipment, and " + items + (if (items > 1) " items" else " item") + ". Total: " + (equip + items) + "/39.")
        val weapon = player.equipment[Equipment.WEAPON_SLOT]
        WeaponInterfaces.assign(player, weapon)
        player.inventory.refreshItems()
        player.equipment.refreshItems()
        player.updateFlag.flag(Flag.APPEARANCE)
        player.save()


        /*System.out.println("--------------------------------------");
		
		for (Item i : player.getDungeoneeringIronInventory().getValidItems()) {
			System.out.println("Found "+i.getAmount()+"x "+i.getDefinition().getName() +" in IronInventory");
		}
		
		for (Item i : player.getDungeoneeringIronEquipment().getValidItems()) {
			System.out.println("Found "+i.getAmount()+"x "+i.getDefinition().getName() +" in IronEquipment");
		}*/
    }

    @JvmStatic
    fun handleQuickRetrieve(player: Player) {
        if (player.busy()) {
            player.packetSender.sendMessage("You are far too busy to do that.")
            return
        }
        if (!player.clickDelay.elapsed(100)) {
            player.packetSender.sendMessage("You're interacting too fast.")
            return
        }
        if (player.equipment.validItems.size > 0 && player.dungeoneeringIronEquipment.validItems.size > 0) {
            player.packetSender.sendMessage("You must not be wearing anything to claim your equipment.")
            return
        }
        if (player.inventory.freeSlots < player.dungeoneeringIronInventory.validItems.size) {
            player.packetSender.sendMessage("You must have at least " + player.dungeoneeringIronInventory.validItems.size + " free inventory slots first.")
            return
        }
        val total =
            player.dungeoneeringIronEquipment.validItems.size + player.dungeoneeringIronInventory.validItems.size
        if (total <= player.inventory.freeSlots && total > 0) {
            val equip = player.dungeoneeringIronEquipment.validItems.size
            val leftover = player.dungeoneeringIronInventory.validItems.size
            for (i in player.dungeoneeringIronEquipment.validItems) {
                player.inventory.add(i)
                PlayerLogs.log(
                    player.username, "Retrieved " + i.amount + "x " + i.definition.name + " in dung HCIM equip"
                )
                player.dungeoneeringIronEquipment.delete(i)
            }
            for (i in player.dungeoneeringIronInventory.validItems) {
                player.inventory.add(i)
                PlayerLogs.log(
                    player.username, "Retrieved " + i.amount + "x " + i.definition.name + " in dung HCIM inv"
                )
                player.dungeoneeringIronInventory.delete(i)
            }
            player.packetSender.sendMessage("<shad=0>@gre@Your final " + (if (equip + leftover > 1) "$equip $leftover items are " else "item is ") + "returned to you.")
        } else if (player.dungeoneeringIronEquipment.validItems.size > 0) {
            val equip = player.dungeoneeringIronEquipment.validItems.size
            val leftover = player.dungeoneeringIronInventory.validItems.size
            for (i in player.dungeoneeringIronEquipment.validItems) {
                player.inventory.add(i)
                PlayerLogs.log(
                    player.username, "Retrieved " + i.amount + "x " + i.definition.name + " in dung HCIM equip"
                )
                player.dungeoneeringIronEquipment.delete(i)
            }
            if (player.dungeoneeringIronInventory.validItems.size > 0) {
                player.packetSender.sendMessage("<shad=0>@gre@You retrieve " + equip + " equipment, and have " + (if (leftover > 1) "$leftover items" else "item") + " remaining.")
            } else {
                player.packetSender.sendMessage("<shad=0>@gre@Your worn equipment is returned to you.")
            }
        } else if (player.dungeoneeringIronInventory.validItems.size > 0) {
            val inv = player.dungeoneeringIronInventory.validItems.size
            for (i in player.dungeoneeringIronInventory.validItems) {
                player.inventory.add(i)
                PlayerLogs.log(
                    player.username, "Retrieved " + i.amount + "x " + i.definition.name + " in dung HCIM inv"
                )
                player.dungeoneeringIronInventory.delete(i)
            }
            player.packetSender.sendMessage("<shad=0>@gre@Your final " + (if (inv > 1) "$inv items are " else "item is ") + "returned to you.")
        } else {
            player.packetSender.sendMessage("<shad=0>@gre@You don't have any other items stored.")
        }
        val weapon = player.equipment[Equipment.WEAPON_SLOT]
        WeaponInterfaces.assign(player, weapon)
        player.inventory.refreshItems()
        player.equipment.refreshItems()
        player.updateFlag.flag(Flag.APPEARANCE)
        player.save()
    }
}