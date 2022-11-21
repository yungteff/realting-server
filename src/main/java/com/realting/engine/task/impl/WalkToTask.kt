package com.realting.engine.task.impl

import com.realting.model.Locations
import com.realting.model.Position
import com.realting.model.entity.character.player.Player

/**
 * Represents a movement action for a game character.
 * @author Gabriel Hannason
 */
class WalkToTask(
    /**
     * The associated game character.
     */
    private val player: Player?,
    /**
     * The destination the game character will move to.
     */
    private val destination: Position?, distance: Int,
    /**
     * The task a player must execute upon reaching said destination.
     */
    private val finalizedTask: FinalizedMovementTask
) {
    interface FinalizedMovementTask {
        fun execute()
    }

    private var distance = -1

    /**
     * The WalkToTask constructor.
     * @param entity            The associated game character.
     * @param destination        The destination the game character will move to.
     * @param finalizedTask        The task a player must execute upon reaching said destination.
     */
    init {
        this.distance = distance
    }

    /**
     * Executes the action if distance is correct
     */
    fun tick() {
        if (player == null) return
        if (!player.isRegistered) {
            player.walkToTask = null
            return
        }
        if (player.isTeleporting || player.constitution <= 0 || destination == null) {
            player.walkToTask = null
            return
        }
        if (Locations.goodDistance(
                player.entityPosition.x,
                player.entityPosition.y,
                destination.x,
                destination.y,
                distance
            ) || destination == player.entityPosition
        ) {
            finalizedTask.execute()
            player.setEntityInteraction(null)
            player.walkToTask = null
        }
    }
}