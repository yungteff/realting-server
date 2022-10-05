package com.realting.world.content.player.skill.herblore


import com.realting.model.Item
import com.realting.world.content.player.skill.herblore.PotionCombinating.CombiningDoses
import com.realting.model.entity.character.player.Player

object Decanting {
    @JvmStatic
    fun notedDecanting(player: Player) {
        /*
		 * This could be written better with an enum, but I was too lazy when writing.
		 * Behold, the culmination of all if statements, Decanting.java
		 */
        for (p in CombiningDoses.values()) {
            val fullU = p.fullId
            val fullN = Item.getNoted(fullU)
            val halfU = p.halfId
            val halfN = Item.getNoted(halfU)
            val quarterU = p.quarterId
            val quarterN = Item.getNoted(quarterU)
            val threeQuartersU = p.threeQuartersId
            val threeQuartersN = Item.getNoted(threeQuartersU)
            var totalDoses = 0
            var remainder = 0
            var totalEmptyPots = 0
            if (player.inventory.contains(fullU)) {
                totalDoses += 4 * player.inventory.getAmount(fullU)
                totalEmptyPots += player.inventory.getAmount(fullU)
                player.inventory.delete(fullU, player.inventory.getAmount(fullU))
            }
            if (player.inventory.contains(threeQuartersN)) {
                totalDoses += 3 * player.inventory.getAmount(threeQuartersN)
                totalEmptyPots += player.inventory.getAmount(threeQuartersN)
                player.inventory.delete(threeQuartersN, player.inventory.getAmount(threeQuartersN))
            }
            if (player.inventory.contains(threeQuartersU)) {
                totalDoses += 3 * player.inventory.getAmount(threeQuartersU)
                totalEmptyPots += player.inventory.getAmount(threeQuartersU)
                player.inventory.delete(threeQuartersU, player.inventory.getAmount(threeQuartersU))
            }
            if (player.inventory.contains(halfN)) {
                totalDoses += 2 * player.inventory.getAmount(halfN)
                totalEmptyPots += player.inventory.getAmount(halfN)
                player.inventory.delete(halfN, player.inventory.getAmount(halfN))
            }
            if (player.inventory.contains(halfU)) {
                totalDoses += 2 * player.inventory.getAmount(halfU)
                totalEmptyPots += player.inventory.getAmount(halfU)
                player.inventory.delete(halfU, player.inventory.getAmount(halfU))
            }
            if (player.inventory.contains(quarterN)) {
                totalDoses += 1 * player.inventory.getAmount(quarterN)
                totalEmptyPots += player.inventory.getAmount(quarterN)
                player.inventory.delete(quarterN, player.inventory.getAmount(quarterN))
            }
            if (player.inventory.contains(quarterU)) {
                totalDoses += 1 * player.inventory.getAmount(quarterU)
                totalEmptyPots += player.inventory.getAmount(quarterU)
                player.inventory.delete(quarterU, player.inventory.getAmount(quarterU))
            }
            if (totalDoses > 0) {
                when {
                    totalDoses >= 4 -> {
                        player.inventory.add(fullN, totalDoses / 4)
                        if (totalDoses % 4 != 0) {
                            totalEmptyPots -= 1
                            remainder = totalDoses % 4
                        }
                    }
                    totalDoses == 3 -> {
                        player.inventory.add(threeQuartersN, 1)
                    }
                    totalDoses == 2 -> {
                        player.inventory.add(halfN, 1)
                    }
                    totalDoses == 1 -> {
                        player.inventory.add(quarterN, 1)
                    }
                }
                if (remainder == 3) {
                    player.inventory.add(threeQuartersN, 1)
                } else if (remainder == 2) {
                    player.inventory.add(halfN, 1)
                } else if (remainder == 1) {
                    player.inventory.add(quarterN, 1)
                }
                totalEmptyPots -= totalDoses / 4
                player.inventory.add(Item.getNoted(PotionCombinating.EMPTY_VIAL), totalEmptyPots)
            }
        }
        player.packetSender.sendMessage("All applicable potions have been decanted!")
    }

    fun startDecanting(player: Player) {
        /**
         * @depreciated use [.notedDecanting] instead.
         */
        for (p in CombiningDoses.values()) {
            val full = p.fullId
            val half = p.halfId
            val quarter = p.quarterId
            val threeQuarters = p.threeQuartersId
            var totalDoses = 0
            var remainder = 0
            var totalEmptyPots = 0
            if (player.inventory.contains(threeQuarters)) {
                totalDoses += 3 * player.inventory.getAmount(threeQuarters)
                totalEmptyPots += player.inventory.getAmount(threeQuarters)
                player.inventory.delete(threeQuarters, player.inventory.getAmount(threeQuarters))
            }
            if (player.inventory.contains(half)) {
                totalDoses += 2 * player.inventory.getAmount(half)
                totalEmptyPots += player.inventory.getAmount(half)
                player.inventory.delete(half, player.inventory.getAmount(half))
            }
            if (player.inventory.contains(quarter)) {
                totalDoses += 1 * player.inventory.getAmount(quarter)
                totalEmptyPots += player.inventory.getAmount(quarter)
                player.inventory.delete(quarter, player.inventory.getAmount(quarter))
            }
            if (totalDoses > 0) {
                if (totalDoses >= 4) player.inventory.add(
                    full, totalDoses / 4
                ) else if (totalDoses == 3) player.inventory.add(
                    threeQuarters, 1
                ) else if (totalDoses == 2) player.inventory.add(
                    half, 1
                ) else if (totalDoses == 1) player.inventory.add(quarter, 1)
                if (totalDoses % 4 != 0) {
                    totalEmptyPots -= 1
                    remainder = totalDoses % 4
                    if (remainder == 3) player.inventory.add(
                        threeQuarters, 1
                    ) else if (remainder == 2) player.inventory.add(
                        half, 1
                    ) else if (remainder == 1) player.inventory.add(quarter, 1)
                }
                totalEmptyPots -= totalDoses / 4
                player.inventory.add(PotionCombinating.EMPTY_VIAL, totalEmptyPots)
            }
        }
    }
}