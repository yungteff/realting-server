package com.realting.world.content;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.Position;
import com.realting.model.entity.character.player.Player;
import com.realting.world.content.cluescrolls.ClueScroll;

public class Digging {
	
	public static void dig(final Player player) {
		if(!player.getClickDelay().elapsed(2000))
			return;
		player.getMovementQueue().reset();
		player.getPacketSender().sendMessage("You start digging..");
		player.performAnimation(new Animation(830));
		TaskManager.submit(new Task(2, player, false) {
			@Override
			public void execute() {
				boolean clue = ClueScroll.handleClueDig(player);
				if (player.getRights().OwnerDeveloperOnly()) {
					player.getPacketSender().sendMessage("[debug] handleClueDig = "+clue);
				}
				Position targetPosition = null;
				if (inArea(player.getEntityPosition(), 3553, 3301, 3561, 3294))
					targetPosition = new Position(3578, 9706, -1);
				else if (inArea(player.getEntityPosition(), 3550, 3287, 3557, 3278))
					targetPosition = new Position(3568, 9683, -1);
				else if (inArea(player.getEntityPosition(), 3561, 3292, 3568, 3285))
					targetPosition = new Position(3557, 9703, -1);
				else if (inArea(player.getEntityPosition(), 3570, 3302, 3579, 3293))
					targetPosition = new Position(3556, 9718, -1);
				else if (inArea(player.getEntityPosition(), 3571, 3285, 3582, 3278))
					targetPosition = new Position(3534, 9704, -1);
				else if (inArea(player.getEntityPosition(), 3562, 3279, 3569, 3273))
					targetPosition = new Position(3546, 9684, -1);
				else if (inArea(player.getEntityPosition(), 2986, 3370, 3013, 3388))
					targetPosition = new Position(3546, 9684, -1);
				if (targetPosition != null) {
					player.moveTo(targetPosition);
					player.getPacketSender().sendMessage("..and break into a crypt!");
				} else if (clue) {
					player.getPacketSender().sendMessage("You manage to continue your clue.");
				} else {
					player.getPacketSender().sendMessage("You find nothing of interest.");
				}
				targetPosition = null;
				stop();
			}
		});
		player.getClickDelay().reset();
	}

	private static boolean inArea(Position pos, int x, int y, int x1, int y1) {
		return pos.getX() > x && pos.getX() < x1 && pos.getY() < y && pos.getY() > y1;
	}
}
