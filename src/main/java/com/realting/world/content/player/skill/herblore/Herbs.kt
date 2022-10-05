package com.realting.world.content.player.skill.herblore

enum class Herbs(val grimyHerb: Int, val cleanHerb: Int, val levelReq: Int, val exp: Int) {
    GUAM(199, 249, 1, 3), MARRENTILL(201, 251, 5, 4), TARROMIN(203, 253, 11, 5), HARRALANDER(205, 255, 20, 7), RANARR(
        207, 257, 25, 8
    ),
    TOADFLAX(3049, 2998, 30, 9), SPIRITWEED(12174, 12172, 35, 9), IRIT(209, 259, 40, 9), WERGALI(
        14836, 14854, 30, 9
    ),
    AVANTOE(211, 261, 48, 10), KWUARM(213, 263, 54, 11), SNAPDRAGON(3051, 3000, 59, 12), CADANTINE(
        215, 265, 65, 13
    ),
    LANTADYME(2485, 2481, 67, 14), DWARFWEED(217, 267, 70, 15), TORSTOL(219, 269, 75, 16);

    companion object {
        fun forId(herbId: Int): Herbs? {
            for (herb in values()) {
                if (herb.grimyHerb == herbId) {
                    return herb
                }
            }
            return null
        }
    }
}