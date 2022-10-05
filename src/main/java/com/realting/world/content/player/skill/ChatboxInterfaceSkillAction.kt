package com.realting.world.content.player.skill

import com.realting.model.entity.character.player.Player
import com.realting.model.input.impl.EnterAmountOfGemsToCut
import com.realting.world.content.player.skill.crafting.Gems
import com.realting.model.input.impl.EnterAmountToCook
import com.realting.world.content.player.skill.cooking.Cooking
import com.realting.model.input.impl.EnterAmountToSpin
import com.realting.world.content.player.skill.crafting.Flax
import com.realting.model.input.impl.EnterAmountOfBonesToSacrifice
import com.realting.world.content.player.skill.prayer.BonesOnAltar
import com.realting.model.input.impl.EnterAmountOfBowsToString
import com.realting.world.content.player.skill.fletching.Fletching
import com.realting.model.input.impl.EnterGemAmount
import com.realting.model.input.impl.EnterAmountToFletch

object ChatboxInterfaceSkillAction {
    @JvmStatic
    fun handleChatboxInterfaceButtons(player: Player, buttonId: Int) {
        if (!player.clickDelay.elapsed(3000) || player.inputHandling != null && handleMakeXInterfaces(
                player,
                buttonId
            )
        ) return
        val amount = if (buttonId == 2799) 1 else if (buttonId == 2798) 5 else if (buttonId == 1747) 28 else -1
        if (player.inputHandling == null || amount <= 0) {
            player.packetSender.sendInterfaceRemoval()
            return
        }
        if (player.inputHandling is EnterAmountOfGemsToCut) Gems.cutGem(
            player,
            amount,
            player.selectedSkillingItem
        ) else if (player.inputHandling is EnterAmountToCook) Cooking.cook(
            player,
            player.selectedSkillingItem,
            amount
        ) else if (player.inputHandling is EnterAmountToSpin) Flax.spinFlax(
            player,
            amount
        ) else if (player.inputHandling is EnterAmountOfBonesToSacrifice) BonesOnAltar.offerBones(
            player,
            amount
        ) else if (player.inputHandling is EnterAmountOfBowsToString) Fletching.stringBow(
            player,
            amount
        ) else if (player.inputHandling is EnterGemAmount) Fletching.crushGems(
            player,
            amount,
            player.selectedSkillingItem
        )
        player.clickDelay.reset()
    }

    fun handleMakeXInterfaces(player: Player, buttonId: Int): Boolean {
        if (buttonId == 8886 || buttonId == 8890 || buttonId == 8894 || buttonId == 8871 || buttonId == 8875 || buttonId == 1748) { // Fletching X amount
            if (player.inputHandling is EnterAmountToFletch) {
                (player.inputHandling as EnterAmountToFletch).setButton(buttonId)
            }
            player.packetSender.sendEnterAmountPrompt("How many would you like to make?")
            return true
        }
        return false
    }
}