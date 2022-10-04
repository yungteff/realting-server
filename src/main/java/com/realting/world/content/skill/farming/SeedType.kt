package com.realting.world.content.skill.farming

enum class SeedType {
    HERB, ALLOTMENT, FLOWER;

    companion object {
        fun forId(id: Int): SeedType {
            for (type in values()) {
                if (type.ordinal == id) return type
            }
            return HERB
        }
    }
}