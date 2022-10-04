package com.ruse.world.content.skill.runecrafting

import com.ruse.model.*
import com.ruse.model.entity.character.player.Player
import com.ruse.util.Misc
import com.ruse.world.content.skill.runecrafting.RunecraftingData.RuneData
import com.ruse.world.content.Achievements
import com.ruse.world.content.Achievements.AchievementData
import com.ruse.world.content.skill.runecrafting.RunecraftingData.TalismanData
import com.ruse.world.content.transportation.TeleportHandler

/**
 * Handles the Runecrafting skill
 * @author Gabriel Hannason
 */
object Runecrafting {
    private var saved = 0
    @JvmStatic
    fun craftRunes(player: Player, rune: RuneData) {
        if (!canRuneCraft(player, rune)) return
        var essence = -1
        if (player.inventory.contains(1436) && !rune.pureRequired()) essence = 1436
        if (player.inventory.contains(7936) && essence < 0) essence = 7936
        if (essence == -1) return
        player.performGraphic(Graphic(186))
        player.performAnimation(Animation(791))
        val amountToMake = RunecraftingData.getMakeAmount(rune, player)
        var amountMade = 0
        var i = 28
        while (i > 0) {
            if (!player.inventory.contains(essence)) break
            if (player.skillManager.skillCape(Skill.RUNECRAFTING) && Misc.getRandom(4) == 1) {
                saved++
                i++
            } else {
                player.inventory.delete(essence, 1)
            }
            player.inventory.add(rune.runeID, amountToMake)
            amountMade += amountToMake
            player.skillManager.addExperience(Skill.RUNECRAFTING, rune.xP)
            i--
        }
        if (rune == RuneData.EARTH_RUNE) {
            Achievements.doProgress(player, AchievementData.CRAFT_100_EARTH_RUNES, amountMade)
        }
        if (rune == RuneData.BLOOD_RUNE) {
            Achievements.doProgress(player, AchievementData.RUNECRAFT_500_BLOOD_RUNES, amountMade)
            Achievements.doProgress(player, AchievementData.RUNECRAFT_8000_BLOOD_RUNES, amountMade)
        }
        player.performGraphic(Graphic(129))
        player.skillManager.addExperience(Skill.RUNECRAFTING, rune.xP)
        player.packetSender.sendMessage("You bind the altar's power into " + rune.name + "s..")
        if (player.skillManager.skillCape(Skill.RUNECRAFTING) && saved > 0) {
            player.packetSender.sendMessage("Your cape has recycled " + saved + " essence into " + amountToMake * saved + " runes.")
            saved = 0
        }
        Achievements.finishAchievement(player, AchievementData.RUNECRAFT_SOME_RUNES)
        player.clickDelay.reset()
    }

    @JvmStatic
    fun handleTalisman(player: Player, ID: Int) {
        val talisman: TalismanData = TalismanData.Companion.forId(ID) ?: return
        if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) < talisman.levelRequirement) {
            player.packetSender.sendMessage("You need a Runecrafting level of at least " + talisman.levelRequirement + " to use this Talisman's teleport function.")
            return
        }
        val targetLocation = talisman.getLocation()
        TeleportHandler.teleportPlayer(player, targetLocation, player.spellbook.teleportType)
    }

    fun canRuneCraft(player: Player, rune: RuneData?): Boolean {
        if (rune == null) return false
        if (player.skillManager.getMaxLevel(Skill.RUNECRAFTING) < rune.levelRequirement) {
            player.packetSender.sendMessage("You need a Runecrafting level of at least " + rune.levelRequirement + " to craft this.")
            return false
        }
        if (rune.pureRequired() && !player.inventory.contains(7936) && !player.inventory.contains(1436)) {
            player.packetSender.sendMessage("You do not have any Pure essence in your inventory.")
            return false
        } else if (rune.pureRequired() && !player.inventory.contains(7936) && player.inventory.contains(1436)) {
            player.packetSender.sendMessage("Only Pure essence has the power to bind this altar's energy.")
            return false
        }
        if (!player.inventory.contains(7936) && !player.inventory.contains(1436)) {
            player.packetSender.sendMessage("You do not have any Rune or Pure essence in your inventory.")
            return false
        }
        return player.clickDelay.elapsed(4500)
    }

    @JvmStatic
    fun runecraftingAltar(player: Player?, ID: Int): Boolean {
        return ID in 2478..2488 || ID == 17010 || ID == 30624 || ID == 47120
    }
}