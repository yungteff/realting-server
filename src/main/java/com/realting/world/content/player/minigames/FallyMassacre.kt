package com.realting.world.content.minigames

import java.util.concurrent.CopyOnWriteArrayList
import java.util.Locale
import java.util.HashMap
import com.realting.model.GroundItem
import com.realting.model.entity.character.player.Player

object FallyMassacre {
    var TOTAL_PLAYERS = 0
    var PLAYERS_ALIVE = 0
    var GRIDCHANGES = 0
    var GRIDTOTAL = 0
    var GRIDCURRENT = 0

    /**
     * @note Stores player and State
     */
    private val playerMap: Map<Player, String> = HashMap()

    /*
	 * Stores items
	 */
    private val itemList = CopyOnWriteArrayList<GroundItem>()

    /**
     * @return HashMap Value
     */
    fun getState(player: Player): String? {
        return playerMap[player]
    }

    /**
     * @note States of minigames
     */
    const val WAITING = "WAITING"
    const val PLAYING = "PLAYING"

    /**
     * Is a game running?
     */
    private const val gameRunning = false
}