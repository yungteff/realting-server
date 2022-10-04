package com.realting.world.content.skill.herblore

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player

/**
 * Given life by Crimson on 5/14/2017.
 * Made functional by crimson tho u fucking goof
 */
enum class Crushing(val input: Int, val output: Int) {
    MUDRUNE(4698, 9594), UNICORNHORN(237, 235), CHOCOLATEBAR(1973, 1975), GOATHORN(9735, 9736), KEBBIT(10109, 10111);

    companion object {
        var pestle = 233

        @JvmStatic
        fun handleCrushing(player: Player, index: Int) {
            if (!player.inventory.contains(pestle)) {
                player.packetSender.sendMessage("You will need a pestle first.")
                return
            }
            if (values()[index] == null) {
                return
            }
            player.skillManager.stopSkilling()
            player.currentTask = object : Task(1, player, false) {
                public override fun execute() {
                    if (!player.inventory.contains(pestle) || !player.inventory.contains(
                            values()[index].input
                        )
                    ) {
                        stop()
                        return
                    }
                    if (player.inventory.isFull && ItemDefinition.forId(
                            values()[index].input
                        ).isStackable
                    ) {
                        player.packetSender.sendMessage("Your inventory is full, please make room first.")
                        stop()
                        return
                    }
                    player.inventory.delete(values()[index].input, 1)
                    player.performAnimation(Animation(364))
                    player.inventory.add(values()[index].output, 1)
                }
            }
            TaskManager.submit(player.currentTask)
            return
        }
    }
}