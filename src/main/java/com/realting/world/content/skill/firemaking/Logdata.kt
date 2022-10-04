package com.realting.world.content.skill.firemaking

import com.realting.model.entity.character.player.Player

object Logdata {
    @JvmStatic
    fun getLogData(p: Player, log: Int): logData? {
        for (l in logData.values()) {
            if (log == l.logId || log == -1 && p.inventory.contains(l.logId)) {
                return l
            }
        }
        return null
    }

    enum class logData(val logId: Int, val level: Int, val xp: Int, val burnTime: Int, val gameObject: Int) {
        LOG(1511, 1, 40, 30, 2732), LOG_RED(7404, 1, 50, 30, 11404), LOG_BLUE(7406, 1, 50, 30, 11406), LOG_GREEN(
            7405, 1, 50, 30, 11405
        ),
        LOG_PURPLE(10329, 1, 50, 30, 20001), LOG_WHITE(10328, 1, 50, 30, 20000), ACHEY(2862, 1, 40, 30, 2732), OAK(
            1521, 15, 60, 40, 2732
        ),
        WILLOW(1519, 30, 90, 45, 2732), TEAK(6333, 35, 105, 45, 2732), ARCTIC_PINE(
            10810, 42, 125, 45, 2732
        ),
        MAPLE(1517, 45, 143, 45, 2732), MAHOGANY(22060, 50, 158, 45, 2732), EUCALYPTUS(12581, 58, 194, 45, 2732), YEW(
            1515, 60, 203, 50, 2732
        ),
        MAGIC(1513, 75, 304, 50, 2732);

    }
}