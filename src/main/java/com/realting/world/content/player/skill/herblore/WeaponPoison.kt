package com.realting.world.content.player.skill.herblore

import com.realting.model.entity.character.CharacterEntity
import com.realting.model.container.impl.Equipment
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatFactory
import com.realting.world.content.combat.effect.CombatPoisonEffect.PoisonType
import java.util.HashMap

object WeaponPoison {
    /**
     * Starts the weapon poison event for each individual weapon item from the
     * enumeration `Weapon`.
     *
     * @param player
     * The Player player.
     * @param itemUse
     * The first item use.
     * @param useWith
     * The second item use.
     */
    @JvmStatic
    fun execute(player: Player, itemUse: Int, useWith: Int) {
        val weapon = Weapon.weapon[useWith]
        if (weapon != null) {
            for (element in weapon.newItemId) if (itemUse == element[0] && player.inventory.contains(itemUse)) {
                player.packetSender.sendMessage("You poison your weapon..")
                player.inventory.delete(element[0], 1)
                player.inventory.delete(weapon.itemId, 1)
                player.inventory.add(229, 1)
                player.inventory.add(element[1], 1)
            }
        }
    }

    /**
     * Checks if poison should be applied for a target.
     * @param p            The player who is going to apply poison onto the target.
     * @param target    The target who is going to be poisoned.
     */
    fun handleWeaponPoison(p: Player, target: CharacterEntity?) {
        val plrWeapon = p.equipment.items[Equipment.WEAPON_SLOT].id
        for (w in Weapon.weapon.values) {
            if (w != null) {
                var random = 0
                if (w.newItemId[0][1] == plrWeapon) //Player has p++
                    random = 5 else if (w.newItemId[1][1] == plrWeapon) //Player has p+
                    random = 10
                if (random > 0) {
                    if (Misc.getRandom(random) == 1) CombatFactory.poisonEntity(
                        target, if (random == 5) PoisonType.EXTRA else PoisonType.MILD
                    )
                    break
                }
            }
        }
    }

    /**
     * Represents a weapon that can be poisoned. Stores the initial weapon item
     * id, the type of poison used on the weapon and the new poisoned weapon
     * that will be obtained.
     *
     */
    private enum class Weapon
    /**
     * Creates the weapon.
     *
     * @param itemId
     * The weapon item id.
     * @param newItemId
     * The poisoned weapon item id.
     */(
        /**
         * The weapon item id.
         */
        val itemId: Int,
        /**
         * The poisoned weapon item id.
         */
        val newItemId: Array<IntArray>
    ) {
        /**
         * Dragon dagger.
         */
        DRAGON_DAGGER(1215, arrayOf(intArrayOf(5940, 5698), intArrayOf(5937, 5680))),

        /**
         * Rune dagger.
         */
        RUNE_DAGGER(1213, arrayOf(intArrayOf(5940, 5696), intArrayOf(5937, 5678))),

        /**
         * Adamant dagger.
         */
        ADAMANT_DAGGER(1211, arrayOf(intArrayOf(5940, 5694), intArrayOf(5937, 5676))),

        /**
         * Mithril dagger.
         */
        MITHRIL_DAGGER(1209, arrayOf(intArrayOf(5940, 5692), intArrayOf(5937, 5674))),

        /**
         * Black dagger.
         */
        BLACK_DAGGER(1217, arrayOf(intArrayOf(5940, 5700), intArrayOf(5937, 5682))),

        /**
         * Steel dagger.
         */
        STEEL_DAGGER(1207, arrayOf(intArrayOf(5940, 5690), intArrayOf(5937, 5672))),

        /**
         * Iron dagger.
         */
        IRON_DAGGER(1203, arrayOf(intArrayOf(5940, 5686), intArrayOf(5937, 5668))),

        /**
         * Bronze dagger.
         */
        BRONZE_DAGGER(1205, arrayOf(intArrayOf(5940, 5688), intArrayOf(5937, 5670)));
        /**
         * Gets the item id.
         *
         * @return the itemId
         */
        /**
         * @return the newItemId
         */

        companion object {
            /**
             * Represents a map for the weapon item ids.
             */
            var weapon = HashMap<Int, Weapon>()

            /**
             * Gets the weapon id by the item.
             *
             * @param id
             * The item id.
             * @return returns null if itemId is not a weapon.
             */
            fun forId(id: Int): Weapon? {
                return weapon[id]
            }

            /**
             * Populates a map for the weapons.
             */
            init {
                for (w in values()) weapon[w.itemId] = w
            }
        }
    }
}