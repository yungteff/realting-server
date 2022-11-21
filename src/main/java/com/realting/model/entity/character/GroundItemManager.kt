package com.realting.model.entity.character

import com.realting.engine.task.impl.GroundItemsTask
import com.realting.model.*
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.world.World
import com.realting.world.content.PlayerLogs
import com.realting.world.content.Sounds
import com.realting.world.content.player.skill.dungeoneering.Dungeoneering.doingDungeoneering
import com.realting.world.content.player.skill.dungeoneering.UltimateIronmanHandler.hasItemsStored
import java.util.concurrent.CopyOnWriteArrayList

object GroundItemManager {
    /*
	 * Our list which holds all grounditems, used an CopyOnWriteArrayList to prevent modification issues
	 * TODO: Change into a queue of some sort
	 */
    @JvmStatic
    val groundItems = CopyOnWriteArrayList<GroundItem>()

    /**
     * Removes a grounditem from the world
     * @param groundItem    The grounditem to remove from the world.
     * @param delistGItem    Should the grounditem be deleted from the arraylist aswell?
     */
    @JvmStatic
    fun remove(groundItem: GroundItem?, delistGItem: Boolean) {
        if (groundItem != null) {
            if (groundItem.isGlobal) {
                for (p in World.getPlayers()) {
                    if (p == null) continue
                    if (p.entityPosition.distanceToPoint(
                            groundItem.entityPosition.x, groundItem.entityPosition.y
                        ) <= 120
                    ) p.packetSender.removeGroundItem(
                        groundItem.item.id, groundItem.entityPosition.x, groundItem.entityPosition.y, groundItem.item.amount
                    )
                }
            } else {
                val person = World.getPlayerByName(groundItem.owner)
                if (person != null && person.entityPosition.distanceToPoint(
                        groundItem.entityPosition.x, groundItem.entityPosition.y
                    ) <= 120
                ) person.packetSender.removeGroundItem(
                    groundItem.item.id, groundItem.entityPosition.x, groundItem.entityPosition.y, groundItem.item.amount
                )
            }
            if (delistGItem) groundItems.remove(groundItem)
        }
    }

    /**
     * This method spawns a grounditem for a player.
     * @param p        The owner of the grounditem
     * @param g        The grounditem to spawn
     */
    @JvmStatic
    fun spawnGroundItem(p: Player?, g: GroundItem) {
        var varGroundItem = g
        if (p == null) // || p.getRights() == PlayerRights.DEVELOPER)
            return
        val item = varGroundItem.item
        if (item.id <= 0) {
            return
        }
        //if(item.getDefinition().getName().toLowerCase().contains("clue scroll"))
        //return;
        if (item.id >= 2412 && item.id <= 2414) {
            p.packetSender.sendMessage("The cape vanishes as it touches the ground.")
            return
        }
        if (doingDungeoneering(p)) {
            varGroundItem = GroundItem(item, varGroundItem.entityPosition, "Dungeoneering", true, -1, false, -1)
            p.minigameAttributes.dungeoneeringAttributes.party!!.groundItems.add(varGroundItem)
            if (item.id == 17489) {
                p.minigameAttributes.dungeoneeringAttributes.party!!.gatestonePosition = varGroundItem.entityPosition.copy()
            }
        }
        if (ItemDefinition.forId(item.id).isStackable) {
            val it = getGroundItem(p, item, varGroundItem.entityPosition)
            if (it != null) {
                it.item.amount =
                    if (it.item.amount + varGroundItem.item.amount > Int.MAX_VALUE) Int.MAX_VALUE else it.item.amount + varGroundItem.item.amount
                if (it.item.amount <= 0) remove(it, true) else it.isRefreshNeeded = true
                return
            }
        }
        /*
		if(Misc.getMinutesPlayed(p) < 15) {
			g.setGlobalStatus(false);
			g.setGoGlobal(false);
		}*/add(varGroundItem, true)
    }

    /**
     * Adds a grounditem to the world
     * @param groundItem    The grounditem to add to the world
     * @param listGItem        Should the grounditem be added to the arraylist?
     */
    @JvmStatic
    fun add(groundItem: GroundItem, listGItem: Boolean) {
        when {
            groundItem.isGlobal -> {
                for (p in World.getPlayers()) {
                    if (p == null) continue
                    if (groundItem.entityPosition.z == p.entityPosition.z && p.entityPosition.distanceToPoint(
                            groundItem.entityPosition.x, groundItem.entityPosition.y
                        ) <= 120
                    ) p.packetSender.createGroundItem(
                        groundItem.item.id, groundItem.entityPosition.x, groundItem.entityPosition.y, groundItem.item.amount
                    )
                }
            }
            else -> {
                val person = World.getPlayerByName(groundItem.owner)
                if (person != null && groundItem.entityPosition.z == person.entityPosition.z && person.entityPosition.distanceToPoint(
                        groundItem.entityPosition.x, groundItem.entityPosition.y
                    ) <= 120
                ) person.packetSender.createGroundItem(
                    groundItem.item.id, groundItem.entityPosition.x, groundItem.entityPosition.y, groundItem.item.amount
                )
            }
        }
        if (listGItem) {
            if (Locations.Location.getLocation(groundItem) === Locations.Location.DUNGEONEERING) groundItem.setShouldProcess(
                false
            )
            groundItems.add(groundItem)
            GroundItemsTask.fireTask()
        }
    }

    /**
     * Handles the pick up of a grounditem
     * @param p            The player picking up the item
     * @param item
     * @param position
     */
    @JvmStatic
    fun pickupGroundItem(p: Player, item: Item, position: Position) {
        var varItem = item
        if (!p.lastItemPickup.elapsed(500)) return
        val canAddItem = p.inventory.freeSlots > 0 || varItem.definition.isStackable && p.inventory.contains(varItem.id)
        if (!canAddItem) {
            p.inventory.full()
            return
        }
        val gt = getGroundItem(p, varItem, position)
        if (gt == null || gt.hasBeenPickedUp() || !groundItems.contains(gt)) //last one isn't needed, but hey, just trying to be safe
            return else {
            /*	if(p.getHostAdress().equals(gt.getFromIP()) && !p.getUsername().equals(gt.getOwner())) { //Transferring items by IP..

				p.getPacketSender().sendMessage("An error occured.");
				return;
			}*/
            if (hasItemsStored(p) && p.location !== Locations.Location.DUNGEONEERING) {
                p.packetSender.sendMessage("<shad=0>@red@You cannot pick up items until you claim your stored Dungeoneering items.")
                return
            }
            if (p.gameMode != GameMode.NORMAL && !doingDungeoneering(p)) {
                if (gt.owner != null && gt.owner != "null" && gt.owner != p.username) {
                    p.packetSender.sendMessage("You cannot pick this item up because it was not spawned for you.")
                    return
                }
            }
            if (varItem.id == 17489 && doingDungeoneering(p)) {
                p.minigameAttributes.dungeoneeringAttributes.party!!.gatestonePosition = null
            }
            varItem = gt.item
            if (varItem.id == 7509 && position == GlobalItemSpawner.ROCKCAKE_POSITION) {
                varItem = Item(7510, gt.item.amount)
            }
            gt.setPickedUp(true)
            remove(gt, true)
            p.inventory.add(varItem)
            if (ItemDefinition.forId(varItem.id) != null && ItemDefinition.forId(varItem.id).name != null) {
                PlayerLogs.log(p.username, "Picked up gr.Item " + varItem.definition.name + ", amount: " + varItem.amount)
            } else {
                PlayerLogs.log(p.username, "Picked up gr.Item " + varItem.id + ", amount: " + varItem.amount)
            }
            p.lastItemPickup.reset()
            Sounds.sendSound(p, Sounds.Sound.PICKUP_ITEM)
            p.inventory.processRefreshItems() // Instant refresh or it looks weird,
            // the item disappears then inventory update way later
        }
    }

    /**
     * Handles a region change for a player.
     * This method respawns all grounditems for a player who has changed region.
     * @param p        The player who has changed region
     */
    @JvmStatic
    fun handleRegionChange(p: Player) {
        for (gi in groundItems) {
            if (gi == null) continue
            p.packetSender.removeGroundItem(gi.item.id, gi.entityPosition.x, gi.entityPosition.y, gi.item.amount)
        }
        for (gi in groundItems) {
            if (gi == null || p.entityPosition.z != gi.entityPosition.z || p.entityPosition.distanceToPoint(
                    gi.entityPosition.x, gi.entityPosition.y
                ) > 120
            ) continue
            if (gi.isGlobal || !gi.isGlobal && gi.owner == p.username) p.packetSender.createGroundItem(
                gi.item.id, gi.entityPosition.x, gi.entityPosition.y, gi.item.amount
            )
        }
    }

    /**
     * Checks if a grounditem exists in the stated position.
     * @param p            The player trying to check if the grounditem exists
     * @param item        The grounditem's item
     * @param position    The position to check if a grounditem exists on
     * @return            true if a grounditem exists in the specified position, otherwise false
     */
    @JvmStatic
    fun getGroundItem(p: Player?, item: Item, position: Position): GroundItem? {
        for (l in groundItems) {
            if (l == null || l.entityPosition.z != position.z) continue
            if (l.entityPosition == position && l.item.id == item.id) {
                if (l.isGlobal) return l else if (p != null) {
                    val owner = World.getPlayerByName(l.owner)
                    if (owner == null || owner.index != p.index) continue
                    return l
                }
            }
        }
        return null
    }

    /**
     * Clears a position of ground items
     * @param pos        The position to remove all ground items on
     * @param owner        The owner of the grounditems to remove
     */
    fun clearArea(pos: Position, owner: String) {
        for (l in groundItems) {
            if (l == null || l.entityPosition.z != pos.z) continue
            if (l.entityPosition == pos && l.owner == owner) remove(l, true)
        }
    }
}