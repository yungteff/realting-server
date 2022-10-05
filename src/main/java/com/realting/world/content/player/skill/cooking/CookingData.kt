package com.realting.world.content.player.skill.cooking

import com.realting.model.Skill
import com.realting.model.entity.character.player.Player
import java.security.SecureRandom

/**
 * Data for the cooking skill.
 * @author Admin Gabriel
 */
enum class CookingData(
    var rawItem: Int,
    var cookedItem: Int,
    var burntItem: Int,
    var levelReq: Int,
    var xp: Int,
    var stopBurn: Int,
    var foodName: String
) {
    RABBIT(3226, 3228, 7222, 1, 30, 33, "rabbit"), BIRD(9978, 9980, 9982, 1, 30, 33, "bird"), CHICKEN(
        2138, 2140, 2144, 1, 30, 33, "chicken"
    ),
    SHRIMP(317, 315, 7954, 1, 30, 33, "shrimp"), ANCHOVIES(321, 319, 323, 1, 30, 34, "anchovies"), TROUT(
        335, 333, 343, 15, 70, 50, "trout"
    ),
    COD(341, 339, 343, 18, 75, 54, "cod"), MACKEREL(353, 355, 357, 10, 60, 22, "mackerel"), SALMON(
        331, 329, 343, 25, 90, 58, "salmon"
    ),
    TUNA(359, 361, 367, 30, 100, 58, "tuna"), LOBSTER(377, 379, 381, 40, 120, 74, "lobster"), BASS(
        363, 365, 367, 40, 130, 75, "bass"
    ),
    SWORDFISH(371, 373, 375, 45, 140, 86, "swordfish"), MONKFISH(7944, 7946, 7948, 62, 150, 91, "monkfish"), SHARK(
        383, 385, 387, 80, 210, 94, "shark"
    ),
    SEA_TURTLE(395, 397, 399, 82, 212, 105, "sea turtle"), MANTA_RAY(389, 391, 393, 91, 217, 99, "manta ray"), ROCKTAIL(
        15270, 15272, 15274, 92, 225, 93, "rocktail"
    ),
    HEIM_CRAB(17797, 18159, 18179, 5, 22, 40, "heim crab"), RED_EYE(
        17799, 18161, 18181, 10, 41, 45, "red-eye"
    ),
    DUSK_EEL(17801, 18163, 18183, 12, 61, 47, "dusk eel"), GIANT_FLATFISH(
        17803, 18165, 18185, 15, 82, 50, "giant flatfish"
    ),
    SHORT_FINNED_EEL(17805, 18167, 18187, 18, 103, 54, "short-finned eel"), WEB_SNIPPER(
        17807, 18169, 18189, 30, 124, 60, "web snipper"
    ),
    BOULDABASS(17809, 18171, 18191, 40, 146, 75, "bouldabass"), SALVE_EEL(
        17811, 18173, 18193, 60, 168, 81, "salve eel"
    ),
    BLUE_CRAB(17813, 18175, 18195, 75, 191, 92, "blue crab");

    companion object {
        @JvmStatic
        fun forFish(fish: Int): CookingData? {
            for (data in values()) {
                if (data.rawItem == fish) {
                    return data
                }
            }
            return null
        }

        val cookingRanges = intArrayOf(12269, 2732, 114, 2728)

        @JvmStatic
        fun isRange(`object`: Int): Boolean {
            for (i in cookingRanges) if (`object` == i) return true
            return false
        }

        /**
         * Get's the rate for burning or successfully cooking food.
         * @param player    Player cooking.
         * @param food        Consumables's enum.
         * @return            Successfully cook food.
         */
        fun success(player: Player, burnBonus: Int, levelReq: Int, stopBurn: Int): Boolean {
            if (player.skillManager.getCurrentLevel(Skill.COOKING) >= stopBurn) {
                return true
            }
            var burn_chance = 45.0 - burnBonus
            val cook_level = player.skillManager.getCurrentLevel(Skill.COOKING).toDouble()
            val lev_needed = levelReq.toDouble()
            val burn_stop = stopBurn.toDouble()
            val multi_a = burn_stop - lev_needed
            val burn_dec = burn_chance / multi_a
            val multi_b = cook_level - lev_needed
            burn_chance -= multi_b * burn_dec
            val randNum = cookingRandom.nextDouble() * 100.0
            return burn_chance <= randNum
        }

        private val cookingRandom = SecureRandom() // The random factor
        fun canCook(player: Player, id: Int): Boolean {
            val fish = forFish(id) ?: return false
            if (player.skillManager.getMaxLevel(Skill.COOKING) < fish.levelReq) {
                player.packetSender.sendMessage("You need a Cooking level of atleast " + fish.levelReq + " to cook this.")
                return false
            }
            if (!player.inventory.contains(id)) {
                player.packetSender.sendMessage("You have run out of fish.")
                return false
            }
            return true
        }
    }
}