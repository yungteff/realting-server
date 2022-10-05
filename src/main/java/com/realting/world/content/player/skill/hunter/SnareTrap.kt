package com.realting.world.content.player.skill.hunter

import com.realting.model.GameObject
import com.realting.model.entity.character.player.Player

/**
 *
 * @author Faris
 */
class SnareTrap(obj: GameObject, state: TrapState, ticks: Int, p: Player?) : Trap(obj, state, ticks, p) {
    /**
     * @return the state
     */
    /**
     * @param state
     * the state to set
     */
    var state: TrapState? = null
}