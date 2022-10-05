package com.realting.world.content.player.skill.cooking

import com.realting.engine.task.Task
import com.realting.model.input.impl.EnterAmountToCook
import com.realting.model.Skill
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player

object Cooking {
    @JvmStatic
    fun selectionInterface(player: Player, cookingData: CookingData?) {
        if (cookingData == null) return
        player.selectedSkillingItem = cookingData.rawItem
        player.inputHandling = EnterAmountToCook()
        player.packetSender.sendString(2799, ItemDefinition.forId(cookingData.cookedItem).name)
            .sendInterfaceModel(1746, cookingData.cookedItem, 150).sendChatboxInterface(4429)
        player.packetSender.sendString(2800, "How many would you like to cook?")
    }

    @JvmStatic
    fun cook(player: Player, rawFish: Int, amount: Int) {
        val fish: CookingData = CookingData.forFish(rawFish) ?: return
        player.skillManager.stopSkilling()
        player.packetSender.sendInterfaceRemoval()
        if (!CookingData.Companion.canCook(player, rawFish)) return
        player.performAnimation(Animation(883))
        player.currentTask = object : Task(2, player, false) {
            var amountCooked = 0
            public override fun execute() {
                if (!CookingData.Companion.canCook(player, rawFish)) {
                    stop()
                    return
                }
                player.performAnimation(Animation(883))
                player.inventory.delete(rawFish, 1)
                if (!CookingData.success(player, 3, fish.levelReq, fish.stopBurn)) {
                    player.inventory.add(fish.burntItem, 1)
                    player.packetSender.sendMessage("You accidently burn the " + fish.foodName + ".")
                } else {
                    player.inventory.add(fish.cookedItem, 1)
                    player.skillManager.addExperience(Skill.COOKING, fish.xp)
                    if (fish == CookingData.SALMON) {
                        //Achievements.finishAchievement(player, AchievementData.COOK_A_SALMON);
                    } else if (fish == CookingData.ROCKTAIL) {
                        Achievements.doProgress(player, AchievementData.COOK_25_ROCKTAILS)
                        Achievements.doProgress(player, AchievementData.COOK_1000_ROCKTAILS)
                    }
                }
                amountCooked++
                if (amountCooked >= amount) stop()
            }

            override fun stop() {
                setEventRunning(false)
                player.selectedSkillingItem = -1
                player.performAnimation(Animation(65535))
            }
        }
        TaskManager.submit(player.currentTask)
    }
}