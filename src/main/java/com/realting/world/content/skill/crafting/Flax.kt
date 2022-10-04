package com.realting.world.content.skill.crafting

import com.realting.engine.task.Task
import com.realting.model.input.impl.EnterAmountToSpin
import com.realting.world.content.skill.crafting.Flax
import com.realting.model.Skill
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Direction
import com.realting.model.entity.character.player.Player

object Flax {
    private const val FLAX_ID = 1779

    @JvmStatic
    fun showSpinInterface(player: Player) {
        player.packetSender.sendInterfaceRemoval()
        player.skillManager.stopSkilling()
        if (!player.inventory.contains(1779)) {
            player.packetSender.sendMessage("You do not have any Flax to spin.")
            return
        }
        player.inputHandling = EnterAmountToSpin()
        player.packetSender.sendString(2799, "Flax").sendInterfaceModel(1746, FLAX_ID, 150).sendChatboxInterface(4429)
        player.packetSender.sendString(2800, "How many would you like to make?")
    }

    @JvmStatic
    fun spinFlax(player: Player, amount: Int) {
        if (amount <= 0) return
        player.skillManager.stopSkilling()
        player.packetSender.sendInterfaceRemoval()
        player.currentTask = object : Task(2, player, true) {
            var amountSpan = 0
            public override fun execute() {
                if (!player.inventory.contains(FLAX_ID)) {
                    stop()
                    return
                }
                player.direction = Direction.NORTH
                player.skillManager.addExperience(Skill.CRAFTING, 15)
                player.performAnimation(Animation(896))
                player.inventory.delete(FLAX_ID, 1)
                player.inventory.add(1777, 1)
                amountSpan++
                if (amountSpan >= amount) stop()
            }
        }
        TaskManager.submit(player.currentTask)
    }
}