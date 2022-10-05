package com.realting.world.content.player.events

import com.realting.model.container.impl.Equipment
import com.realting.model.entity.character.player.Player
import com.realting.world.content.ItemDegrading
import com.realting.world.content.ItemDegrading.DegradingItem

object BrawlingGloves {
    private val GLOVES_SKILLS = arrayOf(
        intArrayOf(13855, 13),
        intArrayOf(13848, 5),
        intArrayOf(13857, 7),
        intArrayOf(13856, 10),
        intArrayOf(13854, 17),
        intArrayOf(13853, 22),
        intArrayOf(13852, 14),
        intArrayOf(13851, 11),
        intArrayOf(13850, 8)
    )

    fun getExperienceIncrease(p: Player, skill: Int, experience: Int): Int {
        val playerGloves = p.equipment.items[Equipment.HANDS_SLOT].id
        if (playerGloves <= 0) return experience
        for (i in GLOVES_SKILLS.indices) {
            if (playerGloves == GLOVES_SKILLS[i][0] && skill == GLOVES_SKILLS[i][1] && ItemDegrading.handleItemDegrading(
                    p, DegradingItem.forNonDeg(playerGloves)
                )
            ) {
                return (experience * 1.25).toInt()
            }
        }
        return experience
    }
}