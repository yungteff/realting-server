package com.realting.world.content.player.events

import com.realting.model.GameMode
import com.realting.model.GroundItem
import com.realting.model.Item
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.GroundItemManager
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World

object Artifacts {
    var artifacts = intArrayOf(
        14876,
        14877,
        14878,
        14879,
        14880,
        14881,
        14882,
        14883,
        14884,
        14885,
        14886,
        14887,
        14888,
        14889,
        14890,
        14891,
        14892
    )

    @JvmStatic
    fun sellArtifacts(c: Player) {
        c.packetSender.sendInterfaceRemoval()
        var artifact = false
        for (k in artifacts.indices) {
            if (c.inventory.contains(artifacts[k])) {
                artifact = true
            }
        }
        if (!artifact) {
            c.packetSender.sendMessage("You do not have any Artifacts in your inventory to sell to Mandrith.")
            return
        }
        for (i in artifacts.indices) {
            for (item in c.inventory.validItems) {
                if (item.id == artifacts[i]) {
                    c.inventory.delete(artifacts[i], 1)
                    c.inventory.add(995, ItemDefinition.forId(artifacts[i]).value)
                    c.inventory.refreshItems()
                }
            }
        }
        c.packetSender.sendMessage("You've sold your artifacts.")
    }

    /*
	 * Artifacts
	 */
    private val LOW_ARTIFACTS = intArrayOf(14888, 14889, 14890, 14891, 14892)
    private val MED_ARTIFACTS = intArrayOf(14883, 14884, 14885, 14886)
    private val HIGH_ARTIFACTS = intArrayOf(14878, 14879, 14880, 14881, 14882)
    private val EXR_ARTIFACTS = intArrayOf(14876, 14877)
    private val PVP_ARMORS = intArrayOf(13899, 13893, 13887, 13902, 13896, 13890, 13858, 13861)

    /**
     * Handles a target drop
     * @param Player player		Player who has killed Player o
     * @param Player o			Player who has been killed by Player player
     */
    @JvmStatic
    fun handleDrops(killer: Player, death: Player, targetKill: Boolean) {
        if (killer.gameMode != GameMode.NORMAL) return
        if (Misc.getRandom(100) >= 85 || targetKill) GroundItemManager.spawnGroundItem(
            killer, GroundItem(
                Item(
                    getRandomItem(LOW_ARTIFACTS)
                ), death.position.copy(), killer.username, false, 110, true, 100
            )
        )
        if (Misc.getRandom(100) >= 90) GroundItemManager.spawnGroundItem(
            killer, GroundItem(
                Item(
                    getRandomItem(
                        MED_ARTIFACTS
                    )
                ), death.position.copy(), killer.username, false, 110, true, 100
            )
        )
        if (Misc.getRandom(100) >= 97) GroundItemManager.spawnGroundItem(
            killer, GroundItem(
                Item(
                    getRandomItem(
                        HIGH_ARTIFACTS
                    )
                ), death.position.copy(), killer.username, false, 110, true, 100
            )
        )
        if (Misc.getRandom(100) >= 99) GroundItemManager.spawnGroundItem(
            killer,
            GroundItem(Item(getRandomItem(PVP_ARMORS)), death.position.copy(), killer.username, false, 110, true, 100)
        )
        if (Misc.getRandom(100) >= 99) {
            val rareDrop = getRandomItem(EXR_ARTIFACTS)
            val itemName = Misc.formatText(ItemDefinition.forId(rareDrop).name)
            GroundItemManager.spawnGroundItem(
                killer, GroundItem(Item(rareDrop), death.position.copy(), killer.username, false, 110, true, 100)
            )
            World.sendMessage(
                "<img=10><col=009966><shad=0> " + killer.username + " has just received " + Misc.anOrA(
                    itemName
                ) + " " + itemName + " from Bounty Hunter!"
            )
        }
    }

    /**
     * Get's a random int from the array specified
     * @param array    The array specified
     * @return        The random integer
     */
    fun getRandomItem(array: IntArray): Int {
        return array[Misc.getRandom(array.size - 1)]
    }
}