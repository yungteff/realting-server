package com.realting.engine.task.impl;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.util.Misc;
import com.realting.model.entity.character.player.Player;

public class BonusExperienceTask extends Task {

	public BonusExperienceTask(final Player player) {
		super(100, player, false);
		this.player = player;
	}

	final Player player;
	int msg;

	@Override
	public void execute() {
		if(player == null || !player.isRegistered()) {
			stop();
			return;
		}
		player.setMinutesBonusExp(-1, true);
		int newMinutes = player.getMinutesBonusExp();
		if(newMinutes < 0) {
			player.getPacketSender().sendMessage("<img=10> <col=330099>Your bonus experience has run out.");
			player.setMinutesBonusExp(-1, false);
			stop();
		} else if(msg == 2 && newMinutes < 10) {
			player.getPacketSender().sendMessage("<img=10> <col=330099>You have "+Misc.format(player.getMinutesBonusExp())+" minutes of bonus experience left.");
		} else if(msg == 4) {
			player.getPacketSender().sendMessage("<img=10> <col=330099>You have "+Misc.format(player.getMinutesBonusExp())+" minutes of bonus experience left.");
			msg = 0;
		}
		msg++;
	}

	public static void addBonusXp(final Player p, int minutes) {
		boolean startEvent = p.getMinutesBonusExp() == -1;
		p.setMinutesBonusExp(startEvent ? (minutes+1) : minutes, true);
		p.getPacketSender().sendMessage("<img=10> <col=330099>You have "+Misc.format(p.getMinutesBonusExp())+" minutes of bonus experience left.");
		if(startEvent) {
			TaskManager.submit(new BonusExperienceTask(p));
		}
	}
}
