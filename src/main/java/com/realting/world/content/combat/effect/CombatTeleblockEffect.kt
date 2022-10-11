package com.realting.world.content.combat.effect

import com.realting.engine.task.Task
import com.realting.model.entity.character.player.Player

/**
 * A [Task] implementation that will unteleblock the player after the
 * counter reaches 0.
 *
 * @author lare96
 */
class CombatTeleblockEffect(player: Player) : Task(1, false) {
    /** The player attached to this task.  */
    private val player: Player

    /**
     * Create a new [CombatTeleblockEffect].
     *
     * @param player
     * the player attached to this task.
     */
    init {
        super.bind(player)
        this.player = player
    }

    public override fun execute() {

        // Timer is at or below 0 so send them a message saying they're not
        // blocked anymore
        if (player.teleblockTimer <= 0) {
            player.packetSender.sendMessage(
                "You are no longer teleblocked."
            )
            stop()
            return
        }

        // Otherwise just decrement the timer.
        player.decrementTeleblockTimer()
    }
}