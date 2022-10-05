package com.realting.world.content.player.skill.fletching

import java.util.Locale

enum class BowData(var logID: Int, var bowID: Int, var xp: Int, var levelReq: Int, var fullBowId: Int) {
    //Log Id, Unstrung Bow Id, Xp, Level Req, PRODUCTID
    SHORTBOW(1511, 50, 5, 5, 841), LONGBOW(1511, 48, 10, 10, 839), OAK_SHORTBOW(
        1521, 54, 17, 20, 843
    ),
    OAK_LONGBOW(1521, 56, 25, 25, 845), WILLOW_SHORTBOW(1519, 60, 34, 35, 849), WILLOW_LONGBOW(
        1519, 58, 42, 40, 847
    ),
    MAPLE_SHORTBOW(1517, 64, 50, 50, 853), MAPLE_LONGBOW(1517, 62, 59, 55, 851), YEW_SHORTBOW(
        1515, 68, 68, 65, 857
    ),
    YEW_LONGBOW(1515, 66, 75, 70, 855), MAGIC_SHORTBOW(1513, 72, 84, 80, 861), MAGIC_LONGBOW(1513, 70, 92, 85, 859);

    companion object {
        fun forBow(id: Int): BowData? {
            for (fl in values()) {
                if (fl.bowID == id) {
                    return fl
                }
            }
            return null
        }

        fun forLog(log: Int): BowData? {
            for (fl in values()) {
                if (fl.logID == log) {
                    return fl
                }
            }
            return null
        }

        @JvmStatic
        fun forLog(log: Int, shortbow: Boolean): BowData? {
            for (fl in values()) {
                if (fl.logID == log) {
                    if (shortbow && fl.toString().lowercase(Locale.getDefault())
                            .contains("shortbow") || !shortbow && fl.toString().lowercase(Locale.getDefault())
                            .contains("longbow")
                    ) {
                        return fl
                    }
                }
            }
            return null
        }

        fun forId(id: Int): BowData? {
            for (fl in values()) {
                if (fl.ordinal == id) {
                    return fl
                }
            }
            return null
        }
    }
}