package com.realting.world.content.skill.crafting

import com.realting.engine.task.Task
import com.realting.model.Skill
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData
import java.util.Locale
import com.realting.model.input.impl.EnterAmountOfLeatherToCraft
import com.realting.util.Misc

object LeatherMaking {
    @JvmStatic
    fun craftLeatherDialogue(player: Player, itemUsed: Int, usedWith: Int) {
        player.skillManager.stopSkilling()
        for (l in leatherData.values()) {
            val leather = if (itemUsed == 1733) usedWith else itemUsed
            if (leather == l.leather) {
                if (l.leather == 1741) {
                    player.packetSender.sendInterfaceModel(8654, 1, 150)
                    player.packetSender.sendInterface(2311)
                    player.inputHandling = EnterAmountOfLeatherToCraft()
                    player.selectedSkillingItem = leather
                    break
                } else if (l.leather == 1743) {
                    player.packetSender.sendString(2799, ItemDefinition.forId(1131).name)
                        .sendInterfaceModel(1746, 1131, 150).sendChatboxInterface(4429)
                    player.packetSender.sendString(2800, "How many would you like to make?")
                    player.inputHandling = EnterAmountOfLeatherToCraft()
                    player.selectedSkillingItem = leather
                    break
                }
                val name = arrayOf(
                    "Body", "Chaps", "Bandana", "Boots", "Vamb"
                )
                if (l.leather == 6289) {
                    player.packetSender.sendChatboxInterface(8938)
                    player.packetSender.sendInterfaceModel(8941, 6322, 180)
                    player.packetSender.sendInterfaceModel(8942, 6324, 180)
                    player.packetSender.sendInterfaceModel(8943, 6326, 180)
                    player.packetSender.sendInterfaceModel(8944, 6328, 180)
                    player.packetSender.sendInterfaceModel(8945, 6330, 180)
                    for (i in name.indices) {
                        player.packetSender.sendString(8949 + i * 4, name[i])
                    }
                    player.inputHandling = EnterAmountOfLeatherToCraft()
                    player.selectedSkillingItem = leather
                    return
                }
            }
        }
        for (d in leatherDialogueData.values()) {
            val leather = if (itemUsed == 1733) usedWith else itemUsed
            val name = arrayOf(
                "Vamb", "Chaps", "Body"
            )
            if (leather == d.leather) {
                player.packetSender.sendChatboxInterface(8880)
                player.packetSender.sendInterfaceModel(8883, d.vamb, 180)
                player.packetSender.sendInterfaceModel(8884, d.chaps, 180)
                player.packetSender.sendInterfaceModel(8885, d.body, 180)
                for (i in name.indices) {
                    player.packetSender.sendString(8889 + i * 4, name[i])
                }
                player.inputHandling = EnterAmountOfLeatherToCraft()
                player.selectedSkillingItem = leather
                return
            }
        }
    }

    @JvmStatic
    fun handleButton(player: Player, button: Int): Boolean {
        if (player.selectedSkillingItem < 0) return false
        for (l in leatherData.values()) {
            if (button == l.getButtonId(button) && player.selectedSkillingItem == l.leather) {
                craftLeather(player, l, l.getAmount(button))
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun craftLeather(player: Player, l: leatherData, amount: Int) {
        player.packetSender.sendInterfaceRemoval()
        if (l.leather == player.selectedSkillingItem) {
            if (player.skillManager.getCurrentLevel(Skill.CRAFTING) < l.level) {
                player.packetSender.sendMessage("You need a Crafting level of at least " + l.level + " to make this.")
                return
            }
            if (!player.inventory.contains(1734)) {
                player.packetSender.sendMessage("You need some thread to make this.")
                player.packetSender.sendInterfaceRemoval()
                return
            }
            if (player.inventory.getAmount(l.leather) < l.hideAmount) {
                player.packetSender.sendMessage("You need some " + ItemDefinition.forId(l.leather).name.lowercase(Locale.getDefault()) + " to make this item.")
                player.packetSender.sendInterfaceRemoval()
                return
            }
            player.currentTask = object : Task(2, player, true) {
                var toMake = amount
                public override fun execute() {
                    if (!player.inventory.contains(1734) || !player.inventory.contains(1733) || player.inventory.getAmount(
                            l.leather
                        ) < l.hideAmount
                    ) {
                        player.packetSender.sendMessage("You have run out of materials.")
                        stop()
                        return
                    }
                    if (Misc.getRandom(5) <= 3) player.inventory.delete(1734, 1)
                    player.inventory.delete(l.leather, l.hideAmount).add(l.product, 1)
                    player.skillManager.addExperience(Skill.CRAFTING, l.xP.toInt())
                    if (l == leatherData.LEATHER_BOOTS) {
                        Achievements.finishAchievement(player, AchievementData.CRAFT_A_PAIR_OF_LEATHER_BOOTS)
                    } else if (l == leatherData.BLACK_DHIDE_BODY) {
                        Achievements.doProgress(player, AchievementData.CRAFT_20_BLACK_DHIDE_BODIES)
                    }
                    player.performAnimation(Animation(1249))
                    toMake--
                    if (toMake <= 0) {
                        stop()
                        return
                    }
                }
            }
            TaskManager.submit(player.currentTask)
        }
    }
}