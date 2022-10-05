package com.realting.world.content.combat.effect

import com.realting.engine.task.Task
import com.realting.model.CombatIcon
import com.realting.model.Hit
import com.realting.model.Hitmask
import com.realting.model.Item
import com.realting.model.entity.character.CharacterEntity

/**
 * A [Task] implementation that handles the poisoning process.
 *
 * @author lare96
 */
class CombatPoisonEffect
/**
 * Create a new [CombatPoisonEffect].
 *
 * @param entity
 * the entity being inflicted with poison.
 */(
    /** The entity being inflicted with poison.  */
    private val entity: CharacterEntity
) : Task(33, entity, false) {
    /**
     * Holds all of the different strengths of poisons.
     *
     * @author lare96
     */
    enum class PoisonType
    /**
     * Create a new [PoisonType].
     *
     * @param damage
     * the starting damage for this poison type.
     */(
        /** The starting damage for this poison type.  */
        val damage: Int
    ) {
        MILD(50), EXTRA(70), SUPER(120);
        /**
         * Gets the starting damage for this poison type.
         *
         * @return the starting damage for this poison type.
         */

    }

    public override fun execute() {

        // Stop the task if the entity is unregistered.
        if (!entity.isRegistered || !entity.isPoisoned) {
            stop()
            return
        }

        // Deal the damage, then try and decrement the damage count.
        entity.dealDamage(Hit(null, entity.andDecrementPoisonDamage, Hitmask.DARK_GREEN, CombatIcon.NONE))
        /* if(entity.isPlayer()) {
        	((Player)entity).getPacketSender().sendInterfaceRemoval();
        }*/
    }

    /**
     * The small utility class that manages all of the combat poison data.
     *
     * @author lare96
     * @author Advocatus
     */
    object CombatPoisonData {
        /** The map of all of the different weapons that poison.  */ // Increase the capacity of the map as more elements are added.
        private val types: MutableMap<Int, PoisonType> = HashMap(97)

        /** Load all of the poison data.  */
        @JvmStatic
        fun init() {
            types[817] = PoisonType.MILD
            types[816] = PoisonType.MILD
            types[818] = PoisonType.MILD
            types[831] = PoisonType.MILD
            types[812] = PoisonType.MILD
            types[813] = PoisonType.MILD
            types[814] = PoisonType.MILD
            types[815] = PoisonType.MILD
            types[883] = PoisonType.MILD
            types[885] = PoisonType.MILD
            types[887] = PoisonType.MILD
            types[889] = PoisonType.MILD
            types[891] = PoisonType.MILD
            types[893] = PoisonType.MILD
            types[870] = PoisonType.MILD
            types[871] = PoisonType.MILD
            types[872] = PoisonType.MILD
            types[873] = PoisonType.MILD
            types[874] = PoisonType.MILD
            types[875] = PoisonType.MILD
            types[876] = PoisonType.MILD
            types[834] = PoisonType.MILD
            types[835] = PoisonType.MILD
            types[832] = PoisonType.MILD
            types[833] = PoisonType.MILD
            types[836] = PoisonType.MILD
            types[1221] = PoisonType.MILD
            types[1223] = PoisonType.MILD
            types[1219] = PoisonType.MILD
            types[1229] = PoisonType.MILD
            types[1231] = PoisonType.MILD
            types[1225] = PoisonType.MILD
            types[1227] = PoisonType.MILD
            types[1233] = PoisonType.MILD
            types[1253] = PoisonType.MILD
            types[1251] = PoisonType.MILD
            types[1263] = PoisonType.MILD
            types[1261] = PoisonType.MILD
            types[1259] = PoisonType.MILD
            types[1257] = PoisonType.MILD
            types[3094] = PoisonType.MILD
            types[5621] = PoisonType.EXTRA
            types[5620] = PoisonType.EXTRA
            types[5617] = PoisonType.EXTRA
            types[5616] = PoisonType.EXTRA
            types[5619] = PoisonType.EXTRA
            types[5618] = PoisonType.EXTRA
            types[5629] = PoisonType.EXTRA
            types[5628] = PoisonType.EXTRA
            types[5631] = PoisonType.EXTRA
            types[5630] = PoisonType.EXTRA
            types[5645] = PoisonType.EXTRA
            types[5644] = PoisonType.EXTRA
            types[5647] = PoisonType.EXTRA
            types[5646] = PoisonType.EXTRA
            types[5643] = PoisonType.EXTRA
            types[5642] = PoisonType.EXTRA
            types[5633] = PoisonType.EXTRA
            types[5632] = PoisonType.EXTRA
            types[5634] = PoisonType.EXTRA
            types[5660] = PoisonType.EXTRA
            types[5656] = PoisonType.EXTRA
            types[5657] = PoisonType.EXTRA
            types[5658] = PoisonType.EXTRA
            types[5659] = PoisonType.EXTRA
            types[5654] = PoisonType.EXTRA
            types[5655] = PoisonType.EXTRA
            types[5680] = PoisonType.EXTRA
            types[5623] = PoisonType.SUPER
            types[5622] = PoisonType.SUPER
            types[5625] = PoisonType.SUPER
            types[5624] = PoisonType.SUPER
            types[5627] = PoisonType.SUPER
            types[5626] = PoisonType.SUPER
            types[5698] = PoisonType.SUPER
            types[5730] = PoisonType.SUPER
            types[5641] = PoisonType.SUPER
            types[5640] = PoisonType.SUPER
            types[5637] = PoisonType.SUPER
            types[5636] = PoisonType.SUPER
            types[5639] = PoisonType.SUPER
            types[5638] = PoisonType.SUPER
            types[5635] = PoisonType.SUPER
            types[5661] = PoisonType.SUPER
            types[5662] = PoisonType.SUPER
            types[5663] = PoisonType.SUPER
            types[5652] = PoisonType.SUPER
            types[5653] = PoisonType.SUPER
            types[5648] = PoisonType.SUPER
            types[5649] = PoisonType.SUPER
            types[5650] = PoisonType.SUPER
            types[5651] = PoisonType.SUPER
            types[5667] = PoisonType.SUPER
            types[5666] = PoisonType.SUPER
            types[5665] = PoisonType.SUPER
            types[5664] = PoisonType.SUPER
        }

        /**
         * Gets the poison type of the specified item.
         *
         * @param item
         * the item to get the poison type of.
         *
         * @return the poison type of the specified item, or `null`
         * if the item is not able to poison the victim.
         */
        @JvmStatic
        fun getPoisonType(item: Item?): PoisonType? {
            return if (item == null || item.id < 1 || item.amount < 1) null else types[item.id]
        }
    }
}