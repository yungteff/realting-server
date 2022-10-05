package com.realting.world.content.player.skill.mining

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.entity.character.player.Player
import java.util.Locale

object Prospecting {
    @JvmStatic
    fun prospectOre(player: Player, objectId: Int): Boolean {
        val oreData = MiningData.forRock(objectId)
        if (oreData != null) {
            if (!player.clickDelay.elapsed(2800)) return true
            player.skillManager.stopSkilling()
            player.packetSender.sendMessage("You examine the ore...")
            TaskManager.submit(object : Task(2, player, false) {
                public override fun execute() {
                    player.packetSender.sendMessage(
                        "..the rock contains " + oreData.toString().lowercase(Locale.getDefault()) + " ore."
                    )
                    stop()
                }
            })
            player.clickDelay.reset()
            return true
        }
        return false
    }
}