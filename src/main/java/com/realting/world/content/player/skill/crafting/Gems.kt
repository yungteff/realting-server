package com.realting.world.content.player.skill.crafting

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Items
import com.realting.model.Skill
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.model.input.impl.EnterAmountOfGemsToCut
import com.realting.util.Misc
import com.realting.world.content.player.events.Achievements
import com.realting.world.content.player.events.Achievements.AchievementData

object Gems {
    @JvmStatic
    fun selectionInterface(player: Player, gem: Int) {
        player.packetSender.sendInterfaceRemoval()
        val data = GEM_DATA.forUncutGem(gem) ?: return
        if (player.skillManager.getMaxLevel(Skill.CRAFTING) < data.levelReq) {
            player.packetSender.sendMessage("You need a Crafting level of atleast " + data.levelReq + " to craft this gem.")
            return
        }
        player.selectedSkillingItem = gem
        player.inputHandling = EnterAmountOfGemsToCut()
        player.packetSender.sendString(2799, ItemDefinition.forId(gem).name).sendInterfaceModel(1746, gem, 150)
            .sendChatboxInterface(4429)
        player.packetSender.sendString(2800, "How many would you like to craft?")
    }

    @JvmStatic
    fun cutGem(player: Player, amount: Int, uncutGem: Int) {
        player.packetSender.sendInterfaceRemoval()
        player.skillManager.stopSkilling()
        val data = GEM_DATA.forUncutGem(uncutGem) ?: return
        player.currentTask = object : Task(2, player, true) {
            var amountCut = 0
            public override fun execute() {
                if (!player.inventory.contains(uncutGem)) {
                    stop()
                    return
                }
                player.performAnimation(data.animation)
                player.inventory.delete(uncutGem, 1)
                if (player.skillManager.skillCape(Skill.CRAFTING) && data.amuletInt != -1 && Misc.getRandom(10) == 1) {
                    player.packetSender.sendMessage("Your cape instantly turns your gem into an amulet!")
                    player.inventory.add(data.amuletInt, 1)
                } else {
                    player.inventory.add(data.cutGem, 1)
                }
                if (data == GEM_DATA.DIAMOND) {
                    Achievements.doProgress(player, AchievementData.CRAFT_1000_DIAMOND_GEMS)
                } else if (data == GEM_DATA.ONYX) {
                    Achievements.finishAchievement(player, AchievementData.CUT_AN_ONYX_STONE)
                }
                player.skillManager.addExperience(Skill.CRAFTING, data.xpReward)
                amountCut++
                if (amountCut >= amount) stop()
            }
        }
        TaskManager.submit(player.currentTask)
    }

    internal enum class GEM_DATA(
        val uncutGem: Int,
        val cutGem: Int,
        val levelReq: Int,
        val xpReward: Int,
        val animation: Animation,
        val amuletInt: Int
    ) {
        OPAL(1625, 1609, 8, 15, Animation(886), -1), JADE(1627, 1611, 13, 20, Animation(886), -1), RED_TOPAZ(
            1629, 1613, 16, 25, Animation(887), -1
        ),
        SAPPHIRE(1623, 1607, 20, 50, Animation(888), 1727), EMERALD(
            1621, 1605, 27, 68, Animation(889), 1729
        ),
        RUBY(1619, 1603, 34, 85, Animation(892), 1725), DIAMOND(1617, 1601, 43, 108, Animation(886), 1731), DRAGONSTONE(
            1631, 1615, 55, 138, Animation(885), 1704
        ),
        ONYX(6571, 6573, 67, 168, Animation(885), 6585), ZENYTE(
            Items.UNCUT_ZENYTE, Items.ZENYTE, 89, 200, Animation(37185), Items.ZENYTE_AMULET
        );

        companion object {
            fun forUncutGem(uncutGem: Int): GEM_DATA? {
                for (data in values()) {
                    if (data.uncutGem == uncutGem) return data
                }
                return null
            }
        }
    }
}