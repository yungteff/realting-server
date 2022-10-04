package com.realting.world.content.skill.crafting

enum class tanningData(
    private val buttonId: Array<IntArray>,
    val hideId: Int,
    val leatherId: Int,
    val price: Int,
    val frameId: IntArray,
    val leatherName: String
) {
    SOFT_LEATHER(
        arrayOf(intArrayOf(14817, 1), intArrayOf(14809, 5), intArrayOf(14801, 29), intArrayOf(14793, 28)),
        1739,
        1741,
        1,
        intArrayOf(14777, 14785, 14769),
        "Soft leather"
    ),
    HARD_LEATHER(
        arrayOf(intArrayOf(14818, 1), intArrayOf(14810, 5), intArrayOf(14802, 29), intArrayOf(14794, 28)),
        1739,
        1743,
        3,
        intArrayOf(14778, 14786, 14770),
        "Hard leather"
    ),
    SNAKESKIN(
        arrayOf(intArrayOf(14819, 1), intArrayOf(14811, 5), intArrayOf(14803, 29), intArrayOf(14795, 28)),
        6287,
        6289,
        15,
        intArrayOf(14779, 14787, 14771),
        "Snakeskin"
    ),
    SNAKESKIN2(
        arrayOf(intArrayOf(14820, 1), intArrayOf(14812, 5), intArrayOf(14804, 29), intArrayOf(14796, 28)),
        6287,
        6289,
        20,
        intArrayOf(14780, 14788, 14772),
        "Snakeskin"
    ),
    GREEN_DRAGON_LEATHER(
        arrayOf(
            intArrayOf(14821, 1),
            intArrayOf(14813, 5),
            intArrayOf(14805, 29),
            intArrayOf(14797, 28)
        ), 1753, 1745, 20, intArrayOf(14781, 14789, 14773), "Green d'hide"
    ),
    BLUE_DRAGON_LEATHER(
        arrayOf(
            intArrayOf(14822, 1),
            intArrayOf(14814, 5),
            intArrayOf(14806, 29),
            intArrayOf(14798, 28)
        ), 1751, 2505, 20, intArrayOf(14782, 14790, 14774), "Blue d'hide"
    ),
    RED_DRAGON_LEATHER(
        arrayOf(
            intArrayOf(14823, 1),
            intArrayOf(14815, 5),
            intArrayOf(14807, 29),
            intArrayOf(14799, 28)
        ), 1749, 2507, 20, intArrayOf(14783, 14791, 14775), "Red d'hide"
    ),
    BLACK_DRAGON_LEATHER(
        arrayOf(
            intArrayOf(14824, 1),
            intArrayOf(14816, 5),
            intArrayOf(14808, 29),
            intArrayOf(14800, 28)
        ), 1747, 2509, 20, intArrayOf(14784, 14792, 14776), "Black d'hide"
    );

    fun getButtonId(button: Int): Int {
        for (i in buttonId.indices) {
            if (button == buttonId[i][0]) {
                return buttonId[i][0]
            }
        }
        return -1
    }

    fun getAmount(button: Int): Int {
        for (i in buttonId.indices) {
            if (button == buttonId[i][0]) {
                return buttonId[i][1]
            }
        }
        return -1
    }

    val nameFrame: Int
        get() = frameId[0]
    val costFrame: Int
        get() = frameId[1]
    val itemFrame: Int
        get() = frameId[2]
}