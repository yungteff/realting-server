package com.realting.world.content.combat

import com.google.common.collect.Lists
import java.util.*

/**
 * A set of constants representing the three different types of combat that can
 * be used.
 *
 * @author lare96
 */
enum class CombatType @JvmOverloads constructor(val attackDistance: Int = 1) {
    MELEE(1), RANGED(8), MAGIC(10), DRAGON_FIRE, MIXED, NONE;

    companion object {
        @JvmField
        val COMBAT_TRIANGLE = Collections.unmodifiableList(Lists.newArrayList(MELEE, RANGED, MAGIC))
    }
}