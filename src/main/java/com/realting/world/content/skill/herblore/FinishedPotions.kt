package com.realting.world.content.skill.herblore

enum class FinishedPotions(
    val finishedPotion: Int, val unfinishedPotion: Int, val itemNeeded: Int, val levelReq: Int, val expGained: Int
) {
    MISSING_INGREDIENTS(-1, -1, -1, -1, -1), ATTACK_POTION(121, 91, 221, 1, 25), ANTIPOISON(
        175, 93, 235, 5, 38
    ),
    STRENGTH_POTION(115, 95, 225, 12, 50), RESTORE_POTION(127, 97, 223, 22, 63), ENERGY_POTION(
        3010, 97, 1975, 26, 68
    ),
    DEFENCE_POTION(133, 99, 239, 30, 75), AGILITY_POTION(3034, 3002, 2152, 34, 80), COMBAT_POTION(
        9741, 97, 9736, 36, 84
    ),
    PRAYER_POTION(139, 99, 231, 38, 88), SUMMONING_POTION(12142, 12181, 12109, 40, 92), CRAFTING_POTION(
        14840, 14856, 5004, 42, 92
    ),
    SUPER_ATTACK(145, 101, 221, 45, 100), VIAL_OF_STENCH(18661, 101, 1871, 46, 0), FISHING_POTION(
        151, 103, 231, 50, 113
    ),
    SUPER_ENERGY(3018, 103, 2970, 52, 118), SUPER_STRENGTH(157, 105, 225, 55, 125), WEAPON_POISON(
        187, 105, 241, 60, 138
    ),
    SUPER_RESTORE(3026, 3004, 223, 63, 143), SUPER_DEFENCE(163, 107, 239, 66, 150), ANTIFIRE(
        2454, 2483, 241, 69, 158
    ),
    RANGING_POTION(169, 109, 245, 72, 163), MAGIC_POTION(3042, 2483, 3138, 76, 173), ZAMORAK_BREW(
        189, 111, 247, 78, 175
    ),
    SARADOMIN_BREW(6687, 3002, 6693, 81, 180), RECOVER_SPECIAL(15301, 3018, 5972, 84, 200), SUPER_ANTIFIRE(
        15305, 2454, 4621, 85, 210
    ),
    SUPER_PRAYER(15329, 139, 4255, 94, 270), SUPER_ANTIPOISON(181, 101, 235, 48, 103), HUNTER_POTION(
        10000, 103, 10111, 53, 110
    ),
    FLETCHING_POTION(14848, 103, 11525, 58, 105), ANTIPOISON_PLUS(5945, 3002, 6049, 68, 154);

    companion object {
        fun forId(id: Int, id2: Int): FinishedPotions? {
            var hasOnePart = false
            for (pot in values()) {
                if (pot.unfinishedPotion == id || pot.unfinishedPotion == id2) {
                    hasOnePart = true
                }
                if ((pot.itemNeeded == id || pot.itemNeeded == id2) && (pot.unfinishedPotion == id || pot.unfinishedPotion == id2)) {
                    return pot
                }
            }
            return if (hasOnePart) MISSING_INGREDIENTS else null
        }
    }
}