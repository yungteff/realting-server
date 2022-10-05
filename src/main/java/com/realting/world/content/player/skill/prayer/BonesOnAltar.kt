package com.realting.world.content.player.skill.prayer

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Skill
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData
import com.realting.model.input.impl.EnterAmountOfBonesToSacrifice
import com.realting.model.Graphic
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player

object BonesOnAltar {
    @JvmStatic
    fun openInterface(player: Player, itemId: Int) {
        player.skillManager.stopSkilling()
        player.selectedSkillingItem = itemId
        player.inputHandling = EnterAmountOfBonesToSacrifice()
        player.packetSender.sendString(2799, ItemDefinition.forId(itemId).name).sendInterfaceModel(1746, itemId, 150)
            .sendChatboxInterface(4429)
        player.packetSender.sendString(2800, "How many would you like to offer?")
    }

    @JvmStatic
    fun offerBones(player: Player, amount: Int) {
        val boneId = player.selectedSkillingItem
        player.skillManager.stopSkilling()
        val currentBone: BonesData = BonesData.Companion.forId(boneId) ?: return
        player.packetSender.sendInterfaceRemoval()
        player.currentTask = object : Task(2, player, true) {
            var amountSacrificed = 0
            public override fun execute() {
                when {
                    amountSacrificed >= amount -> {
                        stop()
                        return
                    }
                    !player.inventory.contains(boneId) -> {
                        player.packetSender.sendMessage("You have run out of " + ItemDefinition.forId(boneId).name + ".")
                        stop()
                        return
                    }
                    player.interactingObject != null -> {
                        player.positionToFace = player.interactingObject.position.copy()
                        player.interactingObject.performGraphic(Graphic(624))
                    }
                }
                if (currentBone == BonesData.BIG_BONES) Achievements.finishAchievement(
                    player,
                    AchievementData.BURY_A_BIG_BONE
                ) else if (currentBone == BonesData.FROSTDRAGON_BONES) {
                    Achievements.doProgress(player, AchievementData.BURY_25_FROST_DRAGON_BONES)
                    Achievements.doProgress(player, AchievementData.BURY_500_FROST_DRAGON_BONES)
                }
                amountSacrificed++
                player.inventory.delete(boneId, 1)
                player.performAnimation(Animation(713))
                if (player.rights.isMember) {
                    player.skillManager.addExperience(Skill.PRAYER, (currentBone.buryingXP * 2.5).toInt())
                    return
                } else player.skillManager.addExperience(Skill.PRAYER, (currentBone.buryingXP * 2))
            }

            override fun stop() {
                setEventRunning(false)
                player.packetSender.sendMessage("You have pleased Crimson with your " + (if (amountSacrificed == 1) "sacrifice" else "sacrifices") + ".")
            }
        }
        TaskManager.submit(player.currentTask)
    }
}