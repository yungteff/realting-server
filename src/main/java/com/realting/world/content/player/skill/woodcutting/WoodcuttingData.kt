package com.realting.world.content.player.skill.woodcutting

import com.realting.model.Skill
import com.realting.model.container.impl.Equipment
import com.realting.model.entity.character.player.Player

object WoodcuttingData {
    @JvmStatic
    fun getHatchet(p: Player): Int {
        for (h in Hatchet.values()) {
            if (p.equipment.items[Equipment.WEAPON_SLOT].id == h.id) {
                return h.id
            } else if (p.inventory.contains(h.id)) {
                return h.id
            }
        }
        return -1
    }

    @JvmStatic
    fun getChopTimer(player: Player, h: Hatchet): Int {
        val skillReducement = (player.skillManager.getMaxLevel(Skill.WOODCUTTING) * 0.05).toInt()
        val axeReducement = h.speed.toInt()
        return skillReducement + axeReducement
    }

    enum class Hatchet(val id: Int, val requiredLevel: Int, val anim: Int, val speed: Double) {
        BRONZE(1351, 1, 879, 1.0), IRON(1349, 1, 877, 1.3), STEEL(1353, 6, 875, 1.5), BLACK(1361, 6, 873, 1.7), MITHRIL(
            1355, 21, 871, 1.9
        ),
        ADAMANT(1357, 31, 869, 2.0), RUNE(1359, 41, 867, 2.2), ADZE(13661, 61, 10227, 2.33),  //old anim = 10227
        DRAGON(6739, 61, 2846, 2.5), SACRED(14140, 61, 406, 2.5);

        companion object {
            var hatchets: MutableMap<Int, Hatchet> = HashMap()

            @JvmStatic
            fun forId(id: Int): Hatchet? {
                return hatchets[id]
            }

            init {
                for (hatchet in values()) {
                    hatchets[hatchet.id] = hatchet
                }
            }
        }
    }

    enum class Trees(
        val req: Int, val xp: Int, val reward: Int, private val objects: IntArray, val ticks: Int, val isMulti: Boolean
    ) {
        NORMAL(
            1, 25, 1511, intArrayOf(
                1276,
                1277,
                1278,
                1279,
                1280,
                1282,
                1283,
                1284,
                1285,
                1286,
                1289,
                1290,
                1291,
                1315,
                1316,
                1318,
                1319,
                1330,
                1331,
                1332,
                1365,
                1383,
                1384,
                3033,
                3034,
                3035,
                3036,
                3881,
                3882,
                3883,
                5902,
                5903,
                5904
            ), 4, false
        ),
        ACHEY(1, 25, 2862, intArrayOf(2023), 4, false), OAK(15, 38, 1521, intArrayOf(1281, 3037), 5, true), WILLOW(
            30, 68, 1519, intArrayOf(1308, 5551, 5552, 5553), 6, true
        ),
        TEAK(35, 85, 6333, intArrayOf(9036), 7, true), DRAMEN(36, 25, 771, intArrayOf(1292), 7, true), MAPLE(
            45, 100, 1517, intArrayOf(1307, 4677, 4674), 7, true
        ),
        MAHOGANY(50, 125, 22060, intArrayOf(9034), 7, true), YEW(60, 175, 1515, intArrayOf(1309), 14, true), MAGIC(
            75, 250, 1513, intArrayOf(1306), 20, true
        );

        companion object {
            private val tree: MutableMap<Int, Trees> = HashMap()

            @JvmStatic
            fun forId(id: Int): Trees? {
                return tree[id]
            }

            init {
                for (t in values()) {
                    for (obj in t.objects) {
                        tree[obj] = t
                    }
                }
            }
        }
    }
}