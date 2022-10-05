package com.realting.world.content.player.skill.smithing

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Skill
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData
import com.realting.world.content.ItemDegrading
import com.realting.world.content.ItemDegrading.DegradingItem
import com.realting.world.content.player.skill.mining.MiningData.Ores
import com.realting.model.Position
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.Sounds

object Smelting {
    @JvmStatic
    fun openInterface(player: Player) {
        player.skillManager.stopSkilling()
        for (j in SmithingData.SMELT_BARS.indices) player.packetSender.sendInterfaceModel(
            SmithingData.SMELT_FRAME[j],
            SmithingData.SMELT_BARS[j],
            150
        )
        player.packetSender.sendChatboxInterface(2400)
    }

    @JvmStatic
    fun smeltBar(player: Player, barId: Int, amount: Int) {
        player.skillManager.stopSkilling()
        player.packetSender.sendInterfaceRemoval()
        if (!SmithingData.canSmelt(player, barId)) return
        player.performAnimation(Animation(896))
        player.currentTask = object : Task(3, player, true) {
            var amountMade = 0
            public override fun execute() {
                if (!SmithingData.canSmelt(player, barId)) {
                    stop()
                    return
                }
                player.positionToFace = Position(3078, 9495, 0)
                player.performAnimation(Animation(896))
                handleBarCreation(barId, player)
                amountMade++
                if (amountMade >= amount) stop()
            }
        }
        TaskManager.submit(player.currentTask)
    }

    @JvmStatic
    fun handleBarCreation(barId: Int, player: Player) {
        if (player.ores[0] > 0) {
            player.inventory.delete(player.ores[0], 1)
            if (player.ores[1] > 0 && player.ores[1] != 453) {
                player.inventory.delete(player.ores[1], 1)
            } else if (player.ores[1] == 453) {
                if (player.skillManager.skillCape(Skill.SMITHING) && Misc.getRandom(3) == 1) {
                    player.packetSender.sendMessage("Your cape saves you some coal!")
                } else {
                    player.inventory.delete(player.ores[1], SmithingData.getCoalAmount(barId))
                }
            }
            if (barId != 2351) { //Iron bar - 50% successrate
                player.inventory.add(barId, 1)
                player.skillManager.addExperience(Skill.SMITHING, getExperience(barId))
                if (barId == 2363) {
                    Achievements.doProgress(player, AchievementData.SMELT_25_RUNE_BARS)
                    Achievements.doProgress(player, AchievementData.SMELT_1000_RUNE_BARS)
                }
            } else if (SmithingData.ironOreSuccess(player) || player.equipment.contains(2568)) { //ring of foraging
                if (player.equipment.contains(2568)) {
                    ItemDegrading.handleItemDegrading(player, DegradingItem.RING_OF_FORGING)
                }
                Achievements.finishAchievement(player, AchievementData.SMELT_AN_IRON_BAR)
                player.inventory.add(barId, 1)
                player.skillManager.addExperience(Skill.SMITHING, getExperience(barId))
            } else player.packetSender.sendMessage("The Iron ore burns too quickly and you're unable to make an Iron bar.")
            Sounds.sendSound(player, Sounds.Sound.SMELT_ITEM)
        }
    }

    @JvmStatic
    fun getExperience(barId: Int): Int {
        when (barId) {
            2349 -> return 7
            2351 -> return 13
            2353 -> return 18
            2355 -> return 14
            2357 -> return 23
            2359 -> return 30
            2361 -> return 38
            2363 -> return 50
        }
        return 0
    }

    fun getBar(o: Ores?): Int {
        return 0
    }
}