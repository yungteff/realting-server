package com.realting.world.content.player.skill.crafting

import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.model.input.impl.EnterAmountOfHidesToTan

object Tanning {
    @JvmStatic
    fun selectionInterface(player: Player) {
        player.packetSender.sendInterface(14670)
        player.skillManager.stopSkilling()
        for (t in tanningData.values()) {
            player.packetSender.sendInterfaceModel(t.itemFrame, t.leatherId, 250)
            player.packetSender.sendString(t.nameFrame, t.leatherName)
            if (player.inventory.getAmount(995) >= t.price) {
                player.packetSender.sendString(t.costFrame, "@gre@Price: " + t.price)
            } else {
                player.packetSender.sendString(t.costFrame, "@red@Price: " + t.price)
            }
        }
    }

    @JvmStatic
    fun tanHide(player: Player, buttonId: Int, amount: Int) {
        var amount = amount
        for (t in tanningData.values()) {
            if (buttonId == t.getButtonId(buttonId)) {
                val invAmt = player.inventory.getAmount(t.hideId)
                if (amount > invAmt) amount = invAmt
                if (amount == 0) {
                    player.packetSender.sendMessage("You do not have any " + ItemDefinition.forId(t.hideId).name + " to tan.")
                    return
                }
                if (amount > t.getAmount(buttonId)) amount = t.getAmount(buttonId)
                val price = amount * t.price
                val usePouch = player.moneyInPouch > price
                val coins = (if (usePouch) player.moneyInPouchAsInt else player.inventory.getAmount(995))
                if (coins == 0) {
                    player.packetSender.sendMessage("You do not have enough coins to tan this hide.")
                    return
                }
                amount = price / t.price
                val hide = t.hideId
                val leather = t.leatherId
                if (coins >= price) {
                    if (player.inventory.contains(hide)) {
                        player.inventory.delete(hide, amount)
                        if (usePouch) {
                            player.moneyInPouch = player.moneyInPouch - price
                            player.packetSender.sendString(8135, "" + player.moneyInPouch) //Update the money pouch
                        } else player.inventory.delete(995, price)
                        player.inventory.add(leather, amount)
                    } else {
                        player.packetSender.sendMessage("You do not have any hides to tan.")
                        return
                    }
                } else {
                    player.packetSender.sendMessage("You do not have enough coins to tan this hide.")
                    return
                }
            }
        }
    }

    @JvmStatic
    fun handleButton(player: Player, id: Int): Boolean {
        for (t in tanningData.values()) {
            if (id == t.getButtonId(id)) {
                if (t.getAmount(id) == 29) {
                    player.inputHandling = EnterAmountOfHidesToTan(id)
                    player.packetSender.sendEnterAmountPrompt("How many would you like to tan?")
                    return true
                }
                tanHide(player, id, player.inventory.getAmount(t.hideId))
                return true
            }
        }
        return false
    }
}