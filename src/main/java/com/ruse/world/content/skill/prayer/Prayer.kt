package com.ruse.world.content.skill.prayer

import com.ruse.engine.task.Task
import com.ruse.engine.task.TaskManager
import com.ruse.model.Animation
import com.ruse.model.Skill
import com.ruse.world.content.Achievements
import com.ruse.world.content.Achievements.AchievementData
import com.ruse.model.Item
import com.ruse.model.entity.character.player.Player
import com.ruse.world.content.Sounds

/**
 * The prayer skill is based upon burying the corpses of enemies. Obtaining a higher level means
 * more prayer abilities being unlocked, which help out in combat.
 *
 * @author Gabriel Hannason
 */
object Prayer {
    @JvmStatic
    fun isBone(bone: Int): Boolean {
        return BonesData.forId(bone) != null
    }

    @JvmStatic
    fun buryBone(player: Player, itemId: Int) {
        if (!player.clickDelay.elapsed(2000)) return
        val currentBone = BonesData.forId(itemId) ?: return
        player.skillManager.stopSkilling()
        player.packetSender.sendInterfaceRemoval()
        player.performAnimation(Animation(827))
        player.packetSender.sendMessage("You dig a hole in the ground..")
        val bone = Item(itemId)
        player.inventory.delete(bone)
        TaskManager.submit(object : Task(3, player, false) {
            public override fun execute() {
                player.packetSender.sendMessage("..and bury the " + bone.definition.name + ".")
                player.skillManager.addExperience(Skill.PRAYER, currentBone.buryingXP)
                Sounds.sendSound(player, Sounds.Sound.BURY_BONE)
                if (currentBone == BonesData.BIG_BONES) Achievements.finishAchievement(
                    player,
                    AchievementData.BURY_A_BIG_BONE
                )
                if (currentBone == BonesData.DRAGON_BONES) Achievements.finishAchievement(
                    player,
                    AchievementData.BURY_A_DRAGON_BONE
                ) else if (currentBone == BonesData.FROSTDRAGON_BONES) {
                    Achievements.doProgress(player, AchievementData.BURY_25_FROST_DRAGON_BONES)
                    Achievements.doProgress(player, AchievementData.BURY_500_FROST_DRAGON_BONES)
                }
                stop()
            }
        })
        player.clickDelay.reset()
    }
}