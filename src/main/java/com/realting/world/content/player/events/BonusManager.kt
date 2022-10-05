package com.realting.world.content.player.events

import com.realting.model.Prayerbook
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.world.content.combat.prayer.CurseHandler

class BonusManager {
    val attackBonus = DoubleArray(5)
    val defenceBonus = DoubleArray(9)
    val otherBonus = DoubleArray(4)

    companion object {
        @JvmStatic
        fun update(player: Player) {
            val bonuses = DoubleArray(18)
            for (item in player.equipment.items) {
                val definition = ItemDefinition.forId(item.id)
                for (bonus in definition.bonuses) {
                    bonuses[bonus.bonus.ordinal] += bonus.value
                }
            }
            for (i in STRING_ID.indices) {
                if (i <= 4) {
                    player.bonusManager.attackBonus[i] = bonuses[i]
                } else if (i <= 13) {
                    val index = i - 5
                    player.bonusManager.defenceBonus[index] = bonuses[i]
                } else if (i <= 17) {
                    val index = i - 14
                    player.bonusManager.otherBonus[index] = bonuses[i]
                }
                val interfaceId = Integer.valueOf(STRING_ID[i][0])
                if (interfaceId == 18895) {
                    // Magic damage boost
                    player.packetSender.sendString(interfaceId, STRING_ID[i][1] + ": " + bonuses[i] + "%")
                } else {
                    player.packetSender.sendString(interfaceId, STRING_ID[i][1] + ": " + bonuses[i])
                }
            }
        }

        private val STRING_ID = arrayOf(
            arrayOf("1675", "Stab"),
            arrayOf("1676", "Slash"),
            arrayOf("1677", "Crush"),
            arrayOf("1678", "Magic"),
            arrayOf("1679", "Range"),
            arrayOf("1680", "Stab"),
            arrayOf("1681", "Slash"),
            arrayOf("1682", "Crush"),
            arrayOf("1683", "Magic"),
            arrayOf("1684", "Range"),
            arrayOf("18890", "Summoning"),
            arrayOf("18891", "Absorb Melee"),
            arrayOf("18892", "Absorb Magic"),
            arrayOf("18893", "Absorb Ranged"),
            arrayOf("1686", "Strength"),
            arrayOf("18894", "Ranged Strength"),
            arrayOf("1687", "Prayer"),
            arrayOf("18895", "Magic Damage")
        )
        const val ATTACK_STAB = 0
        const val ATTACK_SLASH = 1
        const val ATTACK_CRUSH = 2
        const val ATTACK_MAGIC = 3
        const val ATTACK_RANGE = 4
        const val DEFENCE_STAB = 0
        const val DEFENCE_SLASH = 1
        const val DEFENCE_CRUSH = 2
        const val DEFENCE_MAGIC = 3
        const val DEFENCE_RANGE = 4
        const val DEFENCE_SUMMONING = 5
        const val ABSORB_MELEE = 6
        const val ABSORB_MAGIC = 7
        const val ABSORB_RANGED = 8
        const val BONUS_STRENGTH = 0
        const val RANGED_STRENGTH = 1
        const val BONUS_PRAYER = 2
        const val MAGIC_DAMAGE = 3

        /** CURSES  */
        @JvmStatic
        fun sendCurseBonuses(p: Player) {
            if (p.prayerbook == Prayerbook.CURSES) {
                sendAttackBonus(p)
                sendDefenceBonus(p)
                sendStrengthBonus(p)
                sendRangedBonus(p)
                sendMagicBonus(p)
            }
        }

        fun sendAttackBonus(p: Player) {
            val boost = p.leechedBonuses[0].toDouble()
            var bonus = 0
            if (p.curseActive[CurseHandler.LEECH_ATTACK]) {
                bonus = 5
            } else if (p.curseActive[CurseHandler.TURMOIL]) bonus = 15
            bonus += boost.toInt()
            if (bonus < -25) bonus = -25
            p.packetSender.sendString(690, "" + getColor(bonus) + "" + bonus + "%")
        }

        fun sendRangedBonus(p: Player) {
            val boost = p.leechedBonuses[4].toDouble()
            var bonus = 0
            if (p.curseActive[CurseHandler.LEECH_RANGED]) bonus = 5
            bonus += boost.toInt()
            if (bonus < -25) bonus = -25
            p.packetSender.sendString(693, "" + getColor(bonus) + "" + bonus + "%")
        }

        fun sendMagicBonus(p: Player) {
            val boost = p.leechedBonuses[6].toDouble()
            var bonus = 0
            if (p.curseActive[CurseHandler.LEECH_MAGIC]) bonus = 5
            bonus += boost.toInt()
            if (bonus < -25) bonus = -25
            p.packetSender.sendString(694, "" + getColor(bonus) + "" + bonus + "%")
        }

        fun sendDefenceBonus(p: Player) {
            val boost = p.leechedBonuses[1].toDouble()
            var bonus = 0
            if (p.curseActive[CurseHandler.LEECH_DEFENCE]) bonus =
                5 else if (p.curseActive[CurseHandler.TURMOIL]) bonus = 15
            bonus += boost.toInt()
            if (bonus < -25) bonus = -25
            p.packetSender.sendString(692, "" + getColor(bonus) + "" + bonus + "%")
        }

        fun sendStrengthBonus(p: Player) {
            val boost = p.leechedBonuses[2].toDouble()
            var bonus = 0
            if (p.curseActive[CurseHandler.LEECH_STRENGTH]) bonus =
                5 else if (p.curseActive[CurseHandler.TURMOIL]) bonus = 23
            bonus += boost.toInt()
            if (bonus < -25) bonus = -25
            p.packetSender.sendString(691, "" + getColor(bonus) + "" + bonus + "%")
        }

        fun getColor(i: Int): String {
            if (i > 0) return "@gre@+"
            return if (i < 0) "@red@" else ""
        }
    }
}