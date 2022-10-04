package com.realting.world.content.skill.mining

import com.realting.model.Skill
import com.realting.model.container.impl.Equipment
import com.realting.model.entity.character.player.Player

object MiningData {
    val RANDOM_GEMS = intArrayOf(1623, 1621, 1619, 1617, 1631)
    fun forPick(id: Int): Pickaxe? {
        return Pickaxe.values().firstOrNull { it.id == id }
    }

    @JvmStatic
    fun forRock(id: Int): Ores? {
        for (ore in Ores.values()) {
            for (obj in ore.objid) {
                if (obj == id) {
                    return ore
                }
            }
        }
        return null
    }

    fun getPickaxe(plr: Player): Int {
        Pickaxe.values().forEach { p ->
            when {
                plr.equipment.items[Equipment.WEAPON_SLOT].id == p.id -> return p.id
                plr.inventory.contains(p.id) -> return p.id
            }
        }
        return -1
    }

    fun isHoldingPickaxe(player: Player): Boolean {
        for (p in Pickaxe.values()) {
            if (player.equipment.items[Equipment.WEAPON_SLOT].id == p.id) {
                return true
            }
        }
        return false
    }

    fun getReducedTimer(plr: Player, pickaxe: Pickaxe): Int {
        val skillReducement = (plr.skillManager.getMaxLevel(Skill.MINING) * 0.03).toInt()
        val pickaxeReducement = pickaxe.speed.toInt()
        return skillReducement + pickaxeReducement
    }

    enum class Pickaxe(//12003
        val id: Int, val req: Int, val anim: Int, val speed: Double
    ) {
        Bronze(1265, 1, 625, 1.0),  //625
        Iron(1267, 1, 626, 1.05),  //626
        Steel(1269, 6, 627, 1.1),  //627
        Mithril(1273, 21, 628, 1.2),  //628
        Adamant(1271, 31, 629, 1.25),  //629
        Rune(1275, 41, 624, 1.3),  //624
        Adze(13661, 80, 10226, 1.60),  //10226
        Dragon(15259, 61, 12188, 1.65),  //12188
        Sacred(14130, 61, 11019, 1.60);

    }

    enum class Ores(
        val objid: IntArray,
        val levelReq: Int,
        val xpAmount: Int,
        val itemId: Int,
        val ticks: Int,
        val respawn: Int
    ) {
        Rune_essence(intArrayOf(24444), 1, 10, 1436, 2, -1), Pure_essence(intArrayOf(24445), 17, 25, 7936, 3, -1), Clay(
            intArrayOf(9711, 9712, 9713, 15503, 15504, 15505),
            1,
            5,
            434,
            3,
            2
        ),
        Copper(
            intArrayOf(3042, 9708, 9709, 9710, 11936, 11960, 11961, 11962, 11189, 11190, 11191, 29231, 29230, 2090),
            1,
            18,
            436,
            4,
            4
        ),
        Tin(
            intArrayOf(3043, 9714, 9715, 9716, 11933, 11957, 11958, 11959, 11186, 11187, 11188, 2094, 29227, 29229),
            1,
            18,
            438,
            4,
            4
        ),
        Iron(
            intArrayOf(9717, 9718, 9719, 2093, 2092, 11954, 11955, 11956, 29221, 29222, 29223),
            15,
            35,
            440,
            5,
            5
        ),
        Silver(intArrayOf(2100, 2101, 29226, 29225, 11948, 11949), 20, 40, 442, 5, 7), Coal(
            intArrayOf(
                2097,
                5770,
                29216,
                29215,
                29217,
                11965,
                11964,
                11963,
                11930,
                11931,
                11932
            ), 30, 50, 453, 5, 7
        ),
        Gold(intArrayOf(9720, 9721, 9722, 11951, 11183, 11184, 11185, 2099), 40, 65, 444, 5, 10), Mithril(
            intArrayOf(
                25370,
                25368,
                5786,
                5784,
                11942,
                11943,
                11944,
                11945,
                11946,
                29236,
                11947,
                11942,
                11943
            ), 50, 80, 447, 6, 11
        ),
        Adamantite(intArrayOf(11941, 11939, 29233, 29235), 70, 95, 449, 7, 14), Runite(
            intArrayOf(
                14859,
                4860,
                2106,
                2107
            ), 85, 125, 451, 7, 45
        ),
        CRASHED_STAR(intArrayOf(38660), 80, 52, 13727, 7, -1);
    }
}