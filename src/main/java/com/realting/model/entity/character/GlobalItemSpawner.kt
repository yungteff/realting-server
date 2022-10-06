package com.realting.model.entity.character

import com.realting.model.GroundItem
import com.realting.model.Item
import com.realting.model.Position
import com.realting.model.entity.character.player.Player
import com.realting.world.World

object GlobalItemSpawner {
    @JvmField
	var ROCKCAKE_POSITION = Position(3667, 2994, 0)
    private var timer = System.currentTimeMillis()
    @JvmStatic
	fun startup() {
        if (System.currentTimeMillis() - timer > 1000 * 60) { //every minute run spawnBarrowSpade for each online player
            World.sendGlobalGroundItems() //This loops through all online players who aren't null, then sends them back to spawnBarrowSpade after filtration
        }
    }

    @JvmStatic
	fun spawnGlobalGroundItems(player: Player) {
        nullCheckAndSpawn(player, Item(952, 1), Position(3571, 3312, 0))
        nullCheckAndSpawn(player, Item(1351, 1), Position(2693, 9560, 0))
        nullCheckAndSpawn(player, Item(1949, 1), Position(3142, 3453, 0))
        nullCheckAndSpawn(player, Item(1005, 1), Position(3143, 3453, 0))
        nullCheckAndSpawn(player, Item(946, 1), Position(3205, 3212, 0))
        nullCheckAndSpawn(player, Item(1923, 1), Position(3208, 3214, 0))
        nullCheckAndSpawn(player, Item(1931, 1), Position(3209, 3214, 0))
        nullCheckAndSpawn(player, Item(1935, 1), Position(3211, 3212, 0))
        nullCheckAndSpawn(player, Item(558, 1), Position(3206, 3208, 0))
        nullCheckAndSpawn(player, Item(7509, 1), ROCKCAKE_POSITION)
        timer = System.currentTimeMillis()
    }

    private fun nullCheckAndSpawn(player: Player, item: Item, pos: Position) {
        if (GroundItemManager.getGroundItem(player, item, pos) == null) {
            GroundItemManager.spawnGroundItem(
                player,
                GroundItem(item, pos, player.username, false, 60 * 60, false, 0)
            ) //each player will have an instance of the shovel, will last 60*60 seconds (1 hr)
        }
    }
}