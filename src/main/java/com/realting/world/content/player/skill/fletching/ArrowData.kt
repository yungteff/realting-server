package com.realting.world.content.player.skill.fletching

enum class ArrowData(var item1: Int, var item2: Int, var outcome: Int, var xp: Int, var levelReq: Int) {
    HEADLESS(52, 314, 53, 1, 1), HEADLESS1(52, 10088, 53, 1, 1), HEADLESS2(52, 10090, 53, 1, 1), HEADLESS3(
        52, 10091, 53, 1, 1
    ),
    HEADLESS4(52, 10089, 53, 1, 1), HEADLESS5(52, 10087, 53, 1, 1), BRONZE(53, 39, 882, 2, 1), IRON(
        53, 40, 884, 3, 15
    ),
    STEEL(53, 41, 886, 5, 30), MITHRIL(53, 42, 888, 8, 45), ADAMANT(53, 43, 890, 10, 60), RUNE(
        53, 44, 892, 13, 75
    ),
    DRAGON(53, 11237, 11212, 15, 90);

    companion object {
        fun forArrow(id: Int): ArrowData? {
            for (ar in values()) {
                if (ar.item2 == id) {
                    return ar
                }
            }
            return null
        }
    }
}