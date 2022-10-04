package com.realting.world.content.skill.fletching

import com.realting.model.Animation

/**
 * Created by brandon on 4/19/2017.
 */
enum class GemData(
    val gem: Int, val outcome: Int, val output: Int, val xp: Int, val levelReq: Int, val animation: Animation
) {
    OPAL(1609, 45, 12, 2, 11, Animation(886)), PEARL(411, 46, 12, 3, 41, Animation(886)), SAPPHIRE(
        1607, 9189, 12, 4, 56, Animation(888)
    ),
    EMERALD(1605, 9190, 12, 6, 58, Animation(889)), RUBY(1603, 9191, 12, 7, 63, Animation(892)), DIAMOND(
        1601, 9192, 12, 8, 65, Animation(886)
    ),
    DRAGONSTONE(1615, 9193, 12, 9, 71, Animation(885)), ONYX(6573, 9194, 12, 10, 73, Animation(885));

    companion object {
        fun forGem(id: Int): GemData? {
            for (gem in values()) {
                if (gem.gem == id) {
                    return gem
                }
            }
            return null
        }
    }
}