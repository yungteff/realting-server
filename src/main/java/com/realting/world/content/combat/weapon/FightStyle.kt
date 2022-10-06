package com.realting.world.content.combat.weapon

import com.realting.model.Skill
import com.realting.world.content.combat.CombatType

/**
 * A collection of constants that each represent a different fighting style.
 *
 * @author lare96
 */
enum class FightStyle {
    ACCURATE {
        override fun skill(type: CombatType): IntArray {
            return if (type === CombatType.RANGED) intArrayOf(Skill.RANGED.ordinal) else intArrayOf(Skill.ATTACK.ordinal)
        }
    },
    AGGRESSIVE {
        override fun skill(type: CombatType): IntArray {
            return if (type === CombatType.RANGED) intArrayOf(Skill.RANGED.ordinal) else intArrayOf(Skill.STRENGTH.ordinal)
        }
    },
    DEFENSIVE {
        override fun skill(type: CombatType): IntArray {
            return if (type === CombatType.RANGED) intArrayOf(
                Skill.RANGED.ordinal, Skill.DEFENCE.ordinal
            ) else intArrayOf(Skill.DEFENCE.ordinal)
        }
    },
    CONTROLLED {
        override fun skill(type: CombatType): IntArray {
            return intArrayOf(Skill.ATTACK.ordinal, Skill.STRENGTH.ordinal, Skill.DEFENCE.ordinal)
        }
    };

    /**
     * Determines the Skill trained by this fighting style based on the
     * [CombatType].
     *
     * @param type
     * the combat type to determine the Skill trained with.
     * @return the Skill trained by this fighting style.
     */
    abstract fun skill(type: CombatType): IntArray
}