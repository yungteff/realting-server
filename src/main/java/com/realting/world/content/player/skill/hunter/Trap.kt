package com.realting.world.content.player.skill.hunter

import com.realting.model.GameObject
import com.realting.model.entity.character.player.Player

/**
 *
 * @author Rene
 */
open class Trap
/**
 * Reconstructs a new Trap
 *
 * @param object
 * @param state
 */(
    /**
     * The WorldObject linked to this HunterObject
     */
    var gameObject: GameObject,
    /**
     * This trap's state
     */
    var trapState: TrapState,
    /**
     * The amount of ticks this object should stay for
     */
    var ticks: Int,
    /**
     * Sets a trap's state
     *
     * @param trapState
     */
    var owner: Player?
) {
    /**
     * The possible states a trap can be in
     */
    enum class TrapState {
        SET, CAUGHT
    }
    /**
     * Gets the GameObject
     */
    /**
     * Sets the GameObject
     *
     * @param gameObject
     */
    /**
     * @return the ticks
     */
    /**
     * @param ticks
     * the ticks to set
     */
    /**
     * Gets a trap's state
     */

}