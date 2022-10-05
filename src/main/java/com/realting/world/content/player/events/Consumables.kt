package com.realting.world.content.player.events

import com.realting.engine.task.TaskManager
import com.realting.engine.task.impl.FireImmunityTask
import com.realting.engine.task.impl.OverloadPotionTask
import com.realting.engine.task.impl.PoisonImmunityTask
import com.realting.engine.task.impl.PrayerRenewalPotionTask
import com.realting.model.Animation
import com.realting.model.Item
import com.realting.model.Locations
import com.realting.model.Skill
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.world.content.Sounds
import com.realting.world.content.minigames.Dueling.Companion.checkRule
import com.realting.world.content.minigames.Dueling.DuelRule
import com.realting.world.content.player.skill.SkillManager.Companion.getLevelForExperience
import java.util.*

/**
 * Consumables are items that players can use to restore stats/points.
 * Examples of Consumable items: Food, Potions
 *
 * @author Gabriel Hannason
 */
object Consumables {
    /**
     * Checks if `item` is a valid consumable food type.
     * @param player    The player clicking on `item`.
     * @param item        The item being clicked upon.
     * @param slot        The slot of the item.
     * @return            If `true` player will proceed to eat said item.
     */
    @JvmStatic
    fun isFood(player: Player, item: Int, slot: Int): Boolean {
        val food = FoodType.types[item]
        if (food != null) {
            eat(player, food, slot)
            return true
        }
        return false
    }

    /**
     * The heal option on the Health Orb
     * @param player        The player to heal
     */
    @JvmStatic
    fun handleHealAction(player: Player) {
        if (!player.foodTimer.elapsed(1100)) return
        for (item in player.inventory.items) {
            if (item != null) {
                if (isFood(player, item.id, player.inventory.getSlot(item.id))) {
                    return
                }
            }
        }
        player.packetSender.sendMessage("You do not have any items that can heal you in your inventory.")
    }

    /**
     * Handles the player eating said food type.
     * @param player    The player eating the consumable.
     * @param food        The food type being consumed.
     * @param slot        The slot of the food being eaten.
     */
    private fun eat(player: Player, food: FoodType?, slot: Int) {
        if (player.constitution <= 0) return
        if (checkRule(player, DuelRule.NO_FOOD)) {
            player.packetSender.sendMessage("Food has been disabled in this duel.")
            return
        }
        if (food != null && player.foodTimer.elapsed(1100)) {
            player.combatBuilder.incrementAttackTimer(2).cooldown(false)
            player.combatBuilder.distanceSession = null
            player.castSpell = null
            player.foodTimer.reset()
            //	player.getPotionTimer().reset();
            player.packetSender.sendInterfaceRemoval()
            player.performAnimation(Animation(829))
            player.inventory.delete(food.item, slot)
            var heal = food.heal
            val nexEffect = player.equipment.wearingNexAmours()
            if (food == FoodType.ROCKTAIL) {
                var max = player.skillManager.getMaxLevel(Skill.CONSTITUTION) + 100
                if (nexEffect) max = player.skillManager.getMaxLevel(Skill.CONSTITUTION) + 400
                if (player.skillManager.getCurrentLevel(Skill.CONSTITUTION) >= max) {
                    heal = 100
                }
                if (player.constitution + heal > max) {
                    player.constitution = max
                }
            } else {
                var max = player.skillManager.getMaxLevel(Skill.CONSTITUTION)
                if (nexEffect) max = 1390
                if (heal + player.skillManager.getCurrentLevel(Skill.CONSTITUTION) > max) {
                    heal = max - player.skillManager.getCurrentLevel(Skill.CONSTITUTION)
                }
            }
            if (food == FoodType.PAPAYA) {
                if (player.runEnergy < 100) {
                    player.runEnergy = 100
                    player.packetSender.sendRunEnergy(100)
                    player.lastRunRecovery.reset()
                }
            }
            if (food == FoodType.CAKE || food == FoodType.SECOND_CAKE_SLICE || food == FoodType.CHOCOLATE_CAKE || food == FoodType.SECOND_SLICE_OF_CHOCOLATE_CAKE) {
                player.inventory.add(Item(food.item.id + 2, 1))
            } else if (food == FoodType.SALMON) {
                //Achievements.finishAchievement(player, AchievementData.EAT_A_SALMON);
            }
            val e = if (food.toString() === "BANDAGES") "use" else "eat"
            player.packetSender.sendMessage("You " + e + " the " + food.name + ".")
            player.constitution = player.constitution + heal
            if (player.constitution > 1190 && !nexEffect) player.constitution = 1190
            Sounds.sendSound(player, Sounds.Sound.EAT_FOOD)
        }
    }

    /**
     * Potions
     */
    @JvmStatic
    fun isPotion(itemId: Int): Boolean {
        val pot = ItemDefinition.forId(itemId).name
        return (pot.contains("(4)") || pot.contains("(3)") || pot.contains("(2)") || pot.contains("(1)") || pot.contains(
            "ummer pi"
        ) || pot.equals("beer", ignoreCase = true) || pot.equals("whisky", ignoreCase = true)
                || pot.equals("Jug of wine", ignoreCase = true) || pot.equals("vodka", ignoreCase = true) || pot.equals(
            "brandy",
            ignoreCase = true
        ) || pot.equals("grog", ignoreCase = true) || pot.equals("wizard's mind bomb", ignoreCase = true))
    }

    fun healingPotion(itemId: Int): Boolean {
        var pot = ItemDefinition.forId(itemId).name
        pot = pot.lowercase(Locale.getDefault())
        return pot.contains("saradomin brew")
    }

    @JvmStatic
    fun handlePotion(player: Player, itemId: Int, slot: Int) {
        if (player.constitution <= 0) return
        if (checkRule(player, DuelRule.NO_FOOD) && healingPotion(itemId)) {
            player.packetSender.sendMessage("Since food has been disabled in this duel, health-healing potions won't work.")
            return
        }
        if (checkRule(player, DuelRule.NO_POTIONS)) {
            player.packetSender.sendMessage("Potions have been disabled in this duel.")
            return
        }
        if (player.potionTimer.elapsed(900)) {
            when (itemId) {
                7218 -> {
                    drinkStatPotion(player, itemId, -1, slot, Skill.AGILITY.ordinal, false)
                    player.heal(110)
                    player.inventory.add(7220, 1)
                }
                7220 -> {
                    drinkStatPotion(player, itemId, 2313, slot, Skill.AGILITY.ordinal, false)
                    player.heal(110)
                }
                2428 -> drinkStatPotion(player, itemId, 121, slot, 0, false) // attack pot 4
                121 -> drinkStatPotion(player, itemId, 123, slot, 0, false) // attack pot 3
                123 -> drinkStatPotion(player, itemId, 125, slot, 0, false) // attack pot2
                125 -> drinkStatPotion(player, itemId, 229, slot, 0, false) // attack pot 1
                2432 -> drinkStatPotion(player, itemId, 133, slot, 1, false) // Defence pot 4
                133 -> drinkStatPotion(player, itemId, 135, slot, 1, false) // Defence pot 3
                135 -> drinkStatPotion(player, itemId, 137, slot, 1, false) // Defence pot 2
                137 -> drinkStatPotion(player, itemId, 229, slot, 1, false) // Defence pot 1
                113 -> drinkStatPotion(player, itemId, 115, slot, 2, false) // Strength pot 4
                115 -> drinkStatPotion(player, itemId, 117, slot, 2, false) // Strength pot 3
                117 -> drinkStatPotion(player, itemId, 119, slot, 2, false) // Strength pot 2
                119 -> drinkStatPotion(player, itemId, 229, slot, 2, false) // Strength pot 1
                1917, 2017, 1993, 2015, 2021, 1915 -> {
                    player.inventory.delete(itemId, 1)
                    player.performAnimation(Animation(829))
                    player.skillManager.setCurrentLevel(
                        Skill.STRENGTH,
                        if (player.skillManager.getCurrentLevel(Skill.STRENGTH) + 3 > player.skillManager.getMaxLevel(
                                Skill.STRENGTH
                            )
                        ) player.skillManager.getMaxLevel(Skill.STRENGTH) + 3 else player.skillManager.getCurrentLevel(
                            Skill.STRENGTH
                        ) + 3
                    )
                    player.heal(10)
                    player.inventory.add(1919, 1)
                    player.inventory.refreshItems()
                }
                1907 -> {
                    player.inventory.delete(itemId, 1)
                    player.performAnimation(Animation(829))
                    player.skillManager.setCurrentLevel(
                        Skill.MAGIC,
                        if (player.skillManager.getCurrentLevel(Skill.MAGIC) + 3 > player.skillManager.getMaxLevel(Skill.MAGIC)) player.skillManager.getMaxLevel(
                            Skill.MAGIC
                        ) + 3 else player.skillManager.getCurrentLevel(Skill.MAGIC) + 3
                    )
                    player.heal(10)
                    player.inventory.add(1919, 1)
                    player.inventory.refreshItems()
                }
                2446 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(175, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 86)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 86 seconds.")
                }
                175 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(177, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 86)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 86 seconds.")
                }
                177 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(179, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 86)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 86 seconds.")
                }
                179 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 86)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 86 seconds.")
                }
                2430 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(127, 1)
                    player.inventory.refreshItems()
                    var i = 0
                    while (i <= 24) {
                        if (i == 3 || i == 5) {
                            i++
                            continue
                        }
                        if (player.skillManager.getCurrentLevel(Skill.forId(i)) < player.skillManager.getMaxLevel(i)) {
                            player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                (player.skillManager.getCurrentLevel(Skill.forId(i)) + player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                ) * 0.11).toInt()
                            )
                            if (player.skillManager.getCurrentLevel(Skill.forId(i)) > player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                )
                            ) player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                player.skillManager.getMaxLevel(Skill.forId(i))
                            )
                        }
                        i++
                    }
                }
                127 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(129, 1)
                    player.inventory.refreshItems()
                    var i = 0
                    while (i <= 24) {
                        if (i == 3 || i == 5) {
                            i++
                            continue
                        }
                        if (player.skillManager.getCurrentLevel(Skill.forId(i)) < player.skillManager.getMaxLevel(i)) {
                            player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                (player.skillManager.getCurrentLevel(Skill.forId(i)) + player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                ) * 0.11).toInt()
                            )
                            if (player.skillManager.getCurrentLevel(Skill.forId(i)) > player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                )
                            ) player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                player.skillManager.getMaxLevel(Skill.forId(i))
                            )
                        }
                        i++
                    }
                }
                129 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(131, 1)
                    player.inventory.refreshItems()
                    var i = 0
                    while (i <= 24) {
                        if (i == 3 || i == 5) {
                            i++
                            continue
                        }
                        if (player.skillManager.getCurrentLevel(Skill.forId(i)) < player.skillManager.getMaxLevel(i)) {
                            player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                (player.skillManager.getCurrentLevel(Skill.forId(i)) + player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                ) * 0.11).toInt()
                            )
                            if (player.skillManager.getCurrentLevel(Skill.forId(i)) > player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                )
                            ) player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                player.skillManager.getMaxLevel(Skill.forId(i))
                            )
                        }
                        i++
                    }
                }
                131 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    var i = 0
                    while (i <= 24) {
                        if (i == 3 || i == 5) {
                            i++
                            continue
                        }
                        if (player.skillManager.getCurrentLevel(Skill.forId(i)) < player.skillManager.getMaxLevel(i)) {
                            player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                (player.skillManager.getCurrentLevel(Skill.forId(i)) + player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                ) * 0.11).toInt()
                            )
                            if (player.skillManager.getCurrentLevel(Skill.forId(i)) > player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                )
                            ) player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                player.skillManager.getMaxLevel(Skill.forId(i))
                            )
                        }
                        i++
                    }
                }
                2452 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(2454, 1)
                    player.inventory.refreshItems()
                    FireImmunityTask.makeImmune(player, 360, 50)
                    player.packetSender.sendMessage("You're now 66% immune to any kind of fire for another 6 minutes.")
                }
                2454 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(2456, 1)
                    player.inventory.refreshItems()
                    FireImmunityTask.makeImmune(player, 360, 50)
                    player.packetSender.sendMessage("You're now 66% immune to any kind of fire for another 6 minutes.")
                }
                2456 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(2458, 1)
                    player.inventory.refreshItems()
                    FireImmunityTask.makeImmune(player, 360, 50)
                    player.packetSender.sendMessage("You're now 66% immune to any kind of fire for another 6 minutes.")
                }
                2458 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    FireImmunityTask.makeImmune(player, 360, 50)
                    player.packetSender.sendMessage("You're now 66% immune to any kind of fire for another 6 minutes.")
                }
                15304 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15305, 1)
                    player.inventory.refreshItems()
                    FireImmunityTask.makeImmune(player, 360, 100)
                    player.packetSender.sendMessage("You're now 100% immune to any kind of fire for another 6 minutes.")
                }
                15305 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15306, 1)
                    player.inventory.refreshItems()
                    FireImmunityTask.makeImmune(player, 360, 100)
                    player.packetSender.sendMessage("You're now 100% immune to any kind of fire for another 6 minutes.")
                }
                15306 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15307, 1)
                    player.inventory.refreshItems()
                    FireImmunityTask.makeImmune(player, 360, 100)
                    player.packetSender.sendMessage("You're now 100% immune to any kind of fire for another 6 minutes.")
                }
                15307 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    FireImmunityTask.makeImmune(player, 360, 100)
                    player.packetSender.sendMessage("You're now 100% immune to any kind of fire for another 6 minutes.")
                }
                3016, 3008 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(itemId + 2, 1)
                    player.inventory.refreshItems()
                    player.runEnergy = player.runEnergy + if (itemId == 3008) 15 else 40
                    if (player.runEnergy > 100) player.runEnergy = 100
                }
                3018, 3010 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(itemId + 2, 1)
                    player.inventory.refreshItems()
                    player.runEnergy = player.runEnergy + if (itemId == 3010) 15 else 40
                    if (player.runEnergy > 100) player.runEnergy = 100
                }
                3020, 3012 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(itemId + 2, 1)
                    player.inventory.refreshItems()
                    player.runEnergy = player.runEnergy + if (itemId == 3012) 15 else 40
                    if (player.runEnergy > 100) player.runEnergy = 100
                }
                3022, 3014 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    player.runEnergy = player.runEnergy + if (itemId == 3014) 15 else 40
                    if (player.runEnergy > 100) player.runEnergy = 100
                }
                9739 -> {
                    drinkStatPotion(player, itemId, 9741, slot, 0, false)
                    drinkStatPotion(player, itemId, 9741, slot, 2, false)
                }
                9741 -> {
                    drinkStatPotion(player, itemId, 9743, slot, 0, false)
                    drinkStatPotion(player, itemId, 9743, slot, 2, false)
                }
                9743 -> {
                    drinkStatPotion(player, itemId, 9745, slot, 0, false)
                    drinkStatPotion(player, itemId, 9745, slot, 2, false)
                }
                9745 -> {
                    drinkStatPotion(player, itemId, 229, slot, 0, false)
                    drinkStatPotion(player, itemId, 229, slot, 2, false)
                }
                3032 -> drinkStatPotion(player, itemId, 3034, slot, 16, false) // Agility pot 4
                3034 -> drinkStatPotion(player, itemId, 3036, slot, 16, false) // Agility pot 3
                3036 -> drinkStatPotion(player, itemId, 3038, slot, 16, false) // Agility pot 2
                3038 -> drinkStatPotion(player, itemId, 229, slot, 16, false) // Agility pot 1
                2434 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(139, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.PRAYER,
                        (player.skillManager.getCurrentLevel(Skill.PRAYER) + player.skillManager.getMaxLevel(Skill.PRAYER) * 0.33).toInt()
                    )
                    if (player.skillManager.getCurrentLevel(Skill.PRAYER) > player.skillManager.getMaxLevel(Skill.PRAYER)) player.skillManager.setCurrentLevel(
                        Skill.PRAYER,
                        player.skillManager.getMaxLevel(Skill.PRAYER)
                    )
                }
                139 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(141, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.PRAYER,
                        (player.skillManager.getCurrentLevel(Skill.PRAYER) + player.skillManager.getMaxLevel(Skill.PRAYER) * 0.33).toInt()
                    )
                    if (player.skillManager.getCurrentLevel(Skill.PRAYER) > player.skillManager.getMaxLevel(Skill.PRAYER)) player.skillManager.setCurrentLevel(
                        Skill.PRAYER,
                        player.skillManager.getMaxLevel(Skill.PRAYER)
                    )
                }
                141 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(143, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.PRAYER,
                        (player.skillManager.getCurrentLevel(Skill.PRAYER) + player.skillManager.getMaxLevel(Skill.PRAYER) * 0.33).toInt()
                    )
                    if (player.skillManager.getCurrentLevel(Skill.PRAYER) > player.skillManager.getMaxLevel(Skill.PRAYER)) player.skillManager.setCurrentLevel(
                        Skill.PRAYER,
                        player.skillManager.getMaxLevel(Skill.PRAYER)
                    )
                }
                143 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.PRAYER,
                        (player.skillManager.getCurrentLevel(Skill.PRAYER) + player.skillManager.getMaxLevel(Skill.PRAYER) * 0.33).toInt()
                    )
                    if (player.skillManager.getCurrentLevel(Skill.PRAYER) > player.skillManager.getMaxLevel(Skill.PRAYER)) player.skillManager.setCurrentLevel(
                        Skill.PRAYER,
                        player.skillManager.getMaxLevel(Skill.PRAYER)
                    )
                }
                12140 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(12142, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.SUMMONING,
                        (player.skillManager.getCurrentLevel(Skill.SUMMONING) + player.skillManager.getMaxLevel(Skill.SUMMONING) * 0.25).toInt()
                    )
                    if (player.skillManager.getCurrentLevel(Skill.SUMMONING) > player.skillManager.getMaxLevel(Skill.SUMMONING)) player.skillManager.setCurrentLevel(
                        Skill.SUMMONING,
                        player.skillManager.getMaxLevel(Skill.SUMMONING)
                    )
                }
                12142 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(12144, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.SUMMONING,
                        (player.skillManager.getCurrentLevel(Skill.SUMMONING) + player.skillManager.getMaxLevel(Skill.SUMMONING) * 0.25).toInt()
                    )
                    if (player.skillManager.getCurrentLevel(Skill.SUMMONING) > player.skillManager.getMaxLevel(Skill.SUMMONING)) player.skillManager.setCurrentLevel(
                        Skill.SUMMONING,
                        player.skillManager.getMaxLevel(Skill.SUMMONING)
                    )
                }
                12144 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(12146, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.SUMMONING,
                        (player.skillManager.getCurrentLevel(Skill.SUMMONING) + player.skillManager.getMaxLevel(Skill.SUMMONING) * 0.25).toInt()
                    )
                    if (player.skillManager.getCurrentLevel(Skill.SUMMONING) > player.skillManager.getMaxLevel(Skill.SUMMONING)) player.skillManager.setCurrentLevel(
                        Skill.SUMMONING,
                        player.skillManager.getMaxLevel(Skill.SUMMONING)
                    )
                }
                12146 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.SUMMONING,
                        (player.skillManager.getCurrentLevel(Skill.SUMMONING) + player.skillManager.getMaxLevel(Skill.SUMMONING) * 0.25).toInt()
                    )
                    if (player.skillManager.getCurrentLevel(Skill.SUMMONING) > player.skillManager.getMaxLevel(Skill.SUMMONING)) player.skillManager.setCurrentLevel(
                        Skill.SUMMONING,
                        player.skillManager.getMaxLevel(Skill.SUMMONING)
                    )
                }
                14838 -> drinkStatPotion(player, itemId, 14840, slot, 12, false) // Crafting pot 4
                14840 -> drinkStatPotion(player, itemId, 14842, slot, 12, false) // Crafting pot 3
                14842 -> drinkStatPotion(player, itemId, 14844, slot, 12, false) // Crafting pot 2
                14844 -> drinkStatPotion(player, itemId, 229, slot, 12, false) // Crafting pot 1
                2436 -> drinkStatPotion(player, itemId, 145, slot, 0, true) // Super Attack pot 4
                145 -> drinkStatPotion(player, itemId, 147, slot, 0, true) // Super Attack pot 3
                147 -> drinkStatPotion(player, itemId, 149, slot, 0, true) // Super Attack pot 2
                149 -> drinkStatPotion(player, itemId, 229, slot, 0, true) // Super Attack pot 1
                2448 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(181, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 346)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 346 seconds.")
                }
                181 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(183, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 346)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 346 seconds.")
                }
                183 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(185, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 346)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 346 seconds.")
                }
                185 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 346)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 346 seconds.")
                }
                2438 -> drinkStatPotion(player, itemId, 151, slot, 10, false) // Fishing pot 4
                151 -> drinkStatPotion(player, itemId, 153, slot, 10, false) // Fishing pot 3
                153 -> drinkStatPotion(player, itemId, 155, slot, 10, false) // Fishing pot 2
                155 -> drinkStatPotion(player, itemId, 229, slot, 10, false) // Fishing pot 1
                9998 -> drinkStatPotion(player, itemId, 10000, slot, 22, false) // Hunter pot 4
                10000 -> drinkStatPotion(player, itemId, 10002, slot, 22, false) // Hunter pot 3
                10002 -> drinkStatPotion(player, itemId, 10004, slot, 22, false) // Hunter pot 2
                10004 -> drinkStatPotion(player, itemId, 229, slot, 22, false) // Hunter pot 1
                2440 -> drinkStatPotion(player, itemId, 157, slot, 2, true) // Super Strength pot 4
                157 -> drinkStatPotion(player, itemId, 159, slot, 2, true) // Super Strength pot 3
                159 -> drinkStatPotion(player, itemId, 161, slot, 2, true) // Super Strength pot 2
                161 -> drinkStatPotion(player, itemId, 229, slot, 2, true) // Super Strength pot 1
                14846 -> drinkStatPotion(player, itemId, 14848, slot, 9, false) // Fletching pot 4
                14848 -> drinkStatPotion(player, itemId, 14850, slot, 9, false) // Fletching pot 3
                14850 -> drinkStatPotion(player, itemId, 14852, slot, 9, false) // Fletching pot 2
                14852 -> drinkStatPotion(player, itemId, 229, slot, 9, false) // Fletching pot 1
                3024 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(3026, 1)
                    player.inventory.refreshItems()
                    var i = 0
                    while (i <= 24) {
                        if (i == 3) {
                            i++
                            continue
                        }
                        if (player.skillManager.getCurrentLevel(Skill.forId(i)) < player.skillManager.getMaxLevel(i)) {
                            val restoreMod = if (i == 5) 0.29 else 0.18
                            val toRestore =
                                (player.skillManager.getCurrentLevel(Skill.forId(i)) + player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                ) * restoreMod).toInt()
                            player.skillManager.setCurrentLevel(Skill.forId(i), toRestore)
                            if (player.skillManager.getCurrentLevel(Skill.forId(i)) > player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                )
                            ) player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                player.skillManager.getMaxLevel(Skill.forId(i))
                            )
                        }
                        i++
                    }
                }
                3026 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(3028, 1)
                    player.inventory.refreshItems()
                    var i = 0
                    while (i <= 24) {
                        if (i == 3) {
                            i++
                            continue
                        }
                        if (player.skillManager.getCurrentLevel(Skill.forId(i)) < player.skillManager.getMaxLevel(i)) {
                            val restoreMod = if (i == 5) 0.29 else 0.18
                            val toRestore =
                                (player.skillManager.getCurrentLevel(Skill.forId(i)) + player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                ) * restoreMod).toInt()
                            player.skillManager.setCurrentLevel(Skill.forId(i), toRestore)
                            if (player.skillManager.getCurrentLevel(Skill.forId(i)) > player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                )
                            ) player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                player.skillManager.getMaxLevel(Skill.forId(i))
                            )
                        }
                        i++
                    }
                }
                3028 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(3030, 1)
                    player.inventory.refreshItems()
                    var i = 0
                    while (i <= 24) {
                        if (i == 3) {
                            i++
                            continue
                        }
                        if (player.skillManager.getCurrentLevel(Skill.forId(i)) < player.skillManager.getMaxLevel(i)) {
                            val restoreMod = if (i == 5) 0.29 else 0.18
                            val toRestore =
                                (player.skillManager.getCurrentLevel(Skill.forId(i)) + player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                ) * restoreMod).toInt()
                            player.skillManager.setCurrentLevel(Skill.forId(i), toRestore)
                            if (player.skillManager.getCurrentLevel(Skill.forId(i)) > player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                )
                            ) player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                player.skillManager.getMaxLevel(Skill.forId(i))
                            )
                        }
                        i++
                    }
                }
                3030 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    var i = 0
                    while (i <= 24) {
                        if (i == 3) {
                            i++
                            continue
                        }
                        if (player.skillManager.getCurrentLevel(Skill.forId(i)) < player.skillManager.getMaxLevel(i)) {
                            val restoreMod = if (i == 5) 0.29 else 0.18
                            val toRestore =
                                (player.skillManager.getCurrentLevel(Skill.forId(i)) + player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                ) * restoreMod).toInt()
                            player.skillManager.setCurrentLevel(Skill.forId(i), toRestore)
                            if (player.skillManager.getCurrentLevel(Skill.forId(i)) > player.skillManager.getMaxLevel(
                                    Skill.forId(i)
                                )
                            ) player.skillManager.setCurrentLevel(
                                Skill.forId(i),
                                player.skillManager.getMaxLevel(Skill.forId(i))
                            )
                        }
                        i++
                    }
                }
                2442 -> drinkStatPotion(player, itemId, 163, slot, 1, true)
                163 -> drinkStatPotion(player, itemId, 165, slot, 1, true)
                165 -> drinkStatPotion(player, itemId, 167, slot, 1, true)
                167 -> drinkStatPotion(player, itemId, 229, slot, 1, true)
                5943 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(5945, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 518)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 518 seconds.")
                }
                5945 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(5947, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 518)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 518 seconds.")
                }
                5947 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(5949, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 518)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 518 seconds.")
                }
                5949 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    PoisonImmunityTask.makeImmune(player, 518)
                    player.packetSender.sendMessage("You're now immune to any kind of poison for another 518 seconds.")
                }
                2444 -> drinkStatPotion(player, itemId, 169, slot, 4, false) // Ranging pot 4
                169 -> drinkStatPotion(player, itemId, 171, slot, 4, false) // Ranging pot 3
                171 -> drinkStatPotion(player, itemId, 173, slot, 4, false) // Ranging pot 2
                173 -> drinkStatPotion(player, itemId, 229, slot, 4, false) // Ranging pot 1
                3040 -> drinkStatPotion(player, itemId, 3042, slot, 6, false) // Magic pot 4
                3042 -> drinkStatPotion(player, itemId, 3044, slot, 6, false) // Magic pot 3
                3044 -> drinkStatPotion(player, itemId, 3046, slot, 6, false) // Magic pot 2
                3046 -> drinkStatPotion(player, itemId, 229, slot, 6, false) // Magic pot 1
                2450 -> {
                    if (player.constitution < 100) {
                        player.packetSender.sendMessage("Your Constitution is too low for this potion.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(189, 1)
                    player.inventory.refreshItems()
                    val toDecrease = intArrayOf(1, 3)
                    for (tD in toDecrease) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            player.skillManager.getCurrentLevel(Skill.forId(tD)) - getBrewStat(player, tD, .10)
                        )
                        if (player.skillManager.getCurrentLevel(Skill.forId(tD)) < 0) player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            if (tD == 1) 1 else 100
                        )
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(0),
                        player.skillManager.getCurrentLevel(Skill.forId(0)) - getBrewStat(player, 0, .20)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(0)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                0
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(0),
                            (player.skillManager.getMaxLevel(Skill.forId(0)) * 1.2).toInt()
                        )
                    }
                    if (player.skillManager.getCurrentLevel(Skill.ATTACK) <= 0) player.skillManager.setCurrentLevel(
                        Skill.ATTACK,
                        1
                    )
                    player.skillManager.setCurrentLevel(
                        Skill.forId(2),
                        player.skillManager.getCurrentLevel(Skill.forId(2)) + getBrewStat(player, 2, .12)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(2)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                2
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(2),
                            (player.skillManager.getMaxLevel(Skill.forId(2)) * 1.2).toInt()
                        )
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(5),
                        player.skillManager.getCurrentLevel(Skill.forId(5)) + getBrewStat(player, 5, .10)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(5)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                5
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(5),
                            (player.skillManager.getMaxLevel(Skill.forId(5)) * 1.2).toInt()
                        )
                    }
                }
                189 -> {
                    if (player.constitution < 100) {
                        player.packetSender.sendMessage("Your Constitution is too low for this potion.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(191, 1)
                    player.inventory.refreshItems()
                    val toDecrease1 = intArrayOf(1, 3)
                    for (tD in toDecrease1) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            player.skillManager.getCurrentLevel(Skill.forId(tD)) - getBrewStat(player, tD, .10)
                        )
                        if (player.skillManager.getCurrentLevel(Skill.forId(tD)) < 0) player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            if (tD == 1) 1 else 100
                        )
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(0),
                        player.skillManager.getCurrentLevel(Skill.forId(0)) - getBrewStat(player, 0, .20)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(0)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                0
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(0),
                            (player.skillManager.getMaxLevel(Skill.forId(0)) * 1.2).toInt()
                        )
                    }
                    if (player.skillManager.getCurrentLevel(Skill.ATTACK) <= 0) player.skillManager.setCurrentLevel(
                        Skill.ATTACK,
                        1
                    )
                    player.skillManager.setCurrentLevel(
                        Skill.forId(2),
                        player.skillManager.getCurrentLevel(Skill.forId(2)) + getBrewStat(player, 2, .12)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(2)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                2
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(2),
                            (player.skillManager.getMaxLevel(Skill.forId(2)) * 1.2).toInt()
                        )
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(5),
                        player.skillManager.getCurrentLevel(Skill.forId(5)) + getBrewStat(player, 5, .10)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(5)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                5
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(5),
                            (player.skillManager.getMaxLevel(Skill.forId(5)) * 1.2).toInt()
                        )
                    }
                }
                191 -> {
                    if (player.constitution < 100) {
                        player.packetSender.sendMessage("Your Constitution is too low for this potion.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(193, 1)
                    player.inventory.refreshItems()
                    val toDecrease11 = intArrayOf(1, 3)
                    for (tD in toDecrease11) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            player.skillManager.getCurrentLevel(Skill.forId(tD)) - getBrewStat(player, tD, .10)
                        )
                        if (player.skillManager.getCurrentLevel(Skill.forId(tD)) < 0) player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            if (tD == 1) 1 else 100
                        )
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(0),
                        player.skillManager.getCurrentLevel(Skill.forId(0)) - getBrewStat(player, 0, .20)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(0)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                0
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(0),
                            (player.skillManager.getMaxLevel(Skill.forId(0)) * 1.2).toInt()
                        )
                    }
                    if (player.skillManager.getCurrentLevel(Skill.ATTACK) <= 0) player.skillManager.setCurrentLevel(
                        Skill.ATTACK,
                        1
                    )
                    player.skillManager.setCurrentLevel(
                        Skill.forId(2),
                        player.skillManager.getCurrentLevel(Skill.forId(2)) + getBrewStat(player, 2, .12)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(2)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                2
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(2),
                            (player.skillManager.getMaxLevel(Skill.forId(2)) * 1.2).toInt()
                        )
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(5),
                        player.skillManager.getCurrentLevel(Skill.forId(5)) + getBrewStat(player, 5, .10)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(5)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                5
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(5),
                            (player.skillManager.getMaxLevel(Skill.forId(5)) * 1.2).toInt()
                        )
                    }
                }
                193 -> {
                    if (player.constitution < 100) {
                        player.packetSender.sendMessage("Your Constitution is too low for this potion.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    val toDecrease111 = intArrayOf(1, 3)
                    for (tD in toDecrease111) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            player.skillManager.getCurrentLevel(Skill.forId(tD)) - getBrewStat(player, tD, .10)
                        )
                        if (player.skillManager.getCurrentLevel(Skill.forId(tD)) < 0) player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            if (tD == 1) 1 else 100
                        )
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(0),
                        player.skillManager.getCurrentLevel(Skill.forId(0)) - getBrewStat(player, 0, .20)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(0)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                0
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(0),
                            (player.skillManager.getMaxLevel(Skill.forId(0)) * 1.2).toInt()
                        )
                    }
                    if (player.skillManager.getCurrentLevel(Skill.ATTACK) <= 0) player.skillManager.setCurrentLevel(
                        Skill.ATTACK,
                        1
                    )
                    player.skillManager.setCurrentLevel(
                        Skill.forId(2),
                        player.skillManager.getCurrentLevel(Skill.forId(2)) + getBrewStat(player, 2, .12)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(2)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                2
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(2),
                            (player.skillManager.getMaxLevel(Skill.forId(2)) * 1.2).toInt()
                        )
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(5),
                        player.skillManager.getCurrentLevel(Skill.forId(5)) + getBrewStat(player, 5, .10)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(5)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                5
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(5),
                            (player.skillManager.getMaxLevel(Skill.forId(5)) * 1.2).toInt()
                        )
                    }
                }
                6685 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(6687, 1)
                    player.inventory.refreshItems()
                    val decrease = intArrayOf(0, 2, 4, 6)
                    for (tD in decrease) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            player.skillManager.getCurrentLevel(Skill.forId(tD)) - getBrewStat(player, tD, .10)
                        )
                        if (player.skillManager.getCurrentLevel(Skill.forId(tD)) < 0) player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            1
                        )
                        player.skillManager.updateSkill(Skill.forId(tD))
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(1),
                        player.skillManager.getCurrentLevel(Skill.forId(1)) + getBrewStat(player, 1, .20)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(1)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                1
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(1),
                            (player.skillManager.getMaxLevel(Skill.forId(1)) * 1.2).toInt()
                        )
                    }
                    player.skillManager.updateSkill(Skill.forId(1))
                    val amount = if (player.equipment.wearingNexAmours()) 1.22 else 1.17
                    val bonus = if (player.equipment.wearingNexAmours()) getBrewStat(player, 3, .21) else getBrewStat(
                        player,
                        3,
                        .15
                    )
                    player.skillManager.setCurrentLevel(
                        Skill.forId(3),
                        player.skillManager.getCurrentLevel(Skill.forId(3)) + bonus
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(3)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                3
                            )
                        ) * amount + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(3),
                            (player.skillManager.getMaxLevel(Skill.forId(3)) * amount).toInt()
                        )
                    }
                }
                6687 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(6689, 1)
                    player.inventory.refreshItems()
                    val decrease = intArrayOf(0, 2, 4, 6)
                    for (tD in decrease) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            player.skillManager.getCurrentLevel(Skill.forId(tD)) - getBrewStat(player, tD, .10)
                        )
                        if (player.skillManager.getCurrentLevel(Skill.forId(tD)) < 0) player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            1
                        )
                        player.skillManager.updateSkill(Skill.forId(tD))
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(1),
                        player.skillManager.getCurrentLevel(Skill.forId(1)) + getBrewStat(player, 1, .20)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(1)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                1
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(1),
                            (player.skillManager.getMaxLevel(Skill.forId(1)) * 1.2).toInt()
                        )
                    }
                    player.skillManager.updateSkill(Skill.forId(1))
                    val amount = if (player.equipment.wearingNexAmours()) 1.22 else 1.17
                    val bonus = if (player.equipment.wearingNexAmours()) getBrewStat(player, 3, .21) else getBrewStat(
                        player,
                        3,
                        .15
                    )
                    player.skillManager.setCurrentLevel(
                        Skill.forId(3),
                        player.skillManager.getCurrentLevel(Skill.forId(3)) + bonus
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(3)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                3
                            )
                        ) * amount + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(3),
                            (player.skillManager.getMaxLevel(Skill.forId(3)) * amount).toInt()
                        )
                    }
                }
                6689 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(6691, 1)
                    player.inventory.refreshItems()
                    val decrease = intArrayOf(0, 2, 4, 6)
                    for (tD in decrease) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            player.skillManager.getCurrentLevel(Skill.forId(tD)) - getBrewStat(player, tD, .10)
                        )
                        if (player.skillManager.getCurrentLevel(Skill.forId(tD)) < 0) player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            1
                        )
                        player.skillManager.updateSkill(Skill.forId(tD))
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(1),
                        player.skillManager.getCurrentLevel(Skill.forId(1)) + getBrewStat(player, 1, .20)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(1)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                1
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(1),
                            (player.skillManager.getMaxLevel(Skill.forId(1)) * 1.2).toInt()
                        )
                    }
                    player.skillManager.updateSkill(Skill.forId(1))
                    val amount = if (player.equipment.wearingNexAmours()) 1.22 else 1.17
                    val bonus = if (player.equipment.wearingNexAmours()) getBrewStat(player, 3, .21) else getBrewStat(
                        player,
                        3,
                        .15
                    )
                    player.skillManager.setCurrentLevel(
                        Skill.forId(3),
                        player.skillManager.getCurrentLevel(Skill.forId(3)) + bonus
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(3)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                3
                            )
                        ) * amount + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(3),
                            (player.skillManager.getMaxLevel(Skill.forId(3)) * amount).toInt()
                        )
                    }
                }
                6691 -> {
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    val decrease = intArrayOf(0, 2, 4, 6)
                    for (tD in decrease) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            player.skillManager.getCurrentLevel(Skill.forId(tD)) - getBrewStat(player, tD, .10)
                        )
                        if (player.skillManager.getCurrentLevel(Skill.forId(tD)) < 0) player.skillManager.setCurrentLevel(
                            Skill.forId(tD),
                            1
                        )
                        player.skillManager.updateSkill(Skill.forId(tD))
                    }
                    player.skillManager.setCurrentLevel(
                        Skill.forId(1),
                        player.skillManager.getCurrentLevel(Skill.forId(1)) + getBrewStat(player, 1, .20)
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(1)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                1
                            )
                        ) * 1.2 + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(1),
                            (player.skillManager.getMaxLevel(Skill.forId(1)) * 1.2).toInt()
                        )
                    }
                    player.skillManager.updateSkill(Skill.forId(1))
                    val amount = if (player.equipment.wearingNexAmours()) 1.22 else 1.17
                    val bonus = if (player.equipment.wearingNexAmours()) getBrewStat(player, 3, .21) else getBrewStat(
                        player,
                        3,
                        .15
                    )
                    player.skillManager.setCurrentLevel(
                        Skill.forId(3),
                        player.skillManager.getCurrentLevel(Skill.forId(3)) + bonus
                    )
                    if (player.skillManager.getCurrentLevel(Skill.forId(3)) > player.skillManager.getMaxLevel(
                            Skill.forId(
                                3
                            )
                        ) * amount + 1
                    ) {
                        player.skillManager.setCurrentLevel(
                            Skill.forId(3),
                            (player.skillManager.getMaxLevel(Skill.forId(3)) * amount).toInt()
                        )
                    }
                }
                15308 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15309, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(0),
                        player.skillManager.getCurrentLevel(Skill.forId(0)) + getExtremePotionBoost(player, 0)
                    )
                }
                15309 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15310, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(0),
                        player.skillManager.getCurrentLevel(Skill.forId(0)) + getExtremePotionBoost(player, 0)
                    )
                }
                15310 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15311, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(0),
                        player.skillManager.getCurrentLevel(Skill.forId(0)) + getExtremePotionBoost(player, 0)
                    )
                }
                15311 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(0),
                        player.skillManager.getCurrentLevel(Skill.forId(0)) + getExtremePotionBoost(player, 0)
                    )
                }
                15312 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15313, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(2),
                        player.skillManager.getCurrentLevel(Skill.forId(2)) + getExtremePotionBoost(player, 2)
                    )
                }
                15313 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15314, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(2),
                        player.skillManager.getCurrentLevel(Skill.forId(2)) + getExtremePotionBoost(player, 2)
                    )
                }
                15314 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15315, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(2),
                        player.skillManager.getCurrentLevel(Skill.forId(2)) + getExtremePotionBoost(player, 2)
                    )
                }
                15315 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(2),
                        player.skillManager.getCurrentLevel(Skill.forId(2)) + getExtremePotionBoost(player, 2)
                    )
                }
                15316 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15317, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(1),
                        player.skillManager.getCurrentLevel(Skill.forId(1)) + getExtremePotionBoost(player, 1)
                    )
                }
                15317 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15318, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(1),
                        player.skillManager.getCurrentLevel(Skill.forId(1)) + getExtremePotionBoost(player, 1)
                    )
                }
                15318 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15319, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(1),
                        player.skillManager.getCurrentLevel(Skill.forId(1)) + getExtremePotionBoost(player, 1)
                    )
                }
                15319 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(1),
                        player.skillManager.getCurrentLevel(Skill.forId(1)) + getExtremePotionBoost(player, 1)
                    )
                }
                15320 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15321, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(6),
                        player.skillManager.getCurrentLevel(Skill.forId(6)) + getExtremePotionBoost(player, 6)
                    )
                }
                15321 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15322, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(6),
                        player.skillManager.getCurrentLevel(Skill.forId(6)) + getExtremePotionBoost(player, 6)
                    )
                }
                15322 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15323, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(6),
                        player.skillManager.getCurrentLevel(Skill.forId(6)) + getExtremePotionBoost(player, 6)
                    )
                }
                15323 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(6),
                        player.skillManager.getCurrentLevel(Skill.forId(6)) + getExtremePotionBoost(player, 6)
                    )
                }
                15324 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15325, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(4),
                        player.skillManager.getCurrentLevel(Skill.forId(4)) + getExtremePotionBoost(player, 4)
                    )
                }
                15325 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15326, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(4),
                        player.skillManager.getCurrentLevel(Skill.forId(4)) + getExtremePotionBoost(player, 4)
                    )
                }
                15326 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(15327, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(4),
                        player.skillManager.getCurrentLevel(Skill.forId(4)) + getExtremePotionBoost(player, 4)
                    )
                }
                15327 -> {
                    if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                        player.packetSender.sendMessage("You cannot use this potion in the Wilderness.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229, 1)
                    player.inventory.refreshItems()
                    player.skillManager.setCurrentLevel(
                        Skill.forId(4),
                        player.skillManager.getCurrentLevel(Skill.forId(4)) + getExtremePotionBoost(player, 4)
                    )
                }
                21630 -> {
                    if (player.prayerRenewalPotionTimer > 0) {
                        player.packetSender.sendMessage("You already have the effect of a Prayer Renewal potion.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(21632, 1)
                    player.inventory.refreshItems()
                    player.prayerRenewalPotionTimer = 600
                    TaskManager.submit(PrayerRenewalPotionTask(player))
                }
                21632 -> {
                    if (player.prayerRenewalPotionTimer > 0) {
                        player.packetSender.sendMessage("You already have the effect of a Prayer Renewal potion.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(21634, 1)
                    player.inventory.refreshItems()
                    player.prayerRenewalPotionTimer = 600
                    TaskManager.submit(PrayerRenewalPotionTask(player))
                }
                21634 -> {
                    if (player.prayerRenewalPotionTimer > 0) {
                        player.packetSender.sendMessage("You already have the effect of a Prayer Renewal potion.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(21636, 1)
                    player.inventory.refreshItems()
                    player.prayerRenewalPotionTimer = 600
                    TaskManager.submit(PrayerRenewalPotionTask(player))
                }
                21636 -> {
                    if (player.prayerRenewalPotionTimer > 0) {
                        player.packetSender.sendMessage("You already have the effect of a Prayer Renewal potion.")
                        return
                    }
                    player.performAnimation(Animation(829))
                    player.inventory.items[slot] = Item(229)
                    player.inventory.refreshItems()
                    player.prayerRenewalPotionTimer = 600
                    TaskManager.submit(PrayerRenewalPotionTask(player))
                }
                15332 -> if (!drinkOverload(player, slot, 15333)) return
                15333 -> if (!drinkOverload(player, slot, 15334)) return
                15334 -> if (!drinkOverload(player, slot, 15335)) return
                15335 -> if (!drinkOverload(player, slot, 229)) return
            }
            player.packetSender.sendInterfaceRemoval()
            player.combatBuilder.incrementAttackTimer(1).cooldown(false)
            player.combatBuilder.distanceSession = null
            player.castSpell = null
            player.foodTimer.reset()
            player.potionTimer.reset()
            val potion = ItemDefinition.forId(itemId).name
            if (potion.contains("ummer pi")) {
                player.packetSender.sendMessage("You eat your $potion.")
            } else {
                player.packetSender.sendMessage("You drink some of your $potion.")
            }
            if (potion.endsWith("(4)")) {
                player.packetSender.sendMessage("You have 3 doses of potion left.")
            } else if (potion.endsWith("(3)")) {
                player.packetSender.sendMessage("You have 2 doses of potion left.")
            } else if (potion.endsWith("(2)")) {
                player.packetSender.sendMessage("You have 1 dose of potion left.")
            } else if (potion.endsWith("(1)")) {
                player.packetSender.sendMessage("You have finished your potion.")
            }
            if (player.overloadPotionTimer > 0) {  // Prevents decreasing stats
                overloadIncrease(player, Skill.ATTACK, 0.19)
                overloadIncrease(player, Skill.STRENGTH, 0.19)
                overloadIncrease(player, Skill.DEFENCE, 0.19)
                overloadIncrease(player, Skill.RANGED, 0.19)
                overloadIncrease(player, Skill.MAGIC, 0.19)
            }
            Sounds.sendSound(player, Sounds.Sound.DRINK_POTION)
        }
    }

    @JvmStatic
    fun drinkStatPotion(player: Player, potion: Int, replacePotion: Int, slot: Int, skill: Int, super_pot: Boolean) {
        if (slot >= 0) {
            player.performAnimation(Animation(829))
            player.inventory.items[slot] = Item(replacePotion, 1)
        }
        player.inventory.refreshItems()
        val cbPot = potion == 9739 || potion == 9741 || potion == 9743 || potion == 9745
        val sk = Skill.forId(skill)
        player.skillManager.setCurrentLevel(
            sk,
            player.skillManager.getCurrentLevel(sk) + getBoostedStat(player, skill, super_pot, cbPot),
            true
        )
    }

    fun drinkOverload(player: Player, slot: Int, replacePotion: Int): Boolean {
        if (player.location === Locations.Location.WILDERNESS || player.location === Locations.Location.DUEL_ARENA) {
            player.packetSender.sendMessage("You cannot use this potion here.")
            return false
        }
        if (player.overloadPotionTimer > 0) {
            player.packetSender.sendMessage("You already have the effect of an Overload potion.")
            return false
        }
        if (player.skillManager.getCurrentLevel(Skill.CONSTITUTION) < 500) {
            player.packetSender.sendMessage("You need to have at least 500 Hitpoints to drink this potion.")
            return false
        }
        player.performAnimation(Animation(829))
        player.inventory.items[slot] = Item(replacePotion, 1)
        player.inventory.refreshItems()
        player.overloadPotionTimer = 600
        TaskManager.submit(OverloadPotionTask(player))
        return true
    }

    fun getBoostedStat(player: Player, skillId: Int, sup: Boolean, combatPotion: Boolean): Int {
        val skill = Skill.forId(skillId)
        var increaseBy = 0
        increaseBy = if (sup) (player.skillManager.getMaxLevel(skill)
            .toDouble() * 0.2).toInt() else (player.skillManager.getMaxLevel(skill)
            .toDouble() * if (combatPotion) 0.10 else 0.13).toInt() + 1
        return if (player.skillManager.getCurrentLevel(skill) + increaseBy > player.skillManager.getMaxLevel(skill) + increaseBy + 1) {
            player.skillManager.getMaxLevel(skill) + increaseBy - player.skillManager.getCurrentLevel(skill)
        } else increaseBy
    }

    @JvmStatic
    fun overloadIncrease(player: Player, skill: Skill?, l: Double) {
        player.skillManager.setCurrentLevel(
            skill!!, (getLevelForExperience(
                player.skillManager.getExperience(
                    skill
                )
            ) + getLevelForExperience(player.skillManager.getExperience(skill)) * l).toInt(), true
        )
    }

    fun getExtremePotionBoost(player: Player, skill: Int): Int {
        var increaseBy = 0
        increaseBy = (getLevelForExperience(player.skillManager.getExperience(Skill.forId(skill))) * .25).toInt() + 1
        return if (player.skillManager.getCurrentLevel(Skill.forId(skill)) + increaseBy > getLevelForExperience(
                player.skillManager.getExperience(
                    Skill.forId(skill)
                )
            ) + increaseBy + 1
        ) {
            (getLevelForExperience(player.skillManager.getExperience(Skill.forId(skill))) + increaseBy
                    - player.skillManager.getCurrentLevel(Skill.forId(skill)))
        } else increaseBy
    }

    fun getBrewStat(player: Player, skill: Int, amount: Double): Int {
        return (player.skillManager.getMaxLevel(Skill.forId(skill)) * amount).toInt()
    }

    /**
     * Represents a valid consumable item.
     *
     * @author relex lawl
     */
    private enum class FoodType(val item: Item, val heal: Int) {
        /*
		 * Fish food types players can get by fishing
		 * or purchasing from other entities.
		 */
        KING_WORM(Item(2162), 20), TOAD_LEGS(Item(2152), 30), BREAD(Item(2309), 50), PAPAYA(
            Item(5972),
            50
        ),
        ROASTED_BIRD_MEAT(
            Item(9980), 60
        ),
        CHICKEN(Item(2140), 50), KEBAB(Item(1971), 40), CHEESE(Item(1985), 45), CAKE(Item(1891), 50), SECOND_CAKE_SLICE(
            Item(1893), 50
        ),
        THIRD_CAKE_SLICE(Item(1895), 50), CHOCOLATE_CAKE(Item(1897), 60), SECOND_SLICE_OF_CHOCOLATE_CAKE(
            Item(1899), 60
        ),
        THIRD_SLICE_OF_CHOCOLATE_CAKE(Item(1901), 60), STRAWBERRY(Item(5504), 60), SWEETCORN(Item(7088), 100), BANDAGES(
            Item(14640), 120
        ),
        JANGERBERRIES(Item(247), 20), WORM_CRUNCHIES(Item(2205), 70), EDIBLE_SEAWEED(Item(403), 40), ANCHOVIES(
            Item(319), 10
        ),
        SHRIMPS(Item(315), 30), SARDINE(Item(325), 40), COD(Item(339), 70), TROUT(Item(333), 70), PIKE(
            Item(351), 80
        ),
        MACKEREL(Item(355), 60), SALMON(Item(329), 90), TUNA(Item(361), 100), LOBSTER(Item(379), 120), BASS(
            Item(365), 130
        ),
        SWORDFISH(Item(373), 140), MEAT_PIZZA(Item(2293), 145), MONKFISH(Item(7946), 160), SHARK(
            Item(385), 200
        ),
        SEA_TURTLE(Item(397), 210), MANTA_RAY(Item(391), 220), CAVEFISH(Item(15266), 230), ROCKTAIL(
            Item(15272), 230
        ),  /*
		 * Baked goods food types a player
		 * can make with the cooking skill.
		 */
        POTATO(Item(1942), 10), BAKED_POTATO(Item(6701), 40), POTATO_WITH_BUTTER(Item(6703), 140), CHILLI_POTATO(
            Item(
                7054
            ), 140
        ),
        EGG_POTATO(
            Item(7056), 160
        ),
        POTATO_WITH_CHEESE(Item(6705), 160), MUSHROOM_POTATO(Item(7058), 200), TUNA_POTATO(Item(7060), 220),  /*
		 * Fruit food types which a player can get
		 * by picking from certain trees or hand-making
		 * them (such as pineapple chunks/rings).
		 */
        SPINACH_ROLL(Item(1969), 20), RABBIT(Item(3228), 50), BANANA(Item(1963), 20), BANANA_(Item(18199), 20), CABBAGE(
            Item(1965), 20
        ),
        ORANGE(Item(2108), 20), PINEAPPLE(Item(2114), 50), PINEAPPLE_CHUNKS(Item(2116), 20), PINEAPPLE_RINGS(
            Item(2118), 20
        ),
        PEACH(Item(6883), 80),  /*
		 * Dungeoneering food types, which you can get
		 * in the Dungeoneering skill dungeons.
		 */
        HEIM_CRAB(Item(18159), 20), RED_EYE(Item(18161), 50), DUSK_EEL(Item(18163), 70), GIANT_FLATFISH(
            Item(18165),
            100
        ),
        SHORT_FINNED_EEL(
            Item(18167), 120
        ),
        WEB_SNIPPER(Item(18169), 150), BOULDABASS(Item(18171), 170), SALVE_EEL(Item(18173), 200), BLUE_CRAB(
            Item(18175), 220
        ),  /*
		 * Other food types.
		 */
        EASTER_EGG(Item(1961), 14), PUMPKIN(Item(1959), 14), PURPLE_SWEETS(Item(4561), 30), OKTOBERTFEST_PRETZEL(
            Item(
                19778
            ), 120
        );

        val foodName: String =
            toString().lowercase(Locale.getDefault()).replace("__".toRegex(), "-").replace("_".toRegex(), " ")

        companion object {
            val types: MutableMap<Int, FoodType> = HashMap()

            init {
                for (type in values()) {
                    types[type.item.id] = type
                }
            }
        }
    }
}