package com.realting.world.content.skill.fletching


/**
 * Created by brandon on 4/19/2017.
 */
enum class BoltData(val bolt: Int, val tip: Int, val outcome: Int, val xp: Int, val levelReq: Int, val amount: Int) {
    // OPAL(877, 45, 879, 2, 11, 10),
    // PEARL(411, 46, 3, 6, 41, 10),
    SAPPHIRE(9142, 9189, 9337, 4, 56, 10), EMERALD(9142, 9190, 9338, 6, 58, 10), RUBY(
        9143, 9191, 9339, 7, 63, 10
    ),
    DIAMOND(9143, 9192, 9340, 8, 65, 10), DRAGONSTONE(9144, 9193, 9341, 9, 71, 10), ONYX(9144, 9194, 9342, 10, 73, 10);

    companion object {
        fun forBolts(id: Int): BoltData? {
            for (bolt in values()) {
                if (bolt.bolt == id) {
                    return bolt
                }
            }
            return null
        }

        fun forTip(id: Int): BoltData? {
            for (bolt in values()) {
                if (bolt.tip == id) {
                    return bolt
                }
            }
            return null
        }
    }
}