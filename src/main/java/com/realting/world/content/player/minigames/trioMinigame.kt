package com.realting.world.content.minigames

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Graphic
import com.realting.model.Locations
import com.realting.model.Position
import com.realting.model.entity.character.player.Player

/**
 * Created by brandon on 5/3/2016.
 */
object trioMinigame {
    @JvmStatic
    fun handleTokenRemoval(player: Player) {
        if (player.minigameAttributes.trioAttuibutes.joinedBossRoom()) return
        player.minigameAttributes.trioAttuibutes.setJoinedBossRoom(true)
        //  player.getPacketSender().sendMessage("Sumbiting the task.");
        //task = 1 * 600 = taskDelay
        TaskManager.submit(object : Task(75, player, false) {
            public override fun execute() {
                if (!player.minigameAttributes.trioAttuibutes.joinedBossRoom()) {
                    //     handleNPCdeSpawning(player);
                    stop()
                    return
                    //fail safe
                }
                if (player.location !== Locations.Location.TRIO_ZONE) {
                    player.minigameAttributes.trioAttuibutes.setJoinedBossRoom(false)
                    stop()
                    return
                    // fail safe #2
                }
                if (player.inventory.contains(11180)) {
                    player.inventory.delete(11180, 1)
                    player.performGraphic(Graphic(1386))
                    player.packetSender.sendMessage("You've spent 1 minute in the trio arena, one of your tokens were destroyed!")
                } else {
                    player.combatBuilder.cooldown(true)
                    player.movementQueue.reset()
                    player.moveTo(
                        Position(
                            2902, 5204
                        )
                    ) // adam change this to where ever the npc you talk to to start minigame is.
                    player.minigameAttributes.trioAttuibutes.setJoinedBossRoom(false)
                    player.packetSender.sendMessage("You've run out of tokens.")
                    stop()
                }
            }
        })
    }
}