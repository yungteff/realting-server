package com.realting.world.content.combat

import com.realting.model.Locations
import com.realting.model.entity.character.CharacterEntity

class CombatDistanceSession
/**
 * Create a new [CombatDistanceSession].
 *
 * @param builder
 * the combat builder.
 * @param victim
 * the victim being hunted.
 */(
    /** The combat builder.  */
    private val builder: CombatBuilder,
    /** The victim being hunted.  */
    private val victim: CharacterEntity
) {
    fun process() {
        builder.determineStrategy()
        builder.attackTimer = 0
        if (builder.victim != null && builder.victim != victim) {
            builder.reset(true)
            stop()
            return
        }
        if (!Locations.Location.ignoreFollowDistance(builder.character)) {
            if (!Locations.goodDistance(builder.character.position, victim.position, 40)) {
                builder.reset(true)
                stop()
                return
            }
        }
        if (Locations.goodDistance(
                builder.character.position, victim.position, builder.strategy.attackDistance(
                    builder.character
                )
            )
        ) {
            successFul()
            stop()
            return
        }
    }

    fun stop() {
        builder.distanceSession = null
    }

    private fun successFul() {
        builder.character.movementQueue.reset()
        builder.victim = victim
        builder.combatSession = CombatSession(builder)
    }
}