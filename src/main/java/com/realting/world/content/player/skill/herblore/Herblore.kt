package com.realting.world.content.player.skill.herblore

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Item
import com.realting.model.Skill
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

object Herblore {
    const val VIAL = 227
    private val ANIMATION = Animation(363)

    @JvmStatic
    fun cleanHerb(player: Player, herbId: Int): Boolean {
        val herb: Herbs = Herbs.Companion.forId(herbId) ?: return false
        if (player.inventory.contains(herb.grimyHerb)) {
            if (player.skillManager.getCurrentLevel(Skill.HERBLORE) < herb.levelReq) {
                player.packetSender.sendMessage("You need a Herblore level of at least " + herb.levelReq + " to clean this leaf.")
                return false
            }
            player.inventory.delete(herb.grimyHerb, 1)
            player.inventory.add(herb.cleanHerb, 1)
            player.skillManager.addExperience(Skill.HERBLORE, herb.exp)
            player.packetSender.sendMessage("You clean the dirt off the leaf.")
            return true
        }
        return false
    }

    @JvmStatic
    fun makeUnfinishedPotion(player: Player, herbId: Int): Boolean {
        val unf: UnfinishedPotions = UnfinishedPotions.Companion.forId(herbId) ?: return false
        if (player.skillManager.getCurrentLevel(Skill.HERBLORE) < unf.levelReq) {
            player.packetSender.sendMessage("You need a Herblore level of at least " + unf.levelReq + " to make this potion.")
            return false
        }
        if (!(player.inventory.contains(VIAL) && player.inventory.contains(unf.herbNeeded))) {
            return false
        }
        player.skillManager.stopSkilling()
        player.performAnimation(ANIMATION)
        player.currentTask = object : Task(1, player, false) {
            public override fun execute() {
                if (player.inventory.contains(VIAL) && player.inventory.contains(unf.herbNeeded)) {
                    player.inventory.delete(VIAL, 1)
                    if (player.skillManager.skillCape(Skill.HERBLORE) && Misc.getRandom(10) == 1) {
                        player.packetSender.sendMessage("Your cape saves you an herb.")
                    } else {
                        player.inventory.delete(unf.herbNeeded, 1)
                    }
                    player.performAnimation(ANIMATION)
                    player.inventory.add(unf.unfPotion, 1)
                    player.packetSender.sendMessage("You put the " + ItemDefinition.forId(unf.herbNeeded).name + " into the vial of water.")
                    player.skillManager.addExperience(Skill.HERBLORE, 1)
                    if (!player.inventory.contains(VIAL) || !player.inventory.contains(unf.herbNeeded)) {
                        stop()
                        return
                    }
                }
            }
        }
        TaskManager.submit(player.currentTask)
        return true
    }

    @JvmStatic
    fun finishPotion(player: Player, itemUsed: Int, usedWith: Int): Boolean {
        val pot = FinishedPotions.forId(itemUsed, usedWith)
        if (pot == FinishedPotions.MISSING_INGREDIENTS) {
            player.packetSender.sendMessage("You don't have the required items to make this potion.")
            return false
        }
        if (pot == null) {
            handleSpecialPotion(player, itemUsed, usedWith)
            return false
        }
        if (player.skillManager.getCurrentLevel(Skill.HERBLORE) < pot.levelReq) {
            player.packetSender.sendMessage("You need a Herblore level of at least " + pot.levelReq + " to make this potion.")
            return false
        }
        if (!(player.inventory.contains(pot.unfinishedPotion) && player.inventory.contains(pot.itemNeeded))) {
            return false
        }
        player.skillManager.stopSkilling()
        player.performAnimation(ANIMATION)
        player.currentTask = object : Task(2, player, false) {
            public override fun execute() {
                if (!player.inventory.contains(pot.unfinishedPotion) || !player.inventory.contains(pot.itemNeeded)) {
                    player.packetSender.sendMessage("You don't have the required items to make this potion.")
                    return
                }
                player.performAnimation(ANIMATION)
                player.inventory.delete(pot.unfinishedPotion, 1).delete(pot.itemNeeded, 1).add(pot.finishedPotion, 1)
                player.skillManager.addExperience(Skill.HERBLORE, pot.expGained)
                val name = ItemDefinition.forId(pot.finishedPotion).name
                player.packetSender.sendMessage("You combine the ingredients to make " + Misc.anOrA(name) + " " + name + ".")
                Achievements.finishAchievement(player, AchievementData.MIX_A_POTION)
                if (!player.inventory.contains(pot.unfinishedPotion) || !player.inventory.contains(pot.itemNeeded)) {
                    stop()
                }
            }
        }
        TaskManager.submit(player.currentTask)
        return true
    }

    @JvmStatic
    fun handleSpecialPotion(p: Player, item1: Int, item2: Int) {
        if (item1 == item2) return
        if (!p.inventory.contains(item1) || !p.inventory.contains(item2)) return
        val specialPotData = SpecialPotion.forItems(item1, item2) ?: return
        if (p.skillManager.getCurrentLevel(Skill.HERBLORE) < specialPotData.levelReq) {
            p.packetSender.sendMessage("You need a Herblore level of at least " + specialPotData.levelReq + " to make this potion.")
            return
        }
        if (!p.clickDelay.elapsed(500)) return
        for (INGREDIENTS in specialPotData.iNGREDIENTS) {
            if (!p.inventory.contains(INGREDIENTS.id) || p.inventory.getAmount(INGREDIENTS.id) < INGREDIENTS.amount) {
                p.packetSender.sendMessage("You do not have all INGREDIENTS for this potion.")
                p.packetSender.sendMessage("Remember: You can purchase an Ingridient's book from the Druid Spirit.")
                return
            }
        }
        for (INGREDIENTS in specialPotData.iNGREDIENTS) p.inventory.delete(INGREDIENTS)
        p.inventory.add(specialPotData.product)
        p.performAnimation(Animation(363))
        p.skillManager.addExperience(Skill.HERBLORE, specialPotData.experience)
        val name = specialPotData.product.definition.name
        p.packetSender.sendMessage("You make " + Misc.anOrA(name) + " " + name + ".")
        p.clickDelay.reset()
        if (specialPotData == SpecialPotion.OVERLOAD) {
            Achievements.finishAchievement(p, AchievementData.MIX_AN_OVERLOAD_POTION)
            Achievements.doProgress(p, AchievementData.MIX_100_OVERLOAD_POTIONS)
        }
    }

    internal enum class SpecialPotion(
        val iNGREDIENTS: Array<Item>, val product: Item, val levelReq: Int, val experience: Int
    ) {
        EXTREME_ATTACK(arrayOf(Item(145), Item(261)), Item(15309), 88, 220), EXTREME_STRENGTH(
            arrayOf(
                Item(157), Item(267)
            ), Item(15313), 88, 230
        ),
        EXTREME_DEFENCE(
            arrayOf(
                Item(163), Item(2481)
            ), Item(15317), 90, 240
        ),
        EXTREME_MAGIC(arrayOf(Item(3042), Item(9594)), Item(15321), 91, 250), EXTREME_RANGED(
            arrayOf(
                Item(169), Item(12539, 5)
            ), Item(15325), 92, 260
        ),
        OVERLOAD(arrayOf(Item(15309), Item(15313), Item(15317), Item(15321), Item(15325)), Item(15333), 96, 300);

        companion object {
            fun forItems(item1: Int, item2: Int): SpecialPotion? {
                for (potData in values()) {
                    var found = 0
                    for (it in potData.iNGREDIENTS) {
                        if (it.id == item1 || it.id == item2) found++
                    }
                    if (found >= 2) return potData
                }
                return null
            }
        }
    }
}