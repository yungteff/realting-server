package com.realting.world.content.player.skill.firemaking

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.entity.character.player.Player

enum class Firelighter(//new Item[] {new Item(1511), new Item(10327)}, new Item(10328), new int[] {21, 0, 0}),
    val lighterId: Int, val coloredLogId: Int
) {
    RED_LOGS(7329, 7404),  //new Item[] {new Item(1511), new Item(7329)}, new Item(7404), new int[] {21, 0, 0}),
    GREEN_LOGS(7330, 7405),  //new Item[] {new Item(1511), new Item(7330)}, new Item(7405), new int[] {21, 0, 0}),
    BLUE_LOGS(7331, 7406),  //new Item[] {new Item(1511), new Item(7331)}, new Item(7406), new int[] {21, 0, 0}),
    PURPLE_LOGS(10326, 10329),  //new Item[] {new Item(1511), new Item(10326)}, new Item(10329), new int[] {21, 0, 0}),
    WHITE_LOGS(10327, 10328);

    companion object {
        @JvmStatic
        fun handleFirelighter(player: Player, index: Int) {
            if (!player.inventory.contains(values()[index].lighterId)) {
                player.packetSender.sendMessage("You'll need a firelighter to color logs.")
                return
            }
            player.skillManager.stopSkilling()
            player.currentTask = object : Task(1, player, false) {
                public override fun execute() {
                    if (!player.inventory.contains(1511)) {
                        player.packetSender.sendMessage("You've run out of logs to recolor.")
                        stop()
                        return
                    }
                    player.inventory.delete(1511, 1)
                    player.performAnimation(Animation(7211)) //CHANGE
                    player.inventory.add(
                        values()[index].coloredLogId, 1
                    )
                }
            }
            TaskManager.submit(player.currentTask)
        }
    }
}