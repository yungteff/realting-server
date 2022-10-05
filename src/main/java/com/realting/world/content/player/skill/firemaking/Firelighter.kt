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
        } /*public static void handleFirelighter(Player player, int firelighterid) {
		if (!player.getClickDelay().elapsed(100)) {
			return;
		}
		if (!player.getInventory().contains(1511)) {
			player.getPacketSender().sendMessage("You'll need some normal logs to use a firelighter.");
			return;
		}
		
		for (int i = 0; i < Firelighter.values().length; i++) {
			
			if (Firelighter.values()[i].getLighterId() == firelighterid) {
				int q = i;
				int logCount = player.getInventory().getAmount(1511); //1511 == normal log id
				//player.getInventory().delete(1511, logCount);
				//player.getInventory().add(Firelighter.values()[i].getColoredLogId(), logCount);
				player.setCurrentTask(new Task(1, player, true) {
					int amount = 0;
					@Override
					public void execute() {
						player.getInventory().delete(1511, 1);
						player.performAnimation(new Animation(7211)); //CHANGE
						player.getInventory().add(Firelighter.values()[q].getColoredLogId(), 1);
						amount++;
						if(amount >= logCount)
							stop();
					}
				});
				TaskManager.submit(player.getCurrentTask());
			}
			
		}
		
		player.getClickDelay().reset();
	}*/
    }
}