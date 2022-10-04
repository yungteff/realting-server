package com.realting.world.content.skill.runecrafting

import com.realting.model.*
import com.realting.model.entity.character.player.Player

object RunecraftingData {
    fun getMakeAmount(rune: RuneData?, player: Player): Int {
        var amount = 1
        when (rune) {
            RuneData.AIR_RUNE -> {
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 11) amount = 2
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 22) amount = 3
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 33) amount = 4
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 44) amount = 5
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 55) amount = 6
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 66) amount = 7
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 77) amount = 8
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 88) amount = 9
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 99) amount = 10
            }
            RuneData.ASTRAL_RUNE -> if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 82) amount = 2
            RuneData.BLOOD_RUNE -> {}
            RuneData.BODY_RUNE -> {
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 46) amount = 2
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 92) amount = 3
            }
            RuneData.CHAOS_RUNE -> if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 74) amount = 2
            RuneData.COSMIC_RUNE -> if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 59) amount = 2
            RuneData.DEATH_RUNE -> {}
            RuneData.EARTH_RUNE -> {
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 26) amount = 2
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 52) amount = 3
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 78) amount = 4
            }
            RuneData.FIRE_RUNE -> {
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 35) amount = 2
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 70) amount = 3
            }
            RuneData.LAW_RUNE -> {}
            RuneData.MIND_RUNE -> {
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 14) amount = 2
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 28) amount = 3
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 42) amount = 4
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 56) amount = 5
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 70) amount = 6
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 84) amount = 7
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 98) amount = 8
            }
            RuneData.NATURE_RUNE -> if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 91) amount = 2
            RuneData.WATER_RUNE -> {
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 19) amount = 2
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 38) amount = 3
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 57) amount = 4
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 76) amount = 5
                if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) >= 95) amount = 6
            }
            else -> {}
        }
        return amount
    }

    enum class TalismanData(val talismanID: Int, val levelRequirement: Int, private val location: Position?) {
        AIR_TALISMAN(1438, 1, Position(2841, 4828)), MIND_TALISMAN(1448, 2, Position(2793, 4827)), WATER_TALISMAN(
            1444,
            5,
            Position(3482, 4834)
        ),
        EARTH_TALISMAN(1440, 9, Position(2655, 4829)), FIRE_TALISMAN(
            1442,
            14,
            Position(2576, 4846)
        ),
        BODY_TALISMAN(1446, 20, Position(2522, 4833)), COSMIC_TALISMAN(1454, 27, Position(2163, 4833)), CHAOS_TALISMAN(
            1452,
            35,
            Position(2282, 4837)
        ),
        ASTRAL_TALISMAN(-1, 40, null), NATURE_TALISMAN(1462, 44, Position(2400, 4834)), LAW_TALISMAN(
            1458,
            54,
            Position(2464, 4817)
        ),
        DEATH_TALISMAN(1456, 65, Position(2208, 4829)), BLOOD_TALISMAN(
            1450,
            77,
            Position(2468, 4888, 1)
        ),
        ARMADYL_TALISMAN(1460, 77, Position(2465, 4771));

        fun getLocation(): Position {
            return location!!.copy()
        }

        companion object {
            fun forId(talismanId: Int): TalismanData? {
                for (data in values()) {
                    if (data.talismanID == talismanId) {
                        return data
                    }
                }
                return null
            }
        }
    }

    enum class RuneData(
        val runeID: Int,
        val levelRequirement: Int,
        val xP: Int,
        val altarID: Int,
        private val pureRequired: Boolean
    ) {
        AIR_RUNE(556, 1, 5, 2478, false), MIND_RUNE(558, 2, 6, 2479, false), WATER_RUNE(
            555,
            5,
            7,
            2480,
            false
        ),
        EARTH_RUNE(557, 9, 8, 2481, false), FIRE_RUNE(554, 14, 10, 2482, false), BODY_RUNE(
            559,
            20,
            11,
            2483,
            false
        ),
        COSMIC_RUNE(564, 27, 12, 2484, true), CHAOS_RUNE(562, 35, 13, 2487, true), ASTRAL_RUNE(
            9075,
            40,
            14,
            17010,
            true
        ),
        NATURE_RUNE(561, 44, 15, 2486, true), LAW_RUNE(563, 54, 16, 2485, true), DEATH_RUNE(
            560,
            65,
            17,
            2488,
            true
        ),
        BLOOD_RUNE(565, 75, 24, 30624, true), ARMADYL_RUNE(21083, 77, 30, 47120, true);

        fun pureRequired(): Boolean {
            return pureRequired
        }

//         override val name: String
//            get() = ItemDefinition.forId(runeID).name

        companion object {
            @JvmStatic
            fun forId(objectId: Int): RuneData? {
                for (runes in values()) {
                    if (runes.altarID == objectId) {
                        return runes
                    }
                }
                return null
            }
        }
    }
}