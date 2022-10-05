package com.realting.world.content.player.skill.herblore

enum class UnfinishedPotions(val unfPotion: Int, val herbNeeded: Int, val levelReq: Int) {
    GUAM_POTION(91, 249, 1), MARRENTILL_POTION(93, 251, 5), TARROMIN_POTION(95, 253, 12), HARRALANDER_POTION(
        97, 255, 22
    ),
    RANARR_POTION(99, 257, 30), TOADFLAX_POTION(3002, 2998, 34), SPIRIT_WEED_POTION(12181, 12172, 40), IRIT_POTION(
        101, 259, 45
    ),
    WERGALI_POTION(14856, 14854, 1), AVANTOE_POTION(103, 261, 50), KWUARM_POTION(105, 263, 55), SNAPDRAGON_POTION(
        3004, 3000, 63
    ),
    CADANTINE_POTION(107, 265, 66), LANTADYME(2483, 2481, 69), DWARF_WEED_POTION(109, 267, 72), TORSTOL_POTION(
        111, 269, 78
    );

    companion object {
        fun forId(herbId: Int): UnfinishedPotions? {
            for (unf in values()) {
                if (unf.herbNeeded == herbId) {
                    return unf
                }
            }
            return null
        }

        fun forUnfPot(unfId: Int): UnfinishedPotions? {
            for (unf in values()) {
                if (unf.unfPotion == unfId) {
                    return unf
                }
            }
            return null
        }
    }
}