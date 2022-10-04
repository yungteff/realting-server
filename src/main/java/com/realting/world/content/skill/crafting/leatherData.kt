package com.realting.world.content.skill.crafting

enum class leatherData(
    private val buttonId: Array<IntArray>,
    val leather: Int,
    val product: Int,
    val level: Int,
    val xP: Double,
    val hideAmount: Int
) {
    LEATHER_GLOVES(
        arrayOf(intArrayOf(8638, 1), intArrayOf(8637, 5), intArrayOf(8636, 10)), 1741, 1059, 1, 13.8, 1
    ),
    LEATHER_BOOTS(
        arrayOf(intArrayOf(8641, 1), intArrayOf(8640, 5), intArrayOf(8639, 10)), 1741, 1061, 7, 16.25, 1
    ),
    LEATHER_COWL(
        arrayOf(intArrayOf(8653, 1), intArrayOf(8652, 5), intArrayOf(8651, 10)), 1741, 1167, 9, 18.5, 1
    ),
    LEATHER_VAMBRACES(
        arrayOf(intArrayOf(8644, 1), intArrayOf(8643, 5), intArrayOf(8642, 10)), 1741, 1063, 11, 22.0, 1
    ),
    LEATHER_BODY(
        arrayOf(intArrayOf(8635, 1), intArrayOf(8634, 5), intArrayOf(8633, 10)), 1741, 1129, 14, 25.0, 1
    ),
    LEATHER_CHAPS(
        arrayOf(intArrayOf(8647, 1), intArrayOf(8646, 5), intArrayOf(8645, 10)), 1741, 1095, 18, 27.0, 1
    ),
    HARD_LEATHER_BODY(
        arrayOf(intArrayOf(2799, 1), intArrayOf(2798, 5), intArrayOf(1747, 28)), 1743, 1131, 28, 35.0, 1
    ),
    COIF(
        arrayOf(intArrayOf(8650, 1), intArrayOf(8649, 5), intArrayOf(8648, 10)), 1741, 1169, 38, 37.0, 1
    ),
    SNAKESKIN_BOOTS(
        arrayOf(intArrayOf(8961, 1), intArrayOf(8960, 5), intArrayOf(8959, 10)), 6289, 6328, 45, 30.0, 6
    ),
    SNAKESKIN_VAMBRACES(
        arrayOf(intArrayOf(8965, 1), intArrayOf(8964, 5), intArrayOf(8963, 10)), 6289, 6330, 47, 35.0, 8
    ),
    SNAKESKIN_BANDANA(
        arrayOf(intArrayOf(8957, 1), intArrayOf(8956, 5), intArrayOf(8955, 10)), 6289, 6326, 48, 45.0, 5
    ),
    SNAKESKIN_CHAPS(
        arrayOf(intArrayOf(8953, 1), intArrayOf(8952, 5), intArrayOf(8951, 10)), 6289, 6324, 51, 50.0, 12
    ),
    SNAKESKIN_BODY(
        arrayOf(intArrayOf(8949, 1), intArrayOf(8948, 5), intArrayOf(8947, 10)), 6289, 6322, 53, 55.0, 15
    ),
    GREEN_DHIDE_VAMBRACES(
        arrayOf(intArrayOf(8889, 1), intArrayOf(8888, 5), intArrayOf(8887, 10)), 1745, 1065, 57, 62.0, 1
    ),
    GREEN_DHIDE_CHAPS(
        arrayOf(intArrayOf(8893, 1), intArrayOf(8892, 5), intArrayOf(8891, 10)), 1745, 1099, 60, 124.0, 2
    ),
    GREEN_DHIDE_BODY(
        arrayOf(intArrayOf(8897, 1), intArrayOf(8896, 5), intArrayOf(8895, 10)), 1745, 1135, 63, 186.0, 3
    ),
    BLUE_DHIDE_VAMBRACES(
        arrayOf(intArrayOf(8889, 1), intArrayOf(8888, 5), intArrayOf(8887, 10)), 2505, 2487, 66, 70.0, 1
    ),
    BLUE_DHIDE_CHAPS(
        arrayOf(intArrayOf(8893, 1), intArrayOf(8892, 5), intArrayOf(8891, 10)), 2505, 2493, 68, 140.0, 2
    ),
    BLUE_DHIDE_BODY(
        arrayOf(intArrayOf(8897, 1), intArrayOf(8896, 5), intArrayOf(8895, 10)), 2505, 2499, 71, 210.0, 3
    ),
    RED_DHIDE_VAMBRACES(
        arrayOf(intArrayOf(8889, 1), intArrayOf(8888, 5), intArrayOf(8887, 10)), 2507, 2489, 73, 78.0, 1
    ),
    RED_DHIDE_CHAPS(
        arrayOf(intArrayOf(8893, 1), intArrayOf(8892, 5), intArrayOf(8891, 10)), 2507, 2495, 75, 156.0, 2
    ),
    RED_DHIDE_BODY(
        arrayOf(intArrayOf(8897, 1), intArrayOf(8896, 5), intArrayOf(8895, 10)), 2507, 2501, 77, 258.0, 3
    ),
    BLACK_DHIDE_VAMBRACES(
        arrayOf(intArrayOf(8889, 1), intArrayOf(8888, 5), intArrayOf(8887, 10)), 2509, 2491, 79, 86.0, 1
    ),
    BLACK_DHIDE_CHAPS(
        arrayOf(intArrayOf(8893, 1), intArrayOf(8892, 5), intArrayOf(8891, 10)), 2509, 2497, 82, 172.0, 2
    ),
    BLACK_DHIDE_BODY(arrayOf(intArrayOf(8897, 1), intArrayOf(8896, 5), intArrayOf(8895, 10)), 2509, 2503, 84, 258.0, 3);

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
}