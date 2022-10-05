package com.realting.world.content.player.skill.magic

import com.realting.GameSettings
import com.realting.model.*
import com.realting.model.entity.character.player.Player
import java.util.HashMap

object Magic {
    private fun hasRunes(player: Player, spellID: Int): Boolean {
        if (player.equipment.contains(17293) || player.equipment.contains(15835)) {
            return true
        }
        val ens = EnchantSpell.forId(spellID)
        return if (ens!!.req3 == 0) {
            player.inventory.contains(ens.req1) && player.inventory.getAmount(ens.req1) >= ens.reqAmt1 && player.inventory.contains(
                ens.req2
            ) && player.inventory.getAmount(ens.req2) >= ens.reqAmt2 && player.inventory.contains(ens.req3) && player.inventory.getAmount(
                ens.req3
            ) >= ens.reqAmt3
        } else {
            player.inventory.contains(ens.req1) && player.inventory.getAmount(ens.req1) >= ens.reqAmt1 && player.inventory.contains(
                ens.req2
            ) && player.inventory.getAmount(ens.req2) >= ens.reqAmt2
        }
    }

    private fun getEnchantmentLevel(spellID: Int): Int {
        when (spellID) {
            1155 -> return 1
            1165 -> return 2
            1176 -> return 3
            1180 -> return 4
            1187 -> return 5
            6003 -> return 6
        }
        return 0
    }

    @JvmStatic
    fun enchantItem(player: Player, itemID: Int, spellID: Int) {
        val enc = Enchant.forId(itemID)
        val ens = EnchantSpell.forId(spellID)
        if (enc == null || ens == null) {
            return
        }
        if (player.skillManager.getCurrentLevel(Skill.MAGIC) >= enc.levelReq) {
            if (player.inventory.contains(enc.unenchanted)) {
                var toMake = enc.amount
                val materials = player.inventory.getAmount(enc.unenchanted)
                if (materials < toMake) {
                    toMake = materials
                }
                if (hasRunes(player, spellID)) {
                    if (getEnchantmentLevel(spellID) == enc.eLevel) {
                        player.inventory.delete(enc.unenchanted, toMake)
                        player.inventory.add(enc.enchanted, toMake)
                        player.skillManager.addExperience(Skill.MAGIC, enc.xp)
                        player.inventory.delete(ens.req1, ens.reqAmt1)
                        player.inventory.delete(ens.req2, ens.reqAmt2)
                        player.performAnimation(Animation(enc.anim))
                        player.performGraphic(Graphic(enc.gFX, GraphicHeight.HIGH))
                        if (ens.req3 != -1) {
                            player.inventory.delete(ens.req3, ens.reqAmt3)
                        }
                        player.packetSender.sendTab(GameSettings.MAGIC_TAB)
                    } else {
                        player.packetSender.sendMessage("You can only enchant this jewelry using a level-" + enc.eLevel + " enchantment spell!")
                    }
                } else {
                    player.packetSender.sendMessage("You do not have enough runes to cast this spell.")
                }
            }
        } else {
            player.packetSender.sendMessage("You need a Magic level of at least " + enc.levelReq + " to cast this spell.")
        }
    }

    enum class Enchant(
        var unenchanted: Int,
        var enchanted: Int,
        var levelReq: Int,
        var xp: Int,
        var anim: Int,
        var gFX: Int,
        var eLevel: Int,
        var amount: Int
    ) {
        SAPPHIRERING(1637, 2550, 7, 18, 719, 114, 1, 1), SAPPHIREAMULET(
            1694, 1727, 7, 18, 719, 114, 1, 1
        ),
        SAPPHIRENECKLACE(1656, 3853, 7, 18, 719, 114, 1, 1), SAPPHIREBOLTS(
            9337, 9240, 7, 18, 712, 238, 1, 10
        ),
        EMERALDRING(1639, 2552, 27, 37, 719, 114, 2, 1), EMERALDAMULET(
            1696, 1729, 27, 37, 719, 114, 2, 1
        ),
        EMERALDNECKLACE(1658, 5521, 27, 37, 719, 114, 2, 1), EMERALDBOLTS(
            9338, 9241, 27, 37, 712, 238, 2, 10
        ),
        RUBYRING(1641, 2568, 47, 59, 720, 115, 3, 1), RUBYAMULET(1698, 1725, 47, 59, 720, 115, 3, 1), RUBYNECKLACE(
            1660, 11194, 47, 59, 720, 115, 3, 1
        ),
        RUBYBOLTS(9339, 9242, 27, 59, 712, 238, 3, 10), DIAMONDRING(1643, 2570, 57, 67, 720, 115, 4, 1), DIAMONDAMULET(
            1700, 1731, 57, 67, 720, 115, 4, 1
        ),
        DIAMONDNECKLACE(1662, 11090, 57, 67, 720, 115, 4, 1), DIAMONDBOLTS(
            9340, 9243, 27, 67, 712, 238, 4, 10
        ),
        DRAGONSTONERING(1645, 22045, 68, 78, 721, 116, 5, 1), DRAGONSTONEAMULET(
            1702, 1712, 68, 78, 721, 116, 5, 1
        ),
        DRAGONSTONENECKLACE(1664, 11113, 68, 78, 721, 116, 5, 1), DRAGONBOLTS(
            9341, 9244, 27, 78, 712, 238, 5, 10
        ),
        ONYXRING(6575, 6583, 87, 97, 721, 452, 6, 1), ONYXAMULET(6581, 6585, 87, 97, 721, 452, 6, 1), ONYXNECKLACE(
            6577, 11128, 87, 97, 721, 452, 6, 1
        ),
        ONYXBOLTS(9342, 9245, 27, 97, 712, 238, 6, 10), ZENYTE_NECKLACE(
            Items.ZENYTE_NECKLACE, Items.NECKLACE_OF_ANGUISH, 87, 97, 721, 452, 6, 1
        ),
        ZENYTE_RING(Items.ZENYTE_RING, Items.RING_OF_SUFFERING, 87, 97, 721, 452, 6, 1), ZENYTE_BRACELET(
            Items.ZENYTE_BRACELET, Items.TORMENTED_BRACELET, 87, 97, 721, 452, 6, 1
        ),
        ZENYTE_AMULET(Items.ZENYTE_AMULET, Items.AMULET_OF_TORTURE, 87, 97, 721, 452, 6, 1);

        companion object {
            private val enc: MutableMap<Int, Enchant> = HashMap()
            fun forId(itemID: Int): Enchant? {
                return enc[itemID]
            }

            init {
                for (en in values()) {
                    enc[en.unenchanted] = en
                }
            }
        }
    }

    private enum class EnchantSpell(
        var spell: Int,
        reqRune1: Int,
        reqAmtRune1: Int,
        reqRune2: Int,
        reqAmtRune2: Int,
        reqRune3: Int,
        reqAmtRune3: Int
    ) {
        SAPPHIRE(1155, 555, 1, 564, 1, -1, 0), EMERALD(1165, 556, 3, 564, 1, -1, 0), RUBY(
            1176, 554, 5, 564, 1, -1, 0
        ),
        DIAMOND(1180, 557, 10, 564, 1, -1, 0), DRAGONSTONE(1187, 555, 15, 557, 15, 564, 1), ONYX(
            6003, 557, 20, 554, 20, 564, 1
        );

        var req1 = reqRune1
        var reqAmt1 = reqAmtRune1
        var req2 = reqRune2
        var reqAmt2 = reqAmtRune2
        var req3 = reqRune3
        var reqAmt3 = reqAmtRune3

        init {
            req1 = reqRune1
            reqAmt1 = reqAmtRune1
            req2 = reqRune2
            reqAmt2 = reqAmtRune2
            req3 = reqRune3
            reqAmt3 = reqAmtRune3
        }

        companion object {
            val ens: MutableMap<Int, EnchantSpell> = HashMap()
            fun forId(id: Int): EnchantSpell? {
                return ens[id]
            }

            init {
                for (en in values()) {
                    ens[en.spell] = en
                }
            }
        }
    }
}