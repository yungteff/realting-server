package com.realting.world.content.skill.fletching

enum class StringingData(
    private val unstrung: Int, private val strung: Int, val level: Int, val xP: Int, val animation: Int
) {
    SHORT_BOW(50, 841, 5, 5, 6678), LONG_BOW(48, 839, 10, 10, 6684), OAK_SHORT_BOW(54, 843, 20, 17, 6679), OAK_LONG_BOW(
        56, 845, 25, 25, 6685
    ),
    WILLOW_SHORT_BOW(60, 849, 35, 34, 6680), WILLOW_LONG_BOW(58, 847, 40, 42, 6686), MAPLE_SHORT_BOW(
        64, 853, 50, 50, 6681
    ),
    MAPLE_LONG_BOW(62, 851, 55, 59, 6687), YEW_SHORT_BOW(68, 857, 65, 68, 6682), YEW_LONG_BOW(
        66, 855, 70, 75, 6688
    ),
    MAGIC_SHORT_BOW(72, 861, 80, 84, 6683), MAGIC_LONG_BOW(70, 859, 85, 92, 6689);

    fun unStrung(): Int {
        return unstrung
    }

    fun Strung(): Int {
        return strung
    }
}